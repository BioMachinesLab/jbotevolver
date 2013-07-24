package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;
import java.util.List;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundRobotForageEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PreyEscapeEvaluationFunction extends EvaluationFunction {

	public PreyEscapeEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		Robot prey = null;
		List<Robot> robots = new ArrayList<Robot>();

		RoundRobotForageEnvironment environment = (RoundRobotForageEnvironment) simulator
				.getEnvironment();

		for (Robot r : simulator.getEnvironment().getRobots()) {
			if (r.getDescription().equals("prey"))
				prey = r;
			else
				robots.add(r);
		}
		
		double distanceToNest  = prey.getPosition().length();
		if (distanceToNest > environment.getForageRadius()) {
			fitness += -3;
		}
		
		for (Robot rb : robots) {
			double distanceToRobot = prey.getPosition().distanceTo(rb.getPosition())- prey.getRadius() - rb.getRadius();

			if ((distanceToRobot ) < 0.05) {
				fitness += -1;
			} else if (distanceToRobot < 0.5) {
				fitness += -((0.5 - distanceToRobot) / 0.5);
			} else if (distanceToRobot >= 2) {
				fitness += 1;
			} else {
				fitness += -((0.5 - distanceToRobot) / 1.5);
			}
		}
	}
}
