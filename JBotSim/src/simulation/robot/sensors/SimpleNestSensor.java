package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowNestChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleNestSensor extends SimpleLightTypeSensor {

	public SimpleNestSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowNestChecker());
	}

}
