package simulation.robot;

import mathutils.MathUtils;
import simulation.Simulator;
import simulation.physicalobjects.collisionhandling.knotsandbolts.CircularShape;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class DifferentialDriveRobot extends Robot {
	/**
	 * Diameter of the robot's wheels.
	 */
	protected double     wheelDiameter      = 0.05;

	/**
	 * Current speed of the left wheel.
	 */	
	protected double     leftWheelSpeed     = 0;
	/**
	 * Current speed of the right wheel.
	 */	
	protected double     rightWheelSpeed    = 0;
	
	/**
	 * Distance between the wheels of the robot.
	 */
	protected double distanceBetweenWheels = 0.05;
	
	public DifferentialDriveRobot(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.distanceBetweenWheels = args.getArgumentAsDoubleOrSetDefault("distancewheels", ((CircularShape)shape).getDiameter());
	}
	
	public void updateActuators(Double time, double timeDelta) {	
		position.set(
				position.getX() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * Math.cos(orientation),
				position.getY() + timeDelta * (leftWheelSpeed + rightWheelSpeed) / 2.0 * Math.sin(orientation));
		
		orientation += timeDelta * 0.5/(distanceBetweenWheels/2.0) * (rightWheelSpeed - leftWheelSpeed); 

		orientation = MathUtils.modPI2(orientation);
		
		for(Actuator actuator: actuators){
			actuator.apply(this);
		}
	}

	/**
	 * Set the rotational speed of the wheels (in radians)
	 * 
	 * @param left the rotational speed of the left wheel (in radians)
	 * @param right the rotational speed of the right wheel (in radians)
	 */	
	public void setRotationalWheelSpeed(double left, double right) {
		leftWheelSpeed  = left * wheelDiameter * Math.PI;
		rightWheelSpeed = right * wheelDiameter * Math.PI;	
	}

	
	/**
	 * Set the speed of the left and the right wheel in terms of how far each wheel drives a robot forward or backwards (in meters/second).
	 * 
	 * @param left the speed of the left wheel (in meters/second)
	 * @param right the speed of the right wheel (in meters/second)
	 */
	public void setWheelSpeed(double left, double right) {
		leftWheelSpeed  = left;
		rightWheelSpeed = right;	
	}

	public double getDistanceBetweenWheels() {
		return distanceBetweenWheels;
	}
	
	public double getRightWheelSpeed() {
		return rightWheelSpeed;
	}
	
	public double getLeftWheelSpeed() {
		return leftWheelSpeed;
	}
	
	public double getWheelDiameter(){
		return this.wheelDiameter;
	}
}