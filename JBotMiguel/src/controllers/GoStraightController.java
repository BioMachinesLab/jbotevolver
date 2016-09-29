package controllers;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;

public class GoStraightController extends Controller {

	private double maxSpeed = 0;

	public GoStraightController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	@Override
	public void controlStep(double time) {
		
		if(maxSpeed == 0)
			maxSpeed = ((TwoWheelActuator)robot.getActuatorByType(TwoWheelActuator.class)).getMaxSpeed();
		((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed, maxSpeed);
	}
	
}