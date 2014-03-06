package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowMouseChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleMouseSensor extends SimpleLightTypeSensor {

	public SimpleMouseSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowMouseChecker(id));
	}

}

