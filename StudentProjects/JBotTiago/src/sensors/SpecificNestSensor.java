package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import checkers.AllowSpecificNestChecker;

public class SpecificNestSensor extends LightTypeSensor {

	@ArgumentsAnnotation(name="specificnest", help="Name of the nest that the robots will be able to sense.", defaultValue="N/A")
	String specificNest;
	
	public SpecificNestSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		specificNest = args.getArgumentAsStringOrSetDefault("specificnest", "");
		setAllowedObjectsChecker(new AllowSpecificNestChecker(specificNest));
	}

}