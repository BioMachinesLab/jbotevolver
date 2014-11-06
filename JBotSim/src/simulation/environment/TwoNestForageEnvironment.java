package simulation.environment;

import java.util.Random;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class TwoNestForageEnvironment extends Environment {

	protected static final double PREY_RADIUS = 0.025;
	protected static final double PREY_MASS = 1;
	
	@ArgumentsAnnotation(name="nestlimit", defaultValue="0.5")
	private double nestLimit;
	
	@ArgumentsAnnotation(name="foragelimit", defaultValue="2.0")
	protected double forageLimit;
	
	@ArgumentsAnnotation(name="forbiddenarea", defaultValue="7.0")
	private	double forbiddenArea;
	
	@ArgumentsAnnotation(name="nestdistance", defaultValue="2.0")
	protected double nestDistance;
	
	protected int amountOfFood;
	protected Nest nestA;
	protected Nest nestB;
	protected int numberOfFoodSuccessfullyForagedNestA = 0;
	protected int numberOfFoodSuccessfullyForagedNestB = 0;
	protected Vector2d center = new Vector2d(0, 0);
	protected Random random;

	public TwoNestForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		nestDistance = arguments.getArgumentIsDefined("nestdistance") ? arguments.getArgumentAsDouble("nestdistance") : 2;
		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 5.0;

		this.random = simulator.getRandom();
		
		if (arguments.getArgumentIsDefined("densityoffood")) {
			double densityoffood = arguments
					.getArgumentAsDouble("densityoffood");
			amountOfFood = (int) (densityoffood * Math.PI * forageLimit
					* forageLimit + .5);
		} else {
			amountOfFood = arguments.getArgumentIsDefined("amountfood") ? arguments
					.getArgumentAsInt("amountfood") : 20;
		}
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		nestA = new Nest(simulator, "NestA", -nestDistance / 2, 0, nestLimit);
		nestA.setParameter("TEAM", 1);
		nestB = new Nest(simulator, "NestB", nestDistance / 2, 0, nestLimit);
		nestB.setParameter("TEAM", 2);
		addObject(nestA);
		addObject(nestB);

		deployPreys(simulator);	
	}

	protected void deployPreys(Simulator simulator) {
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
			double radius = random.nextDouble() * (forageLimit);
			double angle = random.nextDouble() * 2 * Math.PI;
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
//			simulator.getExperiment().endExperiment();
		}
	}

	protected void dropPreysDueToCollison() {
		for(Robot robot: robots){
			PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class);
			if (sensor.preyCarried() && robot.isInvolvedInCollison()){
				PreyPickerActuator actuator = (PreyPickerActuator)robot.getActuatorByType(PreyPickerActuator.class);
				Prey preyToDrop = actuator.dropPrey();
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

	public Vector2d getNestAPosition() {
		return nestA.getPosition();
	}

	public Vector2d getNestBPosition() {
		return nestB.getPosition();
	}
}