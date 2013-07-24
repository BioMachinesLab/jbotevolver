package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.environment.RoundPreyForageEnvironment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PushPreyToNestEvaluationFunction extends EvaluationFunction {

	private int fp;

	public PushPreyToNestEvaluationFunction(Arguments args) {
		super(args);
	}

	public void update(Simulator simulator) {
		Vector2d nestPosition = new Vector2d(0, 0);
		Vector2d robot = new Vector2d();
		RoundForageEnvironment environment = ((RoundForageEnvironment) simulator.getEnvironment());
		Prey prey = null;

		for (Prey p : environment.getPrey()) {
			prey = p;
		}

		double distanceToNest = prey.getPosition().distanceTo(nestPosition);

		fitness += (environment.getForageRadius() - distanceToNest) / environment.getForageRadius() * .0001;

		if (distanceToNest < environment.getNestRadius()) {
			fp += 1;
			prey.teleportTo(new Vector2d(Math.random() * environment.getForageRadius() + environment.getNestRadius(),
					Math.random() * environment.getForageRadius()	+ environment.getNestRadius()));
		}
		for (Robot r : simulator.getEnvironment().getRobots()) {
			robot.set(r.getPosition());
			
			double distanceToPrey = robot.distanceTo(prey.getPosition());

			fitness += (environment.getForageRadius() - distanceToPrey) / environment.getForageRadius() * .00001;
		}
	}

	@Override
	public double getFitness() {
		return super.getFitness() + fp;
	}
}
