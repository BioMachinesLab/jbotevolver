package experiments;

import java.util.ArrayList;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.actuators.RobotRGBColorActuator;
import simulation.util.Arguments;

public class ColorMatchExperiment extends Experiment {
	protected int numberOfNonMovingRobots;
	protected double[] colorToMatch;

	public ColorMatchExperiment(Simulator simulator,
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotArguments, Arguments controllerArguments) {

		super(simulator, experimentArguments, environmentArguments,
				robotArguments, controllerArguments);

	}

	public ArrayList<Robot> createRobots() {
		ArrayList<Robot> evolvingRobots = super.createRobots();
		numberOfNonMovingRobots = 1;
		if (experimentArguments != null) {
			if (experimentArguments.getArgumentIsDefined("nonmovingrobots")) {
				numberOfNonMovingRobots = experimentArguments
						.getArgumentAsInt("nonmovingrobots");
			}
		}

		robots = new ArrayList<Robot>();
		colorToMatch = new double[3];
		colorToMatch[0] = simulator.getRandom().nextDouble();
		colorToMatch[1] = simulator.getRandom().nextDouble();
		colorToMatch[2] = simulator.getRandom().nextDouble();

		for (int i = 0; i < numberOfNonMovingRobots; i++)
			robots.add(createNonMovingRobot(robotArguments));

		robots.addAll(evolvingRobots);

		return robots;
	}

	private Robot createNonMovingRobot(Arguments robotArguments) {
		Robot robot = robotFactory.getRobot(robotArguments);
		robot.setBodyColor(colorToMatch);
		((RobotRGBColorActuator) robot.getActuatorWithId(2))
				.setAll(colorToMatch);
		return robot;
	}
}
