package sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;

public class RobotDistanceSensor extends LightTypeSensor {
	
	public RobotDistanceSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker( new AllowAllRobotsChecker(robot.getId()));
	}
	
}