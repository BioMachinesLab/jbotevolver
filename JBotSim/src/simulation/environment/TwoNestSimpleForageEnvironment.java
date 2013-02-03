package simulation.environment;

import gui.renderer.Renderer;
import java.awt.Point;
import java.util.LinkedList;
import mathutils.Point2d;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TwoNestSimpleForageEnvironment extends TwoNestForageEnvironment {

	// private static final double PREY_RADIUS = 0.025;
	// private static final double PREY_MASS = 1;
	// private double nestLimit;
	// private double forageLimit;
	// private double forbiddenArea;
	// private int amountOfFood;
	// private Nest nestA;
	// private Nest nestB;
	// private int numberOfFoodSuccessfullyForagedNestA = 0;
	// private int numberOfFoodSuccessfullyForagedNestB = 0;
	// private double nestDistance;
	// private Vector2d center = new Vector2d(0, 0);

	// public TwoNestSimpleForageEnvironment(Simulator simulator, double width,
	// double height) {
	// super(simulator, width, height);
	// }

	public TwoNestSimpleForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		// nestDistance = arguments.getArgumentIsDefined("nestdistance") ?
		// arguments
		// .getArgumentAsDouble("nestdistance") : 2;
		// nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments
		// .getArgumentAsDouble("nestlimit") : .5;
		// forageLimit = arguments.getArgumentIsDefined("foragelimit") ?
		// arguments
		// .getArgumentAsDouble("foragelimit") : 2.0;
		// forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ?
		// arguments
		// .getArgumentAsDouble("forbiddenarea") : 5.0;
		//
		// if (arguments.getArgumentIsDefined("densityoffood")) {
		// double densityoffood = arguments
		// .getArgumentAsDouble("densityoffood");
		// amountOfFood = (int) (densityoffood * Math.PI * forageLimit
		// * forageLimit + .5);
		// } else {
		// amountOfFood = arguments.getArgumentIsDefined("amountfood") ?
		// arguments
		// .getArgumentAsInt("amountfood") : 20;
		// }
		//
	}
	
	@Override
	public void setup(Simulator simulator) {
		// nestA = new Nest(simulator, "NestA", -nestDistance / 2, 0,
		// nestLimit);
		// nestA.setParameter("TEAM", 1);
		// nestB = new Nest(simulator, "NestB", nestDistance / 2, 0, nestLimit);
		// nestB.setParameter("TEAM", 2);
		// addObject(nestA);
		// addObject(nestB);
		//
		// for (int i = 0; i < amountOfFood; i++) {
		// addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(), 0,
		// PREY_MASS, PREY_RADIUS));
		// }	
	}

	// private Vector2d newRandomPosition() {
	// Vector2d position;
	// do {
	// double radius = simulator.getRandom().nextDouble() * (forageLimit);
	// double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
	// position = new Vector2d(radius * Math.cos(angle), radius
	// * Math.sin(angle));
	// } while (position.distanceTo(nestA.getPosition()) < nestA.getRadius()
	// || position.distanceTo(nestB.getPosition()) < nestB.getRadius());
	// return position;
	// }

	
//	@Override
//	protected void dropPreysDueToCollison() {
//		for (Robot robot : robots) {
//			if (robot.isCarryingPrey() && robot.isInvolvedInCollison()) {
//				LinkedList<Prey> preysToDrop = ((MultiPreyForagerRobot) robot)
//						.dropPreys();
//				for (Prey prey : preysToDrop) {
//					double offset = robot.getRadius() + prey.getRadius();
//					double angle = simulator.getRandom().nextDouble() * 2 * Math.PI;
//					Vector2d newPosition = new Vector2d(robot.getPosition()
//							.getX()
//							+ offset
//							* Math.cos(robot.getOrientation() + angle), robot
//							.getPosition().getY()
//							+ offset
//							* Math.sin(robot.getOrientation() + angle));
//					prey.teleportTo(newPosition);
//
//					// prey.setPosition(new
//					// Vector2d(robot.getPosition().getX(),robot.getPosition().getY()));
//					((MultiPreyForagerRobot) robot).stopRobotDueToColision();
//				}
//			} else {
//				((MultiPreyForagerRobot) robot).updatePreyPosition();
//			}
//		}
//	}
//
//	@Override
//	protected int calculateNewForagePrey(Nest nest) {
//		CloseObjectIterator i = nest.shape.getClosePrey().iterator();
//		int numberOfFoodSuccessfullyForaged = 0;
//		Vector2d nestPosition = nest.getPosition();
//		while (i.hasNext()) {
//			PhysicalObjectDistance preyDistance = i.next();
//			Prey nextPrey = (Prey) (preyDistance.getObject());
//			double distance = nextPrey.getPosition().distanceTo(nestPosition);
//			if ((nextPrey.isEnabled() || nextPrey.getHolder() != null)
//					&& distance+nextPrey.getDiameter() < nestLimit) {
//				if (distance == 0) {
//					System.out.println("ERRO--- zero");
//				}
//				// Fixed number of prey
//				// nextPrey.teleportTo(newRandomPosition());
//				numberOfFoodSuccessfullyForaged++;
//				if (((MultiPreyForagerRobot) nextPrey.getHolder()) != null) {
//					((MultiPreyForagerRobot) nextPrey.getHolder())
//							.dropPrey(nextPrey);
//				}
//				releasePrey(nextPrey);
//			}
//			i.updateCurrentDistance(distance);
//		}
//		return numberOfFoodSuccessfullyForaged;
//	}

	protected void releasePrey(Prey prey) {
		prey.setEnabled(false);
	}

}
