package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.environment.GroupedPreyEnvironment;
import simulation.robot.Robot;

public class CommunicationEvaluationFunction extends EvaluationFunction{
	private double 	    fitness;
	private Vector2d    nestPosition = new Vector2d(0, 0);
	private double		forbidenArea;
	private double		foragingArea;

	public CommunicationEvaluationFunction(Simulator simulator) {
		super(simulator);
		forbidenArea =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForbiddenArea();
		foragingArea =  ((GroupedPreyEnvironment)(simulator.getEnvironment())).getForageRadius();		
	}

	//@Override
	public double getFitness() {
		return fitness + ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() * 1.0;
	}
	
	private boolean haveForaged(){
		return ((GroupedPreyEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() > 0;
	}

	//@Override
	public void step() {			
		int numberOfRobotsWithPrey       = 0;
		int numberOfRobotsBeyondForbidenLimit       = 0;
		int numberOfRobotsBeyondForagingLimit       = 0;
		
		double collectiveSpeed = 0;

		for(Robot r : simulator.getEnvironment().getRobots()){
			double distanceToNest = r.getPosition().distanceTo(nestPosition);
		
			if(distanceToNest > forbidenArea){
				numberOfRobotsBeyondForbidenLimit++;
			} else 	if(distanceToNest > foragingArea){
				numberOfRobotsBeyondForagingLimit++;
			}
			
			if (r.isCarryingPrey()) {
				numberOfRobotsWithPrey++;
			}
			
			if(haveForaged())
				collectiveSpeed+= (Math.abs(r.getLeftWheelSpeed())+Math.abs(r.getRightWheelSpeed()))/2;
			
		}

		fitness += (double) numberOfRobotsWithPrey * 0.001 + 
			numberOfRobotsBeyondForbidenLimit * -0.1 + 
			numberOfRobotsBeyondForagingLimit * -0.0001 +
			collectiveSpeed * -0.0001;
	}
}
