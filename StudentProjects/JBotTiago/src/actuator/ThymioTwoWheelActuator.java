package actuator;

import java.util.Random;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class ThymioTwoWheelActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	protected double previousLeftSpeed = 0;
	protected double previousRightSpeed = 0;
	
	protected double leftSpeed = 0;
	protected double rightSpeed = 0;
	protected Random random;
	protected double maxSpeed = 0.155;
	
	private double speedIncrement;
	
	public ThymioTwoWheelActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.random = simulator.getRandom();
		speedIncrement = args.getArgumentAsDoubleOrSetDefault("speedincrement", 0.05);
	}

	public void setLeftWheelSpeed(double value) {
		leftSpeed = (value - 0.5) * maxSpeed * 2.0;
	}

	public void setRightWheelSpeed(double value) {
		rightSpeed = (value - 0.5) * maxSpeed * 2.0;
	}

	@Override
	public void apply(Robot robot) {
		leftSpeed*= (1 + random.nextGaussian() * NOISESTDEV);
		rightSpeed*= (1 + random.nextGaussian() * NOISESTDEV);

		if (leftSpeed < -maxSpeed)
			leftSpeed = -maxSpeed;
		else if (leftSpeed > maxSpeed)
			leftSpeed = maxSpeed;

		if (rightSpeed < -maxSpeed)
			rightSpeed = -maxSpeed;
		else if (rightSpeed > maxSpeed)
			rightSpeed = maxSpeed;
		
		double newLeftSpeed = getNewWheelSpeed(leftSpeed, previousLeftSpeed);
		double newRightSpeed = getNewWheelSpeed(rightSpeed, previousRightSpeed);
		
		((DifferentialDriveRobot) robot).setWheelSpeed(newLeftSpeed, newRightSpeed);
		
		previousLeftSpeed = newLeftSpeed;
		previousRightSpeed = newRightSpeed;
	}

	private double getNewWheelSpeed(double speed, double previousSpeed) {
		double speedDiff = speed - previousSpeed;
		double newSpeed;
		
		if (Math.abs(speedDiff) > speedIncrement)
			newSpeed = previousSpeed + speedIncrement;
		else
			newSpeed = previousSpeed + speedDiff;
		
		System.out.println("Previous Speed: " + previousSpeed);
		System.out.println("Diff: " + speedDiff);
		System.out.println("New Speed: " + newSpeed);
		System.out.println();
		
		return newSpeed;
	}

	@Override
	public String toString() {
		return "ThymioTwoWheelActuator [leftSpeed=" + leftSpeed + ", rightSpeed=" + rightSpeed + "]";
	}
	
	public double getMaxSpeed() {
		return maxSpeed;
	}
	
	public double[] getSpeed(){
		double[] velocities = {leftSpeed, rightSpeed};
		return velocities;
	}
	
	public double[] getSpeedPrecentage(){
		double leftPercentage = leftSpeed/maxSpeed;
		double rightPercentage = rightSpeed/maxSpeed;
		
		return new double[]{leftPercentage, rightPercentage};
	}

}
