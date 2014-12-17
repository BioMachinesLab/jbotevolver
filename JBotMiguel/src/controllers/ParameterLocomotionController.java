package controllers;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.robot.sensors.WallRaySensor;
import simulation.util.Arguments;

public class ParameterLocomotionController extends Controller {
	
	private double parameter = 0;
	private WallRaySensor wallSensor;
	private TwoWheelActuator wheels;
	private double parameterRange = 5;

	public ParameterLocomotionController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		this.parameter = args.getArgumentAsDoubleOrSetDefault("parameter", parameter);
		wallSensor = (WallRaySensor)robot.getSensorByType(WallRaySensor.class);
		wheels = (TwoWheelActuator)robot.getActuatorByType(TwoWheelActuator.class);
	}
	
	@Override
	public void controlStep(double time) {
		
		DifferentialDriveRobot r = (DifferentialDriveRobot)robot;
		
		boolean left = parameter < 0;
		double leftSpeed = 0;
		double rightSpeed = 0;
		
		if(wallSensor.getSensorReading(0) > 0.5) {
			//obstacle in front
			if(left) {
				leftSpeed = -wheels.getMaxSpeed()/2;
				rightSpeed = wheels.getMaxSpeed()/2;
			} else {
				leftSpeed = wheels.getMaxSpeed()/2;
				rightSpeed = +wheels.getMaxSpeed()/2;
			}
			
		} else {
			
			if(left) {
				leftSpeed = wheels.getMaxSpeed() * Math.abs(parameter/parameterRange);
				rightSpeed = wheels.getMaxSpeed();
			} else {
				leftSpeed = wheels.getMaxSpeed();
				rightSpeed = wheels.getMaxSpeed() * Math.abs(parameter/parameterRange);
			}
		}
		
		r.setWheelSpeed(leftSpeed, rightSpeed);
		
	}
	
	public double getParameter() {
		return parameter;
	}
	
	public void setParameter(double parameter) {
		this.parameter = parameter;
	}

}
