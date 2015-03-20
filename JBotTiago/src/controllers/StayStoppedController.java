package controllers;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class StayStoppedController extends Controller {

	private double maxSpeed = 0.1;
	private double direction = 1;
	
	private int turnSteps = 0;
	
	public StayStoppedController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	@Override
	public void controlStep(double time) {
		((DifferentialDriveRobot)robot).setWheelSpeed(0, 0);
	}
	
}
