package fourwheeledrobot;

import mathutils.MathUtils;
import mathutils.Vector2d;
import net.jafama.FastMath;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoWheelZeroAxesActuator extends MultipleWheelAxesActuator{
	
	public TwoWheelZeroAxesActuator(Simulator simulator, int id, Arguments args) {
		super(simulator,id,args,2,0);
	}
	
	@Override
	public void apply(Robot robot, double timeDelta) {
		
		Vector2d position = robot.getPosition();
		
		double orientation = robot.getOrientation();
		
		double leftSpeed = speeds[0];
		double rightSpeed = speeds[1];
		
		position.set(
				position.getX() + timeDelta * (leftSpeed + rightSpeed) / 2.0 * FastMath.cosQuick(orientation),
				position.getY() + timeDelta * (leftSpeed + rightSpeed) / 2.0 * FastMath.sinQuick(orientation));
		
		orientation += timeDelta * 0.5/(distanceBetweenWheels/2.0) * (rightSpeed - leftSpeed); 

		orientation = MathUtils.modPI2(orientation);
		robot.setOrientation(orientation);
		robot.setPosition(position);
	}
	
}
