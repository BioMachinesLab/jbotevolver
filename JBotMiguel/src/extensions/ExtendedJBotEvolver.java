package extensions;


import java.util.ArrayList;
import java.util.HashMap;

import mathutils.Vector2d;
import neatCompatibilityImplementation.ERNEATPopulation;
import neatCompatibilityImplementation.NEATNetworkController;

import org.encog.ml.MLMethod;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class ExtendedJBotEvolver extends JBotEvolver {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		try {
			ExtendedJBotEvolver jBotEvolver = new ExtendedJBotEvolver(new String[]{"neat_cluttered.conf"});
			TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
			taskExecutor.start();
			Evolution evo = Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
			evo.executeEvolution();
			taskExecutor.stopTasks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ExtendedJBotEvolver(HashMap<String, Arguments> arguments,
			long randomSeed) {
		super(arguments, randomSeed);
	}

	public ExtendedJBotEvolver(String[] args) throws Exception {
		super(args);
	}

	/*public void setMLControllers(ArrayList<Robot> robots, MLMethod network) {
		for (Robot r : robots) {
			setIndividualMLController(r, network);
		}	
	}

	private void setIndividualMLController(Robot r, MLMethod method) {
		if(r.getController() instanceof ANNODNEATController){
			ANNODNEATController controller = (ANNODNEATController) r.getController();
			NEATNetwork net = ANNODNEATController.createCopyNetwork((NEATNetwork) method);
			controller.setNetwork(net);
		}
	}*/

	@Override
	public void setupBestIndividual(Simulator simulator) {

		ArrayList<Robot> robots;

		if(simulator.getRobots().isEmpty()) {
			robots = createRobots(simulator);
			simulator.addRobots(robots);
		} else
			robots = simulator.getRobots();

		Population p = getPopulation();
		setupControllers(p, robots);
	}

	private void setupControllers(Population pop, ArrayList<Robot> robots) {
		if(pop instanceof ERNEATPopulation){
			ERNEATPopulation neatPop = (ERNEATPopulation) pop;
			NEATGenome best = neatPop.getBestGenome();
			NEATNetwork net = (NEATNetwork) new NEATCODEC().decode(best);
			for(Robot r : robots) {
				setIndividualMLController(r, net);
			}
		}
		else {
			Chromosome c = pop.getBestChromosome();
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

	public void setMLControllers(ArrayList<Robot> robots, MLMethod network) {
		for (Robot r : robots) {
			setIndividualMLController(r, network);
		}	
	}

	private void setIndividualMLController(Robot r, MLMethod method) {
		if(r.getController() instanceof NEATNetworkController) {
			NEATNetworkController controller = (NEATNetworkController) r.getController();
			NEATNetwork network = (NEATNetwork) method;
			controller.setNetwork(network);
		}
	}

}
