package controllers;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.sensors.RobotSensor;
import simulation.util.Arguments;

public class RandomRobotController extends Controller {

	private static final double INITIAL_CIRCLE_WHEN_LOST = 0.3;
	private double multFactor = INITIAL_CIRCLE_WHEN_LOST;
	private double maxSpeed = 0.1;

	public RandomRobotController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		if(args.getArgumentIsDefined("actuators")){
			Arguments actuators = new Arguments(args.getArgumentAsString("actuators"));
			maxSpeed =actuators.getArgumentAsDoubleOrSetDefault("maxspeed", 0.1);
		}
	}

	@Override
	public void controlStep(double time) {
		double rightSpeed = robot.getSensorWithId(1).getSensorReading(0);
		double leftSpeed  = robot.getSensorWithId(1).getSensorReading(1);
		
		((DifferentialDriveRobot)robot).setWheelSpeed(leftSpeed * maxSpeed, rightSpeed * maxSpeed);
		
		if(leftSpeed == 0.0 && rightSpeed == 0.0)
			((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed, maxSpeed * Math.min(.98, 1 - multFactor));
	}

}
