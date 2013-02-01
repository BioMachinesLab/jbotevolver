package simulation.robot.behaviors;

import simulation.Simulator;
import simulation.robot.Robot;

public class TurnLeftBehavior extends Behavior {

	public static final float NOISESTDEV = 0;//.05f;

	private double leftSpeed = -0.01;
	private double rightSpeed = 0.01;
	private boolean active = false;
	private double startOrientation = 0;
	
	public TurnLeftBehavior(Simulator simulator, Robot r, boolean lock) {
		super(simulator, r, lock);
	}
	
	public boolean isLocked() {
		if(lock)
			return active;
		else
			return false;
	}
	
	@Override
	public void applyBehavior() {
		
		if(!lock)
			active = false;
		
		if(!active)
			startOrientation = robot.getOrientation();
		
		active = true;
		
		double currentOrientation = robot.getOrientation();
		
		if(Math.abs(currentOrientation-startOrientation) >= Math.PI/2-0.05)
		{
			robot.setWheelSpeed(0, 0);
			active = false;
		} else {
			/*leftSpeed *= (1 + simulator.getRandom().nextGaussian() * NOISESTDEV);
			rightSpeed *= (1 + simulator.getRandom().nextGaussian() * NOISESTDEV);
	
			if (leftSpeed < -Robot.MAXIMUMSPEED)
				leftSpeed = -Robot.MAXIMUMSPEED;
			else if (leftSpeed > Robot.MAXIMUMSPEED)
				leftSpeed = Robot.MAXIMUMSPEED;
	
			if (rightSpeed < -Robot.MAXIMUMSPEED)
				rightSpeed = -Robot.MAXIMUMSPEED;
			else if (rightSpeed > Robot.MAXIMUMSPEED)
				rightSpeed = Robot.MAXIMUMSPEED;*/
			
			robot.setWheelSpeed(leftSpeed, rightSpeed);
		}
	}
	
	@Override
	public String toString() {
		return "TurnLeftActuator [active=" + active + "]";
	}

}
