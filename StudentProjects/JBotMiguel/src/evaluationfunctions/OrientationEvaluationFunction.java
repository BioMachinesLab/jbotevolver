package evaluationfunctions;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class OrientationEvaluationFunction extends EvaluationFunction{
	
	private double orientation = 0;
	private Vector2d position;
	private double distanceTraveled = 0;
	private double maxSpeed = 0.1;
	private double maxDistance;
	
	private boolean ignoreDistance = false;
	private boolean ignoreOrientation = false;
	
	public OrientationEvaluationFunction(Arguments args) {
		super(args);
		ignoreDistance = args.getFlagIsTrue("ignoredistance");
		ignoreOrientation = args.getFlagIsTrue("ignoreorientation");
	}

	@Override
	public void update(Simulator simulator) {
		Robot r = simulator.getRobots().get(0);
		
		if(position != null) {
			distanceTraveled+=position.distanceTo(r.getPosition());
		} else {
			maxDistance = simulator.getEnvironment().getSteps()*maxSpeed*simulator.getTimeDelta();
		}
		
		orientation = r.getOrientation();
		position = new Vector2d(r.getPosition());
	}
	
	@Override
	public double getFitness() {
		double resultDistance = ignoreDistance ? 0 : (maxDistance - distanceTraveled)/(maxDistance);
		double resultOrientation = ignoreOrientation ? 0 : calculateOrientationFitness(position, orientation);
		return resultOrientation + resultDistance;
	}
	
	public static double calculateOrientationFitness(Vector2d pos, double orientation) {
		double result = getOrientationFromCircle(pos) - MathUtils.modPI2(orientation);
		result = MathUtils.modPI(result);
		result = Math.abs((Math.PI-result)/(Math.PI));
		return result;
	}
	
	public static double calculateOrientationDistanceFitness(Vector2d pos, double orientation, double distanceTraveled, double maxDistance) {
		double result = getOrientationFromCircle(pos) - MathUtils.modPI2(orientation);
		result = MathUtils.modPI(result);
		result = Math.abs((Math.PI-result)/(Math.PI));
		result+=(maxDistance - distanceTraveled)/(maxDistance);
		return result;
	}
	
	public static double getOrientationFromCircle(Vector2d pos) {
		
		double b = Math.sqrt((pos.x/2)*(pos.x/2)+(pos.y/2)*(pos.y/2));
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
