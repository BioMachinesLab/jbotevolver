package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TouchThePreyEvaluationFunction extends EvaluationFunction {
	
	private double distance = 0.2;

	public TouchThePreyEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		Vector2d robot = new Vector2d();
		Prey prey = null;
		
		for (Prey p : simulator.getEnvironment().getPrey()) {
			prey = p;
		}
		for (Robot r : simulator.getEnvironment().getRobots()) {
			robot.set(r.getPosition());

			double distanceToPrey = robot.distanceTo(prey.getPosition());

			if (distanceToPrey - r.getRadius() - prey.getRadius() < distance)
				fitness += 1;
			
			fitness += 1 / distanceToPrey * .00001;
		}
	}
}
