package evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class DistanceTravelledEvaluationFunction extends EvaluationFunction{
	
	private Vector2d position;
	private double distanceTraveled = 0;
	private double maxSpeed = 0.1;
	private double maxDistance;
	
	public DistanceTravelledEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		Robot r = simulator.getRobots().get(0);
		
		if(position != null) {
			distanceTraveled+=position.distanceTo(r.getPosition());
		} else {
			maxDistance = simulator.getEnvironment().getSteps()*maxSpeed*simulator.getTimeDelta();
		}
		
		position = new Vector2d(r.getPosition());
	}
	
	@Override
	public double getFitness() {
		return (maxDistance - distanceTraveled)/(maxDistance);
	}
	
}
