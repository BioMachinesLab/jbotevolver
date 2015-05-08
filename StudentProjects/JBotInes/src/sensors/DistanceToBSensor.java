package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;
import checkers.AllowTypeBRobotsChecker;

public class DistanceToBSensor extends LightTypeSensor {

	public DistanceToBSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowTypeBRobotsChecker(robot.getId()));
	}

}
