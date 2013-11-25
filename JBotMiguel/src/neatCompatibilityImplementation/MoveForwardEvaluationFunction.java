package neatCompatibilityImplementation;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import extensions.MoveForwardEnvironment;

public class MoveForwardEvaluationFunction extends EvaluationFunction {

	protected int steps;
	protected MoveForwardEnvironment env;

	public MoveForwardEvaluationFunction(Arguments args) {
		super(args);
		steps = args.getArgumentAsIntOrSetDefault("steps", 100);
	}

	@Override
	public void update(Simulator simulator) {
		
		env = (MoveForwardEnvironment) simulator.getEnvironment();
		Robot r = env.getRobots().get(0);
		Vector2d currentPosition = r.getPosition();
//		FrontWallRaySensor sensor = (FrontWallRaySensor) r.getSensorByType(FrontWallRaySensor.class);
//
//		double totalDistance = env.getTotalDistance(sensor.getRange());
//		double currentDistance = env.computeTraveledDistance(currentPosition);
//		double percentage = (totalDistance-currentDistance)/totalDistance;
//		fitness = percentage;
//		//System.out.println("F: " + fitness);
//		
//		if(currentDistance >= totalDistance){
//			fitness = 1.0 + (steps-simulator.getTime())/steps;
//			simulator.stopSimulation();
//		}
	}

}
