package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class AverageDistanceFromCenterEvaluationFunction extends EvaluationFunction{
	private Vector2d    centerPosition = new Vector2d(0, 0);
	private boolean     countEvolvingRobotsOnly = false;
	private double time;
	
	public AverageDistanceFromCenterEvaluationFunction(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		countEvolvingRobotsOnly = arguments.getFlagIsTrue("countevolvingrobotsonly");
	}
	
	@Override
	public double getFitness() {
		return time > 0 ? fitness/time : 0;
	}

	@Override
	public void update(double time) {
		this.time = time;
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
	}
}