package evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ForagingEvaluationFunction2 extends EvaluationFunction{

	@ArgumentsAnnotation(name="distance", defaultValue="1")
	private double distance;
	private double preyPercentage;
	private double preyDistance;
	private double totalPreyDistance = 0;
	private Vector2d nestPosition = new Vector2d(0, 0);
	private int numberOfPreyForaged = 0;
	private double previousTotalPreyDistance = 10;
	private double previousDistanceToNest = 0;
	
	public ForagingEvaluationFunction2(Arguments args) {
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
		int numberOfRobotsWithPrey = 0;
		double currentFitness = 0.0;
		double totalPreyDistance = 0.0;
		double currentDistanceToNest = 0.0;

		for(Robot r : simulator.getEnvironment().getRobots()){
			currentDistanceToNest = r.getPosition().distanceTo(nestPosition);
			
			for(Prey p: simulator.getEnvironment().getPrey()){

				totalPreyDistance+= p.getPosition().distanceTo(nestPosition);
				double distance = r.getPosition().distanceTo(p.getPosition());
								
				if (!((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()) {
					if(distance < preyDistance)
						currentFitness+= (preyDistance-distance)* 0.00001;
					else
						currentFitness-= distance*0.000001;
				}
			}
			
			if (((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()) {
				numberOfRobotsWithPrey++;
				
				currentFitness -= 0.0001 * currentDistanceToNest;
				//currentDistanceToNest = r.getPosition().distanceTo(nestPosition);
				//currentFitness+= (previousDistanceToNest-currentDistanceToNest)/previousDistanceToNest;				
			}
		}
				
		fitness+= currentFitness + ((previousTotalPreyDistance-totalPreyDistance)/ previousTotalPreyDistance)*0.0001
				+ (double) numberOfRobotsWithPrey * 0.0002;
		previousTotalPreyDistance = totalPreyDistance;
		//previousDistanceToNest = currentDistanceToNest;
		numberOfPreyForaged = ((RoundForageEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged();
		
	}

}
