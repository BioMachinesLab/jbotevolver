package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimplePreySensor extends SimpleLightTypeSensor {

	public SimplePreySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowOrderedPreyChecker(robot.getId()));
	}
}