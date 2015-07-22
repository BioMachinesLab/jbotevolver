package evolutionaryrobotics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.neuralnetworks.MultipleChromosome;
import evolutionaryrobotics.populations.Population;

public class JBotEvolver extends JBotSim {
	
	public JBotEvolver(HashMap<String,Arguments> arguments, long randomSeed) {
		super(arguments,randomSeed);
	}
	
	public JBotEvolver(String[] args) throws Exception {
		super(args);
	}
	
	public EvaluationFunction getEvaluationFunction() {
		EvaluationFunction eval;
		if(arguments.get("--evaluation") != null)
			eval = EvaluationFunction.getEvaluationFunction(arguments.get("--evaluation"));
		else
			eval = EvaluationFunction.getEvaluationFunction(arguments.get("--evaluationa"));
		return eval;
	}
	
	public EvaluationFunction getSpecificEvaluationFunction(String name) {
		return EvaluationFunction.getEvaluationFunction(arguments.get("--evaluation" + name.toLowerCase()));
	}
	
	public Population getPopulation() {
		try {
			return Population.getPopulation(getArguments().get("--population"));
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public Population getSpecificPopulation(String name) {
		try {
			return Population.getCoEvolutionPopulations(getArguments().get("--population"),name);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
	
	public ArrayList<Robot> createRobots(Simulator simulator, Chromosome chromosome) {
		
		ArrayList<Robot> totalRobots = new ArrayList<Robot>();
		
		if(chromosome instanceof MultipleChromosome){
		
			MultipleChromosome c = (MultipleChromosome) chromosome;
			
			int previousrobots = 0;
			
			for(int i = 0; i < c.getNumberOfChromosomes(); i++){
				// get("robots" + i)
				ArrayList<Robot> robots;
				if(i == 0) {
					robots = Robot.getRobots(simulator, arguments.get("--robots"));
					previousrobots = robots.size();
				} else{
					arguments.get("--robots" + i).setArgument("previousrobots", previousrobots);
					robots = Robot.getRobots(simulator, arguments.get("--robots" + i));
				}
				
				for(Robot r : robots) {
					if(i == 0)
						r.setController(Controller.getController(simulator, r, arguments.get("--controllers")));
					else
						r.setController(Controller.getController(simulator, r, arguments.get("--controllers" + i)));
					totalRobots.add(r);
					
					Chromosome subChromosome = c.getChromosome(i);
					
					if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
						FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)r.getController();
						controller.setNNWeights(subChromosome.getAlleles());
					}
				}
			}
			
		} else {
			totalRobots = createRobots(simulator);
			for (Robot r : totalRobots) {
				if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
					FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)r.getController();
					controller.setNNWeights(chromosome.getAlleles());
				}
			}
		}
		
		return totalRobots;
	}
	
	public void setChromosome(ArrayList<Robot> robots, Chromosome chromosome) {
		for (Robot r : robots) {
			
			if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)r.getController();
				controller.setNNWeights(chromosome.getAlleles());
			}
		}	
	}
	
	@Override
	protected void loadArguments(String[] args) throws IOException, ClassNotFoundException {
		super.loadArguments(args);
		
		String absolutePath = "";
		
		if(arguments.get("--output") != null)
			absolutePath = (new File("./"+arguments.get("--output").getCompleteArgumentString())).getCanonicalPath();
		
		if(arguments.get("--population") != null) {
			if(parentFolder.isEmpty()) {
				arguments.get("--population").setArgument("parentfolder", absolutePath);
			}else{
				arguments.get("--population").setArgument("parentfolder", parentFolder);
			}
		}
	}

	public void setupBestIndividual(Simulator simulator) {
		ArrayList<Robot> robots;
		
		Population p = getPopulation();
		
		if(simulator.getRobots().isEmpty()) {
			robots = createRobots(simulator, p.getBestChromosome());
			simulator.addRobots(robots);
		} else{
			robots = simulator.getRobots();
			for(Robot r : robots) {
				p.setupIndividual(r);
			}
		}
	}
	
	public void setupBestCoIndividual(Simulator simulator) {
		
		// Obter o numero de presas
		Arguments numbRobotsPreys= getArguments().get("--robots");
		int nPreys = numbRobotsPreys.getArgumentAsIntOrSetDefault("numberofrobots", 1);
		
		ArrayList<Robot> robots;
		
		if(simulator.getRobots().isEmpty()) {
			robots = createCoEvolutionRobots(simulator);
			simulator.addRobots(robots);
		} else
			robots = simulator.getRobots();
		
		ArrayList<Robot> preys = new ArrayList<Robot>();
		ArrayList<Robot> predators = new ArrayList<Robot>();
		for (int j = 0; j < robots.size(); j++) {
			if (j < nPreys) {
				preys.add(robots.get(j));
			} else {
				predators.add(robots.get(j));
			}
		}
		
		Population pa = getSpecificPopulation("a");
		Arguments popArgs = getArguments().get("--population");
		
		Chromosome ca;
		
		if(popArgs.getArgumentIsDefined("chromosomea"))
			ca = pa.getChromosome(popArgs.getArgumentAsInt("chromosomea"));
		else
			ca = pa.getBestChromosome();
		
		for(Robot r : preys) {
			if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController fc = (FixedLenghtGenomeEvolvableController)r.getController();
				if(fc.getNNWeights() == null) {
					fc.setNNWeights(ca.getAlleles());
				}
			}
		}
		
		Population pb = getSpecificPopulation("b");
		
		popArgs = getArguments().get("--population");
		
		Chromosome cb;
		
		if(popArgs.getArgumentIsDefined("chromosomeb"))
			cb = pb.getChromosome(popArgs.getArgumentAsInt("chromosomeb"));
		else
			cb = pb.getBestChromosome();
		
		System.out.println(ca.getID()+" "+cb.getID());
		
		for(Robot r : predators) {
			if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController fc = (FixedLenghtGenomeEvolvableController)r.getController();
				if(fc.getNNWeights() == null) {
					fc.setNNWeights(cb.getAlleles());
				}
			}
		}
		
	}
}