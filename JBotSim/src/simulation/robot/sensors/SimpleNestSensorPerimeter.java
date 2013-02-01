package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowNestChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleNestSensorPerimeter extends PerimeterSimpleLightTypeSensor {

	public SimpleNestSensorPerimeter(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowNestChecker());
	}
}