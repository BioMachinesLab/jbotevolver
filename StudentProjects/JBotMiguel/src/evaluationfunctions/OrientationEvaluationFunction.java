package evaluationfunctions;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class OrientationEvaluationFunction extends EvaluationFunction{
	
	private double orientation = 0;
	private double targetOrientation = 0;
	private boolean target = false;
	private Vector2d position;
	
	public OrientationEvaluationFunction(Arguments args) {
		super(args);
		
		if(args.getArgumentIsDefined("target")) {
			this.target = true;
			this.targetOrientation = Math.toRadians(args.getArgumentAsDouble("target"));
		}
	}

	@Override
	public void update(Simulator simulator) {
		Robot r = simulator.getRobots().get(0);
		orientation = r.getOrientation();
		position = r.getPosition();
	}
	
	@Override
	public double getFitness() {
		
		if(!target)
			targetOrientation = getOrientationFromCircle(position);
		
		double result = targetOrientation - MathUtils.modPI2(orientation);
		result = MathUtils.modPI(result);
		result = Math.abs((Math.PI-result)/(Math.PI));
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
