package simulation.environment;

import gui.renderer.Renderer;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;

public class TwoNestForageEnvironment extends Environment implements
		NestEnvironment {

	protected static final double PREY_RADIUS = 0.025;
	protected static final double PREY_MASS = 1;
	protected double nestLimit;
	protected double forageLimit;
	protected double forbiddenArea;
	protected int amountOfFood;
	protected Nest nestA;
	protected Nest nestB;
	protected int numberOfFoodSuccessfullyForagedNestA = 0;
	protected int numberOfFoodSuccessfullyForagedNestB = 0;
	protected double nestDistance;
	protected Vector2d center = new Vector2d(0, 0);

	// public TwoNestForageEnvironment(Simulator simulator, double width,
	// double height) {
	// super(simulator, width, height);
	// }

	public TwoNestForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator,
				arguments.getArgumentIsDefined("forbiddenarea") ? arguments
						.getArgumentAsDouble("forbiddenarea") : 5.0, arguments
						.getArgumentIsDefined("forbiddenarea") ? arguments
						.getArgumentAsDouble("forbiddenarea") : 5.0);

		nestDistance = arguments.getArgumentIsDefined("nestdistance") ? arguments
				.getArgumentAsDouble("nestdistance") : 2;
		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments
				.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments
				.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments
				.getArgumentAsDouble("forbiddenarea") : 5.0;

		if (arguments.getArgumentIsDefined("densityoffood")) {
			double densityoffood = arguments
					.getArgumentAsDouble("densityoffood");
			amountOfFood = (int) (densityoffood * Math.PI * forageLimit
					* forageLimit + .5);
		} else {
			amountOfFood = arguments.getArgumentIsDefined("amountfood") ? arguments
					.getArgumentAsInt("amountfood") : 20;
		}

		nestA = new Nest(simulator, "NestA", -nestDistance / 2, 0, nestLimit);
		nestA.setParameter("TEAM", 1);
		nestB = new Nest(simulator, "NestB", nestDistance / 2, 0, nestLimit);
		nestB.setParameter("TEAM", 2);
		addObject(nestA);
		addObject(nestB);

		deployPreys();
	}

	protected void deployPreys() {
		for (int i = 0; i < getAmoutOfFood(); i++) {
			addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(), 0,
					PREY_MASS, PREY_RADIUS));
		}

	}

	protected int getAmoutOfFood() {
		return amountOfFood;
	}

	protected Vector2d newRandomPosition() {
		Vector2d position;
		do {
			double radius = simulator.getRandom().nextDouble() * (forageLimit);
			double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
			position = new Vector2d(radius * Math.cos(angle), radius
					* Math.sin(angle));
		} while (position.distanceTo(nestA.getPosition()) < nestA.getRadius()
				|| position.distanceTo(nestB.getPosition()) < nestB.getRadius());
		return position;
	}

	@Override
	public void update(double time) {
		nestA.shape.getClosePrey().update(time, teleported);
		nestB.shape.getClosePrey().update(time, teleported);
		dropPreysDueToCollison();
		numberOfFoodSuccessfullyForagedNestA += calculateNewForagePrey(nestA);
		numberOfFoodSuccessfullyForagedNestB += calculateNewForagePrey(nestB);

		if (numberOfFoodSuccessfullyForagedNestB
				+ numberOfFoodSuccessfullyForagedNestA >= amountOfFood) {
			simulator.getExperiment().endExperiment();
		}
	}

	protected void dropPreysDueToCollison() {
		for (Robot robot : robots) {
			if (robot.isCarryingPrey() && robot.isInvolvedInCollison()) {
				Prey preyToDrop = robot.dropPrey();
				preyToDrop.teleportTo(newRandomPosition());
			}
		}
	}

	protected int calculateNewForagePrey(Nest nest) {
		CloseObjectIterator i = nest.shape.getClosePrey().iterator();
		int numberOfFoodSuccessfullyForaged = 0;
		Vector2d nestPosition = nest.getPosition();
		while (i.hasNext()) {
			PhysicalObjectDistance preyDistance = i.next();
			Prey nextPrey = (Prey) (preyDistance.getObject());
			double distance = nextPrey.getPosition().distanceTo(nestPosition);
			if (nextPrey.isEnabled() && distance < nestLimit) {
				if (distance == 0) {
					System.out.println("ERRO--- zero");
				}
				// Fixed number of prey
				// nextPrey.teleportTo(newRandomPosition());
				numberOfFoodSuccessfullyForaged++;
				nextPrey.setEnabled(false);
			}
			i.updateCurrentDistance(distance);
		}
		return numberOfFoodSuccessfullyForaged;
	}

	public int getNumberOfFoodSuccessfullyForagedNestA() {
		return numberOfFoodSuccessfullyForagedNestA;
	}

	public int getNumberOfFoodSuccessfullyForagedNestB() {
		return numberOfFoodSuccessfullyForagedNestB;
	}

	public double getNestRadius() {
		return nestLimit;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

	@Override
	public void draw(Renderer renderer) {
		renderer.drawCircle(center, forageLimit);
		renderer.drawCircle(center, forbiddenArea);
	}

	public Vector2d getNestAPosition() {
		return nestA.getPosition();
	}

	public Vector2d getNestBPosition() {
		return nestB.getPosition();
	}
}
