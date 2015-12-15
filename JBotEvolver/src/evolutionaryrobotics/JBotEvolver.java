package evolutionaryrobotics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class JBotEvolver extends JBotSim {
	
	public JBotEvolver(HashMap<String,Arguments> arguments, long randomSeed) {
		super(arguments,randomSeed);
	}
	
	public JBotEvolver(String[] args) throws Exception {
		super(args);
	}
	
	public EvaluationFunction[] getEvaluationFunction() {
		EvaluationFunction[] evals = new EvaluationFunction[1];
		Arguments evalArgs = arguments.get("--evaluation");
		if(evalArgs != null) {
			
			if(!evalArgs.getArgumentIsDefined("multieval"))
				evals[0] = EvaluationFunction.getEvaluationFunction(evalArgs);
			else {
				//for multiobjective EAs
				Arguments multiObjectives = new Arguments(evalArgs.getArgumentAsString("multieval"));
				evals = new EvaluationFunction[multiObjectives.getNumberOfArguments()];
				for(int i = 0 ; i < evals.length ; i++) {
					evals[i] = EvaluationFunction.getEvaluationFunction(new Arguments(multiObjectives.getArgumentAsString(multiObjectives.getArgumentAt(i))));
				}
			}
		}else
			evals[0] = EvaluationFunction.getEvaluationFunction(arguments.get("--evaluationa"));
		return evals;
	}
	
	public EvaluationFunction getSpecificEvaluationFunction(String name) {
		return EvaluationFunction.getEvaluationFunction(arguments.get("--evaluation" + name.toLowerCase()));
	}
	
	public Population getPopulation() {
		try {
			return Population.getPopulation(getArguments().get("--population"));
		} catch(Exception e) {
			e.printStackTrace();
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
		return chromosome.setupRobots(this, simulator);
	}
	
	public void setChromosome(ArrayList<Robot> robots, Chromosome chromosome) {
		
		for (Robot r : robots)
			chromosome.setupRobot(r);
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