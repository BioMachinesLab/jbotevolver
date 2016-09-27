package behaviors;

import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TurnLeftBehavior extends Behavior {

	public static final float NOISESTDEV = 0;//.05f;

	private double leftSpeed = -0.01;
	private double rightSpeed = 0.01;
	private double startOrientation = 0;
	
	public TurnLeftBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
	}
	
	@Override
	public void controlStep(double time) {
		
		if(!lock)
			isLocked = false;
		
		if(!isLocked)
			startOrientation = robot.getOrientation();
		
		isLocked = true;
		
		double currentOrientation = robot.getOrientation();
		
		if(Math.abs(currentOrientation-startOrientation) >= Math.PI/2-0.05)
		{
			((DifferentialDriveRobot) robot).setWheelSpeed(0, 0);
			isLocked = false;
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
			
			((DifferentialDriveRobot) robot).setWheelSpeed(leftSpeed, rightSpeed);
		}
	}
	
	@Override
	public String toString() {
		return "TurnLeftActuator [active=" + isLocked + "]";
	}
}