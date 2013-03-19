package simulation.robot.actuators;

import java.util.Random;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoWheelActuator extends Actuator {

	public static final float NOISESTDEV = 0.05f;

	private double leftSpeed = 0;
	private double rightSpeed = 0;
	private Random random;
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

	public void setWheelSpeed(double leftSpeed, double rightSpeed) {
		this.leftSpeed = leftSpeed;
		this.rightSpeed = rightSpeed;
	}

	@Override
	public void apply(Robot robot) {
		leftSpeed*= (1 + random.nextGaussian() * NOISESTDEV);
		rightSpeed*= (1 + random.nextGaussian() * NOISESTDEV);

		if (leftSpeed < -Robot.MAXIMUMSPEED)
			leftSpeed = -Robot.MAXIMUMSPEED;
		else if (leftSpeed > Robot.MAXIMUMSPEED)
			leftSpeed = Robot.MAXIMUMSPEED;

		if (rightSpeed < -Robot.MAXIMUMSPEED)
			rightSpeed = -Robot.MAXIMUMSPEED;
		else if (rightSpeed > Robot.MAXIMUMSPEED)
			rightSpeed = Robot.MAXIMUMSPEED;
		((DifferentialDriveRobot) robot).setWheelSpeed(leftSpeed, rightSpeed);
//		((DifferentialDriveRobot) robot).setWheelSpeed(maxSpeed/4, -maxSpeed/4);
	}

	@Override
	public String toString() {
		return "TwoWheelActuator [leftSpeed=" + leftSpeed + ", rightSpeed="
				+ rightSpeed + "]";
	}
}