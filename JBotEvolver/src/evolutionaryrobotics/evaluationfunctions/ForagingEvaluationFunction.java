package evolutionaryrobotics.evaluationfunctions;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.robot.Robot;

public class ForagingEvaluationFunction extends EvaluationFunction{
	private double 	    fitness;
	private Vector2d    nestPosition = new Vector2d(0, 0);
	private double forbidenArea;
	private double foragingArea;

	public ForagingEvaluationFunction(Simulator simulator) {
		super(simulator);
		forbidenArea =  ((RoundForageEnvironment)(simulator.getEnvironment())).getForbiddenArea();
		foragingArea =  ((RoundForageEnvironment)(simulator.getEnvironment())).getForageRadius();		
	}

	//@Override
	public double getFitness() {
		return fitness + ((RoundForageEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged() * 1.0;
	}

	//@Override
	public void step() {			
		int numberOfRobotsWithPrey       = 0;
		int numberOfRobotsBeyondForbidenLimit       = 0;
		int numberOfRobotsBeyondForagingLimit       = 0;
		

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
		}

		fitness += (double) numberOfRobotsWithPrey * 0.001 + numberOfRobotsBeyondForbidenLimit * -0.1 + numberOfRobotsBeyondForagingLimit * -0.0001;
	}
}
