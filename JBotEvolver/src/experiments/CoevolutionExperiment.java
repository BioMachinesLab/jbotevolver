package experiments;

import java.util.ArrayList;
import java.util.Iterator;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.NestEnvironment;
import simulation.environment.TwoNestForageEnvironment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;

public class CoevolutionExperiment extends Experiment {

	private static final int TEAM1 = 1;
	private static final int TEAM2 = 2;
	public int numberOfrobotsTeamA = 0;
	public int numberOfrobotsTeamB = 0;
	private double[] adversariesLastWeights;

	public CoevolutionExperiment(Simulator simulator,
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotArguments, Arguments controllerArguments) {
		super(simulator, experimentArguments, environmentArguments,
				robotArguments, controllerArguments);

		numberOfrobotsTeamA = numberOfRobots / 2;
		numberOfrobotsTeamB = numberOfRobots / 2;

	}

	@Override
	public ArrayList<Robot> createRobots() {
		robots.clear();
		if (numberOfrobotsTeamA == 0) {
			numberOfrobotsTeamA = numberOfRobots / 2;
			numberOfrobotsTeamB = numberOfRobots / 2;
		}

		for (int i = 0; i < numberOfrobotsTeamA; i++) {
			Robot robot = createOneRobotFromTeam(robotArguments, controllerArguments,TEAM1);
			robots.add(robot);

		}
		for (int i = 0; i < numberOfrobotsTeamB; i++) {
			Robot robot = createOneRobotFromTeam(robotArguments, controllerArguments,TEAM2);
			robots.add(robot);
		}
		return robots;
	}

	private Robot createOneRobotFromTeam(Arguments argumentsRobot, Arguments argumentsControler, int team) {

		Robot robot = robotFactory.getRobotFromTeam(argumentsRobot, team);
		addControllerToRobot(robot,argumentsControler);
		return robot;
	}

	public void setChromosome(Chromosome chromosome, Chromosome adversary) {
		this.adversariesLastWeights = adversary.getAlleles();
		this.lastWeights = chromosome.getAlleles();
		Iterator<Robot> robotIterator = robots.iterator();

		for (int i = 0; i < numberOfrobotsTeamA; i++) {
			Robot r = robotIterator.next();
			if (r.getController() instanceof NeuralNetworkController) {
				NeuralNetworkController nnController = (NeuralNetworkController) r
						.getController();
				nnController.setNNWeights(chromosome.getAlleles());
			}
		}

		for (int i = 0; i < numberOfrobotsTeamB; i++) {
			Robot r = robotIterator.next();
			if (r.getController() instanceof NeuralNetworkController) {
				NeuralNetworkController nnController = (NeuralNetworkController) r
						.getController();
				nnController.setNNWeights(adversary.getAlleles());
			}
		}

	}

	@Override
	protected void placeRobotsUsingPlacement() {
		if (robotArguments.getArgumentAsString("placement").equalsIgnoreCase(
				"nest")) {

			// System.out.println("Placing robots in a rectangle...");
			Iterator<Robot> robotIterator = robots.iterator();

			for (int i = 0; i < numberOfrobotsTeamA; i++) {
				Robot r = robotIterator.next();
				placeSingleRobot(r, ((TwoNestForageEnvironment) simulator
						.getEnvironment()).getNestAPosition());
			}
			for (int i = 0; i < numberOfrobotsTeamB; i++) {
				Robot r = robotIterator.next();
				placeSingleRobot(r, ((TwoNestForageEnvironment) simulator
						.getEnvironment()).getNestBPosition());
			}
		} else {
			super.placeRobotsUsingPlacement();
		}
	}

	private void placeSingleRobot(Robot r, Vector2d nest) {
		boolean tooCloseToSomeOtherRobot = false;
		double maxRadius = ((NestEnvironment) (simulator
				.getEnvironment())).getNestRadius();
		do {
			tooCloseToSomeOtherRobot = false;
			double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
			double radius = simulator.getRandom().nextDouble() * maxRadius;
			double positionX = nest.getX() + Math.cos(angle) * radius;
			double positionY = nest.getY() + Math.sin(angle) * radius;

			r.setPosition(new Vector2d(positionX, positionY));
			Robot closestRobot = getRobotClosestTo(r);
			if (closestRobot != null) {
				if (r.getPosition().distanceTo(closestRobot.getPosition()) < 0.05)
					tooCloseToSomeOtherRobot = true;
			}
			maxRadius += .05;
		} while (tooCloseToSomeOtherRobot);
		r.teleportTo(r.getPosition());

	}
//
//	@Override
//	public void placeRobots() {
//		for (Robot r : robots) {
//			r.setPosition(new Vector2d(0, -10));
//		}
//
//		Iterator<Robot> r = robots.iterator();
//		Vector2d nestAPosition = ((TwoNestForageEnvironment) simulator
//				.getEnvironment()).getNestAPosition();
//		for (int i = 0; i < numberOfrobotsTeamA; i++) {
//			r.next().setPosition(
//					new Vector2d(nestAPosition.getX(), nestAPosition.getY()));
//			// r.next().teleportTo(new
//			// Vector2d(nestAPosition.getX(),nestAPosition.getY()));
//		}
//
//		Vector2d nestBPosition = ((TwoNestForageEnvironment) simulator
//				.getEnvironment()).getNestBPosition();
//		for (int i = 0; i < numberOfrobotsTeamB; i++) {
//			r.next().setPosition(
//					new Vector2d(nestBPosition.getX(), nestBPosition.getY()));
//			// r.next().teleportTo(new
//			// Vector2d(nestBPosition.getX(),nestBPosition.getY()));
//		}
//	}

}
