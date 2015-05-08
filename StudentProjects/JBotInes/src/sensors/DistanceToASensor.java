package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;
import checkers.AllowTypeARobotsChecker;

public class DistanceToASensor extends LightTypeSensor {

	public DistanceToASensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowTypeARobotsChecker(robot.getId()));
	}

}
