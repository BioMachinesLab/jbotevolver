package evorbc.qualitymetrics;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CircularQualityMetric extends EvaluationFunction{

	protected double orientation = 0;
	protected boolean distance = false;
	protected DistanceQualityMetric dqm;
	protected Vector2d position;
	
	public CircularQualityMetric(Arguments args) {
		super(args);
		
		distance = args.getFlagIsTrue("distance");
		
		if(distance)
			dqm = new DistanceQualityMetric(args);
	}

	@Override
	public void update(Simulator simulator) {
		Robot r = simulator.getRobots().get(0);
		
		position = new Vector2d(r.getPosition());
		orientation = r.getOrientation();
		
		if(distance)
			dqm.update(simulator);
	}
	
	@Override
	public double getFitness() {
		double fitness = calculateOrientationFitness(position, orientation);
		
		if(distance)
			fitness+=dqm.getFitness();
		
		return fitness;
	}
	
	public double getDistanceFitness() {
		if(distance)
			return dqm.getFitness();
		return 0;
	}
	
	public double getOrientationFitness() {
		return calculateOrientationFitness(position, orientation);
	}
	
	public static double calculateOrientationFitness(Vector2d pos, double orientation) {
		double result = getTargetOrientation(pos) - MathUtils.modPI2(orientation);
		result = MathUtils.modPI(result);
		result = Math.abs((Math.PI-result)/(Math.PI));
		return result;
	}
	
	public static double getTargetOrientation(Vector2d pos) {
		double b = Math.sqrt((pos.x/2.0)*(pos.x/2.0)+(pos.y/2.0)*(pos.y/2.0));
		double alpha = Math.atan2(pos.y,pos.x);
		double a = b/Math.cos(alpha);
		double beta = Math.atan2(pos.y,pos.x-a);
		double orientation = 0;
		
		if(pos.x < 0)
			orientation = beta-Math.PI;
		else
			orientation = beta;
		
		orientation = MathUtils.modPI(orientation);
		
		return orientation;
	}

}
