package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleRobotSensor extends SimpleLightTypeSensor {

	public SimpleRobotSensor(Simulator simulator, int id, Robot robot,Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
		
	}
	
}
