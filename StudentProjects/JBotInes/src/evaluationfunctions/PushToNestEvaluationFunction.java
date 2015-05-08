package evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class PushToNestEvaluationFunction extends EvaluationFunction{



	@ArgumentsAnnotation(name="distance", defaultValue="1")
	private double distance;
	private double preyPercentage;
	private double preyDistance;
	private double totalPreyDistance = 0;
	private Vector2d nestPosition = new Vector2d(0, 0);
	private int numberOfPreyForaged = 0;
	private double previousTotalPreyDistance = 100000000;

	public PushToNestEvaluationFunction(Arguments args) {
		super(args);
		this.preyPercentage = args.getArgumentAsDoubleOrSetDefault("preypercentage", 0.1);
		this.preyDistance = args.getArgumentAsDoubleOrSetDefault("preydistance", 1);

	}

	@Override
	public double getFitness() {
		return fitness + numberOfPreyForaged;
	}

	@Override
	public void update(Simulator simulator) {

		double currentFitness = 0.0;
		double totalPreyDistance = 0.0;

		for(Robot r : simulator.getEnvironment().getRobots()){
			for(Prey p: simulator.getEnvironment().getPrey()){

				totalPreyDistance+= p.getPosition().distanceTo(nestPosition);
				double distance = r.getPosition().distanceTo(p.getPosition());
				
				if(distance < preyDistance){
					currentFitness+= (preyDistance-distance)*preyPercentage;

					//					Vector2d coord = p.getPosition();
					//					double distanceToNest = coord.distanceTo(nestPosition);
					//					totalPreyDistance+= distanceToNest;
					//					currentFitness-= 1/totalPreyDistance;
				}

			}
		}
				
		fitness+= (currentFitness/(double)simulator.getEnvironment().getSteps()) 
				+ ((previousTotalPreyDistance-totalPreyDistance)/ previousTotalPreyDistance);
		previousTotalPreyDistance = totalPreyDistance;
		numberOfPreyForaged = ((RoundForageEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged();
	}
}
