package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class StayAtDistanceToNestEvaluationFunction extends EvaluationFunction {

	private Vector2d nestPosition = new Vector2d(0, 0);
	
	@ArgumentsAnnotation(name="distance", defaultValue="1")
	private double distance;
	
	public StayAtDistanceToNestEvaluationFunction(Arguments args) {
		super(args);
		distance = args.getArgumentIsDefined("distance") ? 
				args.getArgumentAsDouble("distance") : 1;
	}

	@Override
	public void update(Simulator simulator) {
		Vector2d coord = new Vector2d();
		for(Robot r : simulator.getEnvironment().getRobots()){
			coord.set(r.getPosition());

			double distanceToNest = coord.distanceTo(nestPosition);
			fitness += 1/(Math.abs(distance - distanceToNest) + .1)*.1; 
		}
	}

}
