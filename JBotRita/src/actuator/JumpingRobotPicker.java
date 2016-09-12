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

public class JumpingRobotPicker extends PreyPickerActuator {

	public JumpingRobotPicker(Simulator simulator, int id, Arguments arguments) {
		super(simulator, id, arguments);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void apply(Robot robot) {
		if(robot instanceof JumpingRobot){
			if(!((JumpingRobot)robot).isJumping())
				super.apply(robot);
		}else{
			super.apply(robot);
		}
	}
	
}
