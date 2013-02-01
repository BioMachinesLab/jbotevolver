package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;

public class ERSimbadForagingEvaluationFunction extends EvaluationFunction {
	private double fitness;
	private Vector2d nestPosition = new Vector2d(0, 0);
	private double FORAGINGAREARADIUS;
	private double NESTRADIUS;

	public ERSimbadForagingEvaluationFunction(Simulator simulator) {
		super(simulator);
	}

	// @Override
	public double getFitness() {
		// double aux=fitness +
		// ((RoundForageEnvironment)(Environment.getInstance())).getNumberOfFoodSuccessfullyForaged()
		// * 1.0;

		return fitness
				+ ((RoundForageEnvironment) (simulator.getEnvironment()))
						.getNumberOfFoodSuccessfullyForaged() * 1000.0;
	}

	// @Override
	public void step() {
		int numberOfRobotsTooCloseToNest = 0;
		int numberOfRobotsTooFarFromNest = 0;
		int numberOfRobotsCloseToAPrey = 0;
		int numberOfRobotsWithPrey = 0;
		double preyDistanceReward = 0.0;

		FORAGINGAREARADIUS = ((RoundForageEnvironment) (simulator
				.getEnvironment())).getForageRadius();
		NESTRADIUS = ((RoundForageEnvironment) (simulator.getEnvironment()))
				.getNestRadius();

		Vector2d coord = new Vector2d();
		for (Robot r : simulator.getEnvironment().getRobots()) {
			coord.set(r.getPosition());

			double distanceToNest = coord.distanceTo(nestPosition);
			if (distanceToNest < NESTRADIUS) {
				numberOfRobotsTooCloseToNest++;
			} else if (distanceToNest > FORAGINGAREARADIUS + 0.5) {
				numberOfRobotsTooFarFromNest++;
			}

			if (getRobotIsCloseToPrey(r)) {
				numberOfRobotsCloseToAPrey++;
			}

			if (r.isCarryingPrey()) {
				numberOfRobotsWithPrey++;
			}
		}

		int foodCounted = 0;
		for (Prey prey : simulator.getEnvironment().getPrey()) {
			double length = prey.getPosition().length();
			if (length > NESTRADIUS) {
				preyDistanceReward += NESTRADIUS / length;
				foodCounted++;
			}
		}
		if (foodCounted > 0)
			preyDistanceReward /= foodCounted;

		fitness += (double) (-numberOfRobotsTooCloseToNest * 0.01
				- numberOfRobotsTooFarFromNest * 0.1
				+ numberOfRobotsCloseToAPrey * 0.1 + numberOfRobotsWithPrey * 0.01)
				+ preyDistanceReward * 0.001;

	}

	public boolean getRobotIsCloseToPrey(Robot robot) {
		CloseObjectIterator iterator = robot.shape.getClosePrey()
				.iterator();

		boolean found = false;

		while (!found && iterator.hasNext()) {
			PhysicalObject obj = iterator.next().getObject();
			if (obj.getPosition().distanceTo(robot.getPosition()) < robot
					.getRadius() + obj.getRadius() + 0.25) {
				found = true;
			}
		}

		return found;
	}
}
