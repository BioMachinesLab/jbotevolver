package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowTeamNestChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleTeamNestSensor extends SimpleLightTypeSensor {

	public SimpleTeamNestSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		String nest = (args.getArgumentIsDefined("nest")) ? args.getArgumentAsString("nest") : "self";
		int nestNumber = robot.getParameterAsInteger("TEAM");
		if (nest.equals("enemy"))
			nestNumber = (nestNumber % 2) + 1;
		setAllowedObjectsChecker(new AllowTeamNestChecker(nestNumber));
	}
}