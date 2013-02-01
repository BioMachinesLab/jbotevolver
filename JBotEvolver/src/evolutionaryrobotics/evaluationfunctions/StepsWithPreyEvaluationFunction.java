package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class StepsWithPreyEvaluationFunction extends EvaluationFunction{
	private double 	    fitness;
	private boolean     countEvolvingRobotsOnly = false;
	
	
	public StepsWithPreyEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator);
		if (arguments.getFlagIsTrue("countevolvingrobotsonly")) {
			countEvolvingRobotsOnly = true;
		}
	}

	public double getFitness() {
		return fitness;
	}

	//@Override
	public void step() {				
		int robotsWithPrey = 0;
		int robotsCounted  = 0;
		
		for(Robot r : simulator.getEnvironment().getRobots()){
			if (!countEvolvingRobotsOnly || r.getController().getClass().equals(NeuralNetworkController.class)) { 
				if (r.isCarryingPrey()) {
					robotsWithPrey++;
				}
				robotsCounted++;
			}
		}

		fitness += (double) robotsWithPrey / (double) robotsCounted;
	}

	public void enableCountEvolvingRobotsOnly() {
		    countEvolvingRobotsOnly  = true;
	}
}
