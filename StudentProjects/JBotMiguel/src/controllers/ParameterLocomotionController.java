package controllers;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;

public class ParameterLocomotionController extends ParameterController {
	
	private TwoWheelActuator wheels;

	public ParameterLocomotionController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		wheels = (TwoWheelActuator)robot.getActuatorByType(TwoWheelActuator.class);
	}
	
	@Override
	public void controlStep(double time) {
		
		DifferentialDriveRobot r = (DifferentialDriveRobot)robot;
		
		double leftSpeed = 0;
		double rightSpeed = 0;
		
		if(behavior == 0) {
			//left on the spot
			leftSpeed = -wheels.getMaxSpeed() * behaviorPercentage;
			rightSpeed = -leftSpeed;
			
		} else if(behavior == 1) {
			//forward
			leftSpeed = wheels.getMaxSpeed() * behaviorPercentage;
			rightSpeed = leftSpeed;
			
		} else if(behavior == 2) {
			//right on the spot
			leftSpeed = wheels.getMaxSpeed() * behaviorPercentage;
			rightSpeed = -leftSpeed;
			
		}
		r.setWheelSpeed(leftSpeed, rightSpeed);
	}
	
}