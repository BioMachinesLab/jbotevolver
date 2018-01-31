package actuator;


import robots.JumpingSumo;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 * JumpingSumoActuator is similar to JumpActuator, 
 * but the angle and power is defined in the JumpingSumoRobot itself,
 * according to the model of that robot
 * @author Rita Ramos
 */

public class JumpSumoActuator extends JumpActuator{

	public JumpSumoActuator(Simulator simulator, int id, Arguments args) {
		super(simulator, id, args);
	}
			
	@Override
	public void apply(Robot robot,double timeDelta) {
		if(isToJump){
			((JumpingSumo) robot).jump();
			isToJump=false;
		}
	}

}