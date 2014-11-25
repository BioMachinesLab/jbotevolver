package controllers;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GoToWallController extends Controller {

	private double maxSpeed = 0.1;
	private double direction = 1;
	
	private int turnSteps = 0;
	
	public GoToWallController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	@Override
	public void controlStep(double time) {
		
		//Wall Sensor
		double rightSpeed = robot.getSensorWithId(1).getSensorReading(0);
		double leftSpeed  = robot.getSensorWithId(1).getSensorReading(1);
		  
		double maxVal = Math.max(rightSpeed, leftSpeed);
		
		if(turnSteps > 0) {
			turnSteps--;
			((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed*direction, -maxSpeed);
		}else{
			if(maxVal < 0.1){
				((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed,maxSpeed);
			}else{
				turnSteps = 8;
				((DifferentialDriveRobot)robot).setWheelSpeed(maxSpeed*direction, -maxSpeed);
			}
		}
		
	}
	
}
