package fourwheeledrobot;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class FourWheelFourAxesActuator extends MultipleWheelAxesActuator{
	
	
	/**
	 * Diagram of the robot (north pointing forward) and the positions of the 4 wheels (.) and the four axes (O)
	 * 
	 *   (forward)
	 *       ^
	 *  0 .O___O. 1
	 *     |   |
	 *  2.O|___|O.3
	 */
	
	public FourWheelFourAxesActuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,4,4);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		
		double rFrontLeft = rotation[0];
		double rFrontRight = rotation[1];
		double rBackLeft = rotation[2];
		double rBackRight = rotation[3];
		
		double speedFrontLeft = speeds[0];
		double speedFrontRight = speeds[1];
		double speedBackLeft = speeds[2];
		double speedBackRight = speeds[3];
		
		Vector2d position = robot.getPosition();
		
		double orientation = robot.getOrientation();
		
		Vector2d frontWheelLeft = new Vector2d(position);
		frontWheelLeft.add(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		Vector2d frontWheelRight = new Vector2d(frontWheelLeft);
		
		Vector2d backWheelLeft = new Vector2d(position);
		backWheelLeft.sub(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		Vector2d backWheelRight = new Vector2d(backWheelLeft);
		
		backWheelLeft.add(new Vector2d(timeDelta*speedBackLeft*Math.cos(orientation+rBackLeft),timeDelta*speedBackLeft*Math.sin(orientation+rBackLeft)));
		backWheelRight.add(new Vector2d(timeDelta*speedBackRight*Math.cos(orientation+rBackRight),timeDelta*speedBackRight*Math.sin(orientation+rBackRight)));
		frontWheelLeft.add(new Vector2d(timeDelta*speedFrontLeft*Math.cos(orientation+rFrontLeft),timeDelta*speedFrontLeft*Math.sin(orientation+rFrontLeft)));
		frontWheelRight.add(new Vector2d(timeDelta*speedFrontRight*Math.cos(orientation+rFrontRight),timeDelta*speedFrontRight*Math.sin(orientation+rFrontRight)));
		
		
		Vector2d frontWheels = new Vector2d(frontWheelLeft);
		frontWheels.add(frontWheelRight);
		frontWheels = new Vector2d(frontWheels.x/2,frontWheels.y/2);
		
		Vector2d backWheels = new Vector2d(backWheelLeft);
		backWheels.add(backWheelRight);
		backWheels = new Vector2d(backWheels.x/2,backWheels.y/2);
		
		Vector2d sum = new Vector2d(frontWheels);
		sum.add(backWheels);
		
		double newOrientation = (Math.atan2(frontWheels.y-backWheels.y,frontWheels.x-backWheels.x));
		
		robot.setPosition(new Vector2d(sum.x/2,sum.y/2));
		robot.setOrientation(newOrientation);
	}
	
}
