package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowMouseChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MouseSensor extends LightTypeSensor {

	public MouseSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowMouseChecker(id));
	}

}

