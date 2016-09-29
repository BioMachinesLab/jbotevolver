package behaviors;

import epuck.Epuck;
import simulation.Simulator;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MoveForwardBehavior extends Behavior {
	
	public static final float NOISESTDEV = 0.05f;//0.05f;

	private double leftSpeed = 0.10;
	private double rightSpeed = 0.10;
	private double prev = 0;
	private double noise = 0;
	
	public MoveForwardBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		this.noise = simulator.getRandom().nextGaussian()*NOISESTDEV;
		leftSpeed+=leftSpeed*noise;
		rightSpeed+=rightSpeed*noise;
	}

	@Override
	public void controlStep(double time) {
		
		double percentage = 0.15/2.0;
		
		double speedRight = rightSpeed;
		double speedLeft = leftSpeed;
		
		if(robot instanceof Epuck && ((Epuck)robot).getIrSensor() != null) {
		
			double distRight = ((Epuck) robot).getIrSensor().last2;
			double distLeft  = ((Epuck) robot).getIrSensor().last5;

		    if(prev == 0){
		        if(distLeft > distRight) prev = distLeft;
		        if(distRight > distLeft) prev = distRight;
		    }
	
		    if(distLeft > distRight && distLeft > 0.8) {
		        speedRight-=speedRight*percentage;
		    }else if(distRight > distLeft && distRight > 0.8) {
		        speedLeft-=speedLeft*percentage;
		    }else {
		        
		        if(distLeft > distRight) {
		            if(prev < distLeft)
		                speedRight-=speedRight*percentage;
		            else
		                speedLeft-=speedLeft*percentage;
		            prev=distLeft;
		        }else if(distRight > distLeft) {
		            if(prev < distRight)
		                speedLeft-=speedLeft*percentage;
		            else
		                speedRight-=speedRight*percentage;
		            prev=distRight;
		        }
		    }
		}
		
		((DifferentialDriveRobot) robot).setWheelSpeed(speedLeft, speedRight);
		
		/*if(robot.getIrSensor() != null) {
		
			double last2 = robot.getIrSensor().last2;
			double last5 = robot.getIrSensor().last5;
			double left = leftSpeed;
			double right = rightSpeed;
			
			if(last2 < last5) {
				right-=0.002;
			} else if(last5 < last2) {
				left-=0.002;
			}
			
			robot.setWheelSpeed(left, right);
			
		}*/
		
		/*double alignment = 	(robot.getOrientation()+Math.PI)%(Math.PI/2);
		//System.out.println(alignment);
		
		leftSpeed *= (1 + simulator.getRandom().nextGaussian() * NOISESTDEV);
		rightSpeed *= (1 + simulator.getRandom().nextGaussian() * NOISESTDEV);

		if (leftSpeed < -Robot.MAXIMUMSPEED)
			leftSpeed = -Robot.MAXIMUMSPEED;
		else if (leftSpeed > Robot.MAXIMUMSPEED)
			leftSpeed = Robot.MAXIMUMSPEED;

		if (rightSpeed < -Robot.MAXIMUMSPEED)
			rightSpeed = -Robot.MAXIMUMSPEED;
		else if (rightSpeed > Robot.MAXIMUMSPEED)
			rightSpeed = Robot.MAXIMUMSPEED;
		
		double closeToPi = Math.PI/2-alignment;
		double closeToZero = alignment;
		
		boolean left = true;
		
		double pequeno = Math.min(closeToPi,closeToZero);
		
		if(pequeno == closeToZero)
			left = false;
		
		if(left)
			robot.setWheelSpeed(leftSpeed-0.01, rightSpeed);
		else
			robot.setWheelSpeed(leftSpeed, rightSpeed-0.01);*/
	}
	
	@Override
	public String toString() {
		return "MoveForward";
	}
}