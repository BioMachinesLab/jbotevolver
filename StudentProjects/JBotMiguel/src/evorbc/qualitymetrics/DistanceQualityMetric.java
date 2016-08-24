package evorbc.qualitymetrics;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class DistanceQualityMetric extends EvaluationFunction{

	protected Vector2d position;
	protected double distanceTraveled = 0;
	
	public DistanceQualityMetric(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		Robot r = simulator.getRobots().get(0);
		
		if(position != null) {
			distanceTraveled+=position.distanceTo(r.getPosition());
		}
		
		position = new Vector2d(r.getPosition());
	}
	
	@Override
	public double getFitness() {
		//minDistance is the distance in a straight line
		double minDistance = position.length();
		
		//the edge case where the best thing is to stay put
		if(distanceTraveled == 0)
			return 1;
		
		return minDistance/distanceTraveled;
	}

}
