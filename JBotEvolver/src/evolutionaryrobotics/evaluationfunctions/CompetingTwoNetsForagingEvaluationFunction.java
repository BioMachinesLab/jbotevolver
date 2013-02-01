package evolutionaryrobotics.evaluationfunctions;

import java.util.ArrayList;

import experiments.CoevolutionExperiment;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.RoundForageEnvironment;
import simulation.environment.TwoNestForageEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class CompetingTwoNetsForagingEvaluationFunction extends
		EvaluationFunction {
	private double fitness;
	private Vector2d nestAPosition;
	// private Vector2d nestBPosition = new Vector2d(0, 0);
	private double forbidenArea;
	private double foragingArea;
	private TwoNestForageEnvironment env;
	private int numberOfRobotInTeam;

	public CompetingTwoNetsForagingEvaluationFunction(Simulator simulator,
			Arguments arguments) {
		super(simulator);
		env = ((TwoNestForageEnvironment) (simulator.getEnvironment()));
		forbidenArea = env.getForbiddenArea();
		foragingArea = env.getForageRadius();
		nestAPosition = env.getNestAPosition();
		numberOfRobotInTeam = ((CoevolutionExperiment) simulator
				.getExperiment()).numberOfrobotsTeamA;
	}

	// @Override
	public double getFitness() {
		TwoNestForageEnvironment environment = ((TwoNestForageEnvironment) (simulator
				.getEnvironment()));
		return fitness
				+ ((double) environment
						.getNumberOfFoodSuccessfullyForagedNestA());
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
			double distanceToNest = r.getPosition().distanceTo(nestAPosition);

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
