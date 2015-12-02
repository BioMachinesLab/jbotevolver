package simulation.robot.actuators;

import java.util.Random;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class TwoWheelActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	protected double leftSpeed = 0;
	protected double rightSpeed = 0;
	protected Random random;
	@ArgumentsAnnotation(name="maxspeed", defaultValue = "0.1")
	protected double maxSpeed;
	
	public TwoWheelActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		this.random = simulator.getRandom();
		this.maxSpeed = arguments.getArgumentAsDoubleOrSetDefault("maxspeed", 0.1);
	}

	public void setLeftWheelSpeed(double value) {
		leftSpeed = (value - 0.5) * maxSpeed * 2.0;
	}

	public void setRightWheelSpeed(double value) {
		rightSpeed = (value - 0.5) * maxSpeed * 2.0;
	}

	@Override
	public void apply(Robot robot, double timeDelta) {
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
		((DifferentialDriveRobot) robot).setWheelSpeed(leftSpeed, rightSpeed);
	}

	@Override
	public String toString() {
		return "TwoWheelActuator [leftSpeed=" + leftSpeed + ", rightSpeed="
				+ rightSpeed + "]";
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