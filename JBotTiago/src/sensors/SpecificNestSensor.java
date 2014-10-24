package sensors;

import checkers.AllowSpecificNestChecker;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.SimpleLightTypeSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class SpecificNestSensor extends SimpleLightTypeSensor {

	@ArgumentsAnnotation(name="specificnest", help="Name of the nest that the robots will be able to sense.", defaultValue="N/A")
	String specificNest;
	
	public SpecificNestSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		specificNest = args.getArgumentAsStringOrSetDefault("specificnest", "");
		setAllowedObjectsChecker(new AllowSpecificNestChecker(specificNest));
	}

}