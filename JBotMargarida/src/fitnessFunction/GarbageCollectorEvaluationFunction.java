package fitnessFunction;

import environment.GarbageCollectorEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import actuators.IntensityPreyPickerActuator;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GarbageCollectorEvaluationFunction extends EvaluationFunction {
	private Vector2d temp;
	private GarbageCollectorEnvironment environment;
	private int numberOfGarbage;

	public GarbageCollectorEvaluationFunction(Arguments args) {
		super(args);

	}

	// @Override
	public double getFitness() {
		// System.out.println("total " + (fitness + numberOfGarbage));

		return fitness + numberOfGarbage;
	}

	// @Override
	public void update(Simulator simulator) {
		environment = (GarbageCollectorEnvironment) simulator.getEnvironment();

		for (Robot r : environment.getRobots()) {

			IntensityPreyPickerActuator actuator = (IntensityPreyPickerActuator) r
					.getActuatorByType(IntensityPreyPickerActuator.class);
			double bestDistance = Double.MAX_VALUE;
			for (Prey prey : environment.getPrey()) {
				double distance = prey.getPosition()
						.distanceTo(r.getPosition());
				if (distance < bestDistance)
					bestDistance = distance;

			}

			if (bestDistance < environment.getForageRadius()
					&& !actuator.isCarryingPrey()) {
				fitness += (environment.getForageRadius() - bestDistance)
						/ environment.getForageRadius() * 0.000001;
				// System.out.println((environment.getForageRadius() -
				// bestDistance)
				// / environment.getForageRadius() * 0.000001);
			}
			if (actuator.isCarryingPrey()) {
				double distanceToNest = r.getPosition().distanceTo(
						environment.getNest().getPosition());
				fitness += (environment.getForageRadius() - distanceToNest)
						/ environment.getForageRadius() * 0.00001;
				// System.out.println((environment.getForageRadius() -
				// distanceToNest)
				// / environment.getForageRadius() * 0.00001);
			}

		}
		numberOfGarbage = environment.getNumberOfGarbageSuccessfullyForaged();

	}
}