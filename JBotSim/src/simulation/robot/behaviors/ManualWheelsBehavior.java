package simulation.robot.behaviors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.SimRandom;

public class ManualWheelsBehavior extends Behavior{
	
	public static final float NOISESTDEV = 0;//0.05f;
	private static double MAX_SPEED = 0.05;
	private double leftValue = 0;
	private double rightValue = 0;
	private SimRandom random;
	
	public ManualWheelsBehavior(Simulator simulator, Robot robot, boolean lock) {
		super(simulator, robot, lock);
		this.numberOfOutputs = 3;
		this.random = simulator.getRandom();
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public void applyBehavior() {
		
		double leftSpeed = (leftValue*2-1)*MAX_SPEED;
		double rightSpeed = (rightValue*2-1)*MAX_SPEED;
		
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
		
		robot.setWheelSpeed(leftSpeed, rightSpeed);
	}
	
	public void setValue(int index, double value) {
		if(index == 1)
			leftValue = value;
		else if(index == 2)
			rightValue = value;
	}
}