package actuator;

import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;


/**
 * PreyPickerActuator for a JumpingRobot. The robot cannot pick the prey while jumping.
 * @author Rita Ramos
 */

public class JumpPreyPickerActuator extends PreyPickerActuator {

	public JumpPreyPickerActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
	}


	/**
	 * Check if it is a jumping robot; if so only pick the prey if not jumping 
	 * (when the robot is jumping, it is ignored the wall collisions; 
	 * thus only pick it when it is not to ignore them)
	 */
	@Override
	public void apply(Robot robot,double timeDelta) {
		if(robot instanceof JumpingRobot){
			if(!((JumpingRobot)robot).ignoreWallCollisions())  
				super.apply(robot, timeDelta);
		}else{
			super.apply(robot, timeDelta);
		}
	}
	
}
