package evorbc.qualitymetrics;

import net.jafama.FastMath;
import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class RadialQualityMetric extends EvaluationFunction{

	protected double orientation = 0;
	protected boolean distance = false;
	protected DistanceQualityMetric dqm;
	protected Vector2d position;
	
	public RadialQualityMetric(Arguments args) {
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
	
	public static double calculateOrientationFitness(Vector2d pos, double orientation) {
		double target = getTargetOrientation(pos);
		
		//forward
		double result = target - MathUtils.modPI2(orientation);
		result = MathUtils.modPI(result);
		result = FastMath.abs((Math.PI-result)/(Math.PI));
		
		//backward
		double result2 = MathUtils.modPI2(target + Math.PI) - MathUtils.modPI2(orientation);
		result2 = MathUtils.modPI(result2);
		result2 = FastMath.abs((Math.PI-result2)/(Math.PI));
		
		return FastMath.max(result, result2);
	}
	
	public static double getTargetOrientation(Vector2d pos) {
		GeometricInfo sensorInfo = new GeometricCalculator().getGeometricInfoBetweenPoints(new Vector2d(0,0), 0, pos, 1);
		return -sensorInfo.getAngle();
	}

}
