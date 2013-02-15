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
import gui.Gui;

public class JBotEvolver extends JBotSim {
	
	public JBotEvolver(HashMap<String,Arguments> arguments, long randomSeed) {
		super(arguments,randomSeed);
	}
	
	public JBotEvolver(String[] args) throws Exception {
		super(args);
		if(arguments.get("--gui") != null)
			Gui.getGui(this,arguments.get("--gui"));
	}
	
	public EvaluationFunction getEvaluationFunction() {
		return EvaluationFunction.getEvaluationFunction(arguments.get("--evaluation"));
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
			if(parentFolder.isEmpty())
				arguments.get("--population").setArgument("parentfolder", absolutePath);
			else
				arguments.get("--population").setArgument("parentfolder", parentFolder);
		}
	}

	public void setupBestIndividual(Simulator simulator) {
		
		ArrayList<Robot> robots;
		
		if(simulator.getRobots().isEmpty()) {
			robots = createRobots(simulator);
			simulator.addRobots(robots);
		} else
			robots = simulator.getRobots();
		
		Population p = getPopulation();
		Chromosome c = p.getBestChromosome();
		for(Robot r : robots) {
			if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController fc = (FixedLenghtGenomeEvolvableController)r.getController();
				if(fc.getNNWeights() == null) {
					fc.setNNWeights(c.getAlleles());
				}
			}
		}
	}
}