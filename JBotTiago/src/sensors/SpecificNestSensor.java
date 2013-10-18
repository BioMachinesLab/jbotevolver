package sensors;

import checkers.AllowSpecificNestChecker;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.SimpleLightTypeSensor;
import simulation.util.Arguments;

public class SpecificNestSensor extends SimpleLightTypeSensor {

	String specificNest;
	
	public SpecificNestSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		specificNest = args.getArgumentAsStringOrSetDefault("specificnest", "");
		setAllowedObjectsChecker(new AllowSpecificNestChecker(specificNest));
	}

}