package actuator;

import java.util.Random;

import robots.JumpingRobot;
import robots.JumpingRobot;
import robots.JumpingSumo;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpActuator extends Actuator{
		
	private boolean isToJump=false;

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
		// TODO Auto-generated method stub
		if(isToJump){
			((JumpingRobot) robot).jump(angle,power);
			isToJump=false;
		}
		
	}

	public void jump( ) {
		isToJump=true;
	}
	
}
