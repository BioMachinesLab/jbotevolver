package fourwheeledrobot;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class FourWheelZeroAxesActuator extends MultipleWheelAxesActuator{
	
	/**
	 * Diagram of the robot (north pointing forward) and the positions of the 4 wheels
	 * 
	 * (forward)
	 *     ^
	 *  0.___.1
	 *    | |
	 *  2.|_|.3
	 */
	
	public FourWheelZeroAxesActuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,4,0);
		
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		
		Vector2d position = robot.getPosition();
		
		double orientation = robot.getOrientation();
		
		double leftSpeed = (speeds[0]+speeds[2])/2.0;
		double rightSpeed = (speeds[1]+speeds[3])/2.0;
		
		position.set(
				position.getX() + timeDelta * (leftSpeed + rightSpeed) / 2.0 * FastMath.cosQuick(orientation),
				position.getY() + timeDelta * (leftSpeed + rightSpeed) / 2.0 * FastMath.sinQuick(orientation));
		
		orientation += timeDelta * 0.5/(distanceBetweenWheels/2.0) * (rightSpeed - leftSpeed); 

		orientation = MathUtils.modPI2(orientation);
		robot.setOrientation(orientation);
		robot.setPosition(position);
	}
	
}
