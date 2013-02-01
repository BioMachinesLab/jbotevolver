package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;

import experiments.CoevolutionExperiment;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.environment.TwoNestForageEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class CollaboratingTwoNetsForagingEvaluationFunction extends
		EvaluationFunction {
	private double fitness;
	private Vector2d nestAPosition;
	private Vector2d nestBPosition;
	private double forbidenArea;
	private double foragingArea;
	private TwoNestForageEnvironment env;
	private int numberOfRobotInTeam;

	public CollaboratingTwoNetsForagingEvaluationFunction(Simulator simulator,
			Arguments arguments) {
		super(simulator);
		env = ((TwoNestForageEnvironment) (simulator.getEnvironment()));
		forbidenArea = env.getForbiddenArea();
		foragingArea = env.getForageRadius();
		nestAPosition = env.getNestAPosition();
		nestBPosition = env.getNestBPosition();
		numberOfRobotInTeam = ((CoevolutionExperiment) simulator
				.getExperiment()).numberOfrobotsTeamA
				+ ((CoevolutionExperiment) simulator.getExperiment()).numberOfrobotsTeamB;
	}

	// @Override
	public double getFitness() {
		TwoNestForageEnvironment environment = ((TwoNestForageEnvironment) (simulator
				.getEnvironment()));
		return fitness
				+ ((double) environment
						.getNumberOfFoodSuccessfullyForagedNestA())
				+ ((double) environment
						.getNumberOfFoodSuccessfullyForagedNestB());
		// return fitness +
		// ((RoundForageEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged()
		// * 1.0;
	}

	// @Override
	public void update(double time) {
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

			if (r.isCarryingPrey()) {
				numberOfRobotsWithPrey++;
			}
		}

		fitness += (double) numberOfRobotsWithPrey * 0.001
				+ numberOfRobotsBeyondForbidenLimit * -0.1
				+ numberOfRobotsBeyondForagingLimit * -0.0001;
	}
}
