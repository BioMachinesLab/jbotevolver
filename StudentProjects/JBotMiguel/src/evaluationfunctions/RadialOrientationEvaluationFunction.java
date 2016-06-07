package evaluationfunctions;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class RadialOrientationEvaluationFunction extends EvaluationFunction{
	
	private double orientation = 0;
	private Vector2d position;
	private double distanceTraveled = 0;
	private double maxSpeed = 0.1;
	private double maxDistance;
	
	private boolean ignoreDistance = false;
	private boolean ignoreOrientation = false;
	
	public RadialOrientationEvaluationFunction(Arguments args) {
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
		double target = getTargetOrientation(pos);
		
		//forward
		double result = target - MathUtils.modPI2(orientation);
		result = MathUtils.modPI(result);
		result = Math.abs((Math.PI-result)/(Math.PI));
		
		//backward
		double result2 = MathUtils.modPI2(target + Math.PI) - MathUtils.modPI2(orientation);
		result2 = MathUtils.modPI(result2);
		result2 = Math.abs((Math.PI-result2)/(Math.PI));
		
		return Math.max(result, result2);
	}
	
	public static double calculateOrientationDistanceFitness(Vector2d pos, double orientation, double distanceTraveled, double maxDistance) {
		double result = calculateOrientationFitness(pos,orientation);
		result+=(maxDistance - distanceTraveled)/(maxDistance);
		return result;
	}
	
	public static double getTargetOrientation(Vector2d pos) {
		GeometricInfo sensorInfo = new GeometricCalculator().getGeometricInfoBetweenPoints(new Vector2d(0,0), 0, pos, 1);
		return -sensorInfo.getAngle();
	}
	
}
