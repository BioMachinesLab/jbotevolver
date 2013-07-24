package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class StayCloserToPreyEvaluationFunction extends EvaluationFunction {
	
	private double distance;

	public StayCloserToPreyEvaluationFunction(Arguments args) {
		super(args);
		distance = args.getArgumentIsDefined("distance") ? 
				args.getArgumentAsDouble("distance") : 0.2;
	}

	@Override
	public void update(Simulator simulator) {
		Vector2d coord = new Vector2d();
		Prey prey = null;
		
		for (Prey p : simulator.getEnvironment().getPrey()) {
			prey = p;
		}
		
		for(Robot r : simulator.getEnvironment().getRobots()){
			coord.set(r.getPosition());
			

			double distanceToPrey = coord.distanceTo(prey.getPosition());
			fitness += 1/(Math.abs(distance - distanceToPrey))*.1; 
		}
	}

}
