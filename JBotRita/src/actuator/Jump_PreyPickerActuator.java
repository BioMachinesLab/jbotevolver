package actuator;

import mathutils.Vector2d;
import net.jafama.FastMath;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Prey;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;

public class Jump_PreyPickerActuator extends PreyPickerActuator {

	/**
	 * PreyPickerActuator for a JumpingRobot. The robot cannot pick the prey while jumping.
	 */
	public Jump_PreyPickerActuator(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		// TODO Auto-generated constructor stub
	}


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
