package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class StayInTheNestEvaluationFunction extends EvaluationFunction{
	private Vector2d    nestPosition = new Vector2d(0, 0);
	private double      NESTRADIUS;

	public StayInTheNestEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {			
		int numberOfRobotsCloseToNest = 0;

		NESTRADIUS         = ((RoundForageEnvironment)(simulator.getEnvironment())).getNestRadius();

		Vector2d coord = new Vector2d();
		for(Robot r : simulator.getEnvironment().getRobots()){
			coord.set(r.getPosition());

			double distanceToNest = coord.distanceTo(nestPosition);
			if (distanceToNest < NESTRADIUS) {
				numberOfRobotsCloseToNest++;
			} 
		}
		fitness += (double) numberOfRobotsCloseToNest * 0.1;
	}
}