package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;

public class StayInTheNestEvaluationFunction extends EvaluationFunction{
	private double 	    fitness;
	private Vector2d    nestPosition = new Vector2d(0, 0);
	private double      NESTRADIUS;

	public StayInTheNestEvaluationFunction(Simulator simulator) {
		super(simulator);
	}

	public double getFitness() {
		return fitness;
	}

	//@Override
	public void update(double time) {			
		int numberOfRobotsCloseToNest = 0;
		double preyDistanceReward        = 0.0;

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
