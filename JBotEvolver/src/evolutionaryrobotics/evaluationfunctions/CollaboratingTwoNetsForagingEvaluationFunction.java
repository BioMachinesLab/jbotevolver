package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.TwoNestForageEnvironment;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class CollaboratingTwoNetsForagingEvaluationFunction extends EvaluationFunction {
	private int numberOfRobotInTeam;
	private int foodNestA = 0;
	private int foodNestB = 0;

	public CollaboratingTwoNetsForagingEvaluationFunction(Arguments arguments) {
		super(arguments);

		//TODO I commented this: (Miguel)
//		numberOfRobotInTeam = ((CoevolutionExperiment) simulator
//				.getExperiment()).numberOfrobotsTeamA
//				+ ((CoevolutionExperiment) simulator.getExperiment()).numberOfrobotsTeamB;
	}

	// @Override
	public double getFitness() {

		return fitness + foodNestA + foodNestB;
		//TODO I commented this: (Miguel)
		// return fitness +
		// ((RoundForageEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged()
		// * 1.0;
	}

	// @Override
	public void update(Simulator simulator) {
		TwoNestForageEnvironment env = ((TwoNestForageEnvironment) (simulator.getEnvironment()));
		double forbidenArea = env.getForbiddenArea();
		double foragingArea = env.getForageRadius();
		Vector2d nestAPosition = env.getNestAPosition();
		Vector2d nestBPosition = env.getNestBPosition();
		int numberOfRobotsWithPrey = 0;
		int numberOfRobotsBeyondForbidenLimit = 0;
		int numberOfRobotsBeyondForagingLimit = 0;

		ArrayList<Robot> robots = simulator.getEnvironment().getRobots();
		for (int i = 0; i < numberOfRobotInTeam; i++) {
			Robot r = robots.get(i);
			// for(Robot r : simulator.getEnvironment().getRobots()){
			double distanceToNest = Math.min(
					r.getPosition().distanceTo(nestAPosition), r.getPosition()
							.distanceTo(nestBPosition));

			if (distanceToNest > forbidenArea) {
				numberOfRobotsBeyondForbidenLimit++;
			} else if (distanceToNest > foragingArea) {
				numberOfRobotsBeyondForagingLimit++;
			}

			if (((PreyCarriedSensor)r.getSensorByType(PreyCarriedSensor.class)).preyCarried()) {
				numberOfRobotsWithPrey++;
			}
		}

		fitness += (double) numberOfRobotsWithPrey * 0.001
				+ numberOfRobotsBeyondForbidenLimit * -0.1
				+ numberOfRobotsBeyondForagingLimit * -0.0001;
		
		this.foodNestA = env.getNumberOfFoodSuccessfullyForagedNestA();
		this.foodNestB = env.getNumberOfFoodSuccessfullyForagedNestB();
	}
}