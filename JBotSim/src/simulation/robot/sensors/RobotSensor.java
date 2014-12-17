package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class RobotSensor extends LightTypeSensor {

	public RobotSensor(Simulator simulator, int id, Robot robot,Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
		
	}
	
}
