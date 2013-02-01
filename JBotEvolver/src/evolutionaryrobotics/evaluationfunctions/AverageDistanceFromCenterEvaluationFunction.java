package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class AverageDistanceFromCenterEvaluationFunction extends EvaluationFunction{
	private double 	    fitness;
	private Vector2d    centerPosition = new Vector2d(0, 0);
	private int         numberOfSteps;
	private boolean     countEvolvingRobotsOnly = false;
	
	
	public AverageDistanceFromCenterEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator)
;		if (arguments.getFlagIsTrue("countevolvingrobotsonly")) {
			countEvolvingRobotsOnly = true;
		}

		numberOfSteps = 0;
	}

	public double getFitness() {
		if (numberOfSteps == 0) 
			return 0;
		else
			return fitness / (double) numberOfSteps;
	}

	//@Override
	public void step() {			
		Vector2d coord = new Vector2d();
		double distanceToNest = 0;
		int    robotsCounted  = 0;
		
		for(Robot r : simulator.getEnvironment().getRobots()){
			if (!countEvolvingRobotsOnly || r.getController().getClass().equals(NeuralNetworkController.class)) { 
				coord.set(r.getPosition());
				distanceToNest += coord.distanceTo(centerPosition);
				robotsCounted++;
			}
		}

		distanceToNest /= (double) robotsCounted;
		
		fitness += (double) distanceToNest * 0.1;
		numberOfSteps++;
	}

	public void enableCountEvolvingRobotsOnly() {
		    countEvolvingRobotsOnly  = true;
	}
}
