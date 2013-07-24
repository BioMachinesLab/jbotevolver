package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;
import java.util.List;

import simulation.Simulator;
import simulation.environment.RoundRobotForageEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PreyCatchEvaluationFunction extends EvaluationFunction {

	public PreyCatchEvaluationFunction(Arguments args) {
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
		
		for (Robot rb : robots) {
			double distanceToPrey = rb.getPosition().distanceTo(prey.getPosition())- prey.getRadius() - rb.getRadius();
			double distanceRobotToNest = rb.getPosition().length();
			if (distanceRobotToNest > environment.getForageRadius()) {
				fitness += -3;
			}
			if (distanceToPrey < 0.05) {
				fitness += 1;
			} else if (distanceToPrey < 0.5) {
				fitness += ((0.5 - distanceToPrey) / 0.5);
			} else if (distanceToPrey >= 2) {
				fitness += -1;
			} else {
				fitness += ((0.5 - distanceToPrey) / 1.5 );
			}
		}
	
	}
}
