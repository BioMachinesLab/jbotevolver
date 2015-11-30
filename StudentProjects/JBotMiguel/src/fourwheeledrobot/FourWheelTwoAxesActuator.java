package fourwheeledrobot;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FourWheelTwoAxesActuator extends MultipleWheelAxesActuator{
	
	/**
	 * Diagram of the robot (north pointing forward) and the positions of the 4 wheels (.) and the two axes (O)
	 * 
	 * (forward)
	 *     ^
	 *  0 ._O_. 1
	 *    |   |
	 *  2.|_O_|.3
	 */
	
	public FourWheelTwoAxesActuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,4,2);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		
		double rFront = rotation[0];
		double rBack = rotation[1];
		double speedFront = (speeds[0]+speeds[1])/2.0;
		double speedBack = (speeds[2]+speeds[3])/2.0;
		
		Vector2d position = robot.getPosition();
		
		double orientation = robot.getOrientation();
		
		Vector2d frontWheel = new Vector2d(position);
		frontWheel.add(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		
		Vector2d backWheel = new Vector2d(position);
		backWheel.sub(new Vector2d(distanceBetweenWheels/2.0*Math.cos(orientation),distanceBetweenWheels/2.0*Math.sin(orientation)));
		
		backWheel.add(new Vector2d(timeDelta*speedBack*Math.cos(orientation+rBack),timeDelta*speedBack*Math.sin(orientation+rBack)));
		frontWheel.add(new Vector2d(timeDelta*speedFront*Math.cos(orientation+rFront),timeDelta*speedFront*Math.sin(orientation+rFront)));
		
		Vector2d sum = new Vector2d(frontWheel);
		sum.add(backWheel);
		
		double newOrientation = (Math.atan2(frontWheel.y-backWheel.y,frontWheel.x-backWheel.x));
		
		robot.setPosition(new Vector2d(sum.x/2,sum.y/2));
		robot.setOrientation(newOrientation);
		
	}
	
}
