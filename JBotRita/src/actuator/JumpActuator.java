package actuator;

import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

/**
 * JumpActuator allows the robot to jump given a angle and power
 * @author Rita Ramos
 */

public class JumpActuator extends Actuator{
		
	protected boolean isToJump=false;

	@ArgumentsAnnotation(name="angle", defaultValue = "80.0")
	protected double angle;
	
	@ArgumentsAnnotation(name="power", defaultValue = "4.0")
	protected double power;
	
	
	public JumpActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
		this.angle = args.getArgumentAsDoubleOrSetDefault("angle", 80.0);
		this.power = args.getArgumentAsDoubleOrSetDefault("power", 4.0);
	}

	@Override
	public void apply(Robot robot,double timeDelta) {
		if(isToJump){
			((JumpingRobot) robot).jump(angle,power);
			isToJump=false;
		}		
	}

	public void jump( ) {
		isToJump=true;
	}
	
}
