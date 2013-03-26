package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class StayAtDistanceToNestEvaluationFunction extends EvaluationFunction {
	private Vector2d nestPosition = new Vector2d(0, 0);
	private double distance;

	public StayAtDistanceToNestEvaluationFunction(Arguments args) {
		super(args);
		distance = args.getArgumentIsDefined("distance") ? args
				.getArgumentAsDouble("distance") : 1;
	}

	@Override
	public void update(Simulator simulator) {
		Vector2d coord = new Vector2d();
		for (Robot r : simulator.getEnvironment().getRobots()) {
			coord.set(r.getPosition());

			//double distanceToNest = coord.distanceTo(nestPosition);
			//fitness += 1 / (Math.abs(distance - distanceToNest) + .1) * .1;
			for(Prey p: simulator.getEnvironment().getPrey()){
				double preyDistance = coord.distanceTo(p.getPosition());
				fitness += 1 / preyDistance * .0001;
			}
			
		}

	}

}