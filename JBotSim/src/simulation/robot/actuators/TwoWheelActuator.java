package simulation.robot.actuators;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.SimRandom;
import simulation.util.Arguments;

public class TwoWheelActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	private double leftSpeed = 0;
	private double rightSpeed = 0;
	private SimRandom random;

	protected double maxSpeed;
	
	public TwoWheelActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id);
		this.random = simulator.getRandom();
		this.maxSpeed = arguments.getArgumentAsDoubleOrSetDefault("maxspeed", 0.1);
	}

	public void setLeftWheelSpeed(double value) {
		leftSpeed = (value - 0.5) * maxSpeed * 2.0;
	}

	public void setRightWheelSpeed(double value) {
		rightSpeed = (value - 0.5) * maxSpeed * 2.0;
	}

	public void setWheelSpeed(double leftSpeed, double rightSpeed) {
		this.leftSpeed = leftSpeed;
		this.rightSpeed = rightSpeed;
	}

	@Override
	public void apply(Robot robot) {
		leftSpeed *= (1 + random.nextGaussian() * NOISESTDEV);
		rightSpeed *= (1 + random.nextGaussian() * NOISESTDEV);

		if (leftSpeed < -Robot.MAXIMUMSPEED)
			leftSpeed = -Robot.MAXIMUMSPEED;
		else if (leftSpeed > Robot.MAXIMUMSPEED)
			leftSpeed = Robot.MAXIMUMSPEED;

		if (rightSpeed < -Robot.MAXIMUMSPEED)
			rightSpeed = -Robot.MAXIMUMSPEED;
		else if (rightSpeed > Robot.MAXIMUMSPEED)
			rightSpeed = Robot.MAXIMUMSPEED;
		robot.setWheelSpeed(leftSpeed, rightSpeed);
	}

	@Override
	public String toString() {
		return "TwoWheelActuator [leftSpeed=" + leftSpeed + ", rightSpeed="
				+ rightSpeed + "]";
	}
}
