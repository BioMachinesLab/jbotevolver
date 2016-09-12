package environment;

import java.awt.Color;
import java.util.Random;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class Consuming1Environment extends Environment {
	protected static final double PREY_RADIUS = 0.025;
	protected static final double PREY_MASS = 1000;
	public static final int MAX_PREY_INTENSITY = 15;
	public static final int MIN_PREY_INTENSITY = 5;
	private boolean firstPositionOfPreyWasAdded = false;
	protected boolean isToTestAllEnv = false;

	// SimplestEnvironment=0
	protected static final double MAX_X_LIMIT_FOR_PREY_PREYENV = 1.8;
	protected static final double MIN_X_LIMIT_FOR_PREY_PREYENV = -1.8;
	protected static final double MAX_Y_LIMIT_FOR_PREY_PREYENV = 1.8;
	protected static final double MIN_Y_LIMIT_FOR_PREY_PREYENV = -1.8;

	// EasyEnvironment ==1
	private static final double MAX_X_LIMIT_FOR_PREY_WESTWALL_EASYENV = -1.6;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV = -1.8;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV = 0;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV = -0.9;
	private static final double MAX_X_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1.8;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV = -0.8;

	// MeddiumEnvironment =2
	private static final double MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.1;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.5;

	private static final double MAX_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = -0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = 0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = 0.1;
	//
	private static final double MAX_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.1;

	private static final double MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -0.1;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -0.5;

	// DifficultEnvironment=3

	private static final double MAX_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.5;
	private static final double MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.8;
	private static final double MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.6;

	private static final double MAX_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = 1;
	private static final double MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -1;
	private static final double MAX_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = 1;
	private static final double MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -0.5;

	@ArgumentsAnnotation(name = "foragelimit", defaultValue = "2.0")
	protected double forageLimit;

	@ArgumentsAnnotation(name = "forbiddenarea", defaultValue = "5.0")
	protected double forbiddenArea;

	@ArgumentsAnnotation(name = "numberofpreys", defaultValue = "20")
	protected int numberOfPreys;

	private int numberOfFoodSuccessfullyForaged = 0;
	protected Random random;

	private Simulator simulator;
	private boolean foodInCenter = false;
	private boolean change_PreyInitialDistance;

	private Prey preyEated;
	private int fitnesssample = 0;

	public Consuming1Environment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments
				.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments
				.getArgumentAsDouble("forbiddenarea") : 5.0;
		numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments
				.getArgumentAsInt("numberofpreys") : 20;
		// System.out.println(numberOfPreys);
		this.random = simulator.getRandom();
		fitnesssample = arguments.getArgumentAsInt("fitnesssample");
		fitnesssample = fitnesssample % 4;

	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		// if(isToTestAllEnv!=false){
		if (fitnesssample == 0) {
			addPreys(newRandomPositionSimplestEnvironment());
		} else if (fitnesssample == 1) {
			addPreys(newRandomPositionEasyEnvironment());
			addStaticObject(new Wall(simulator, 0.4, 0.5, 0.3, 0.1));
			addStaticObject(new Wall(simulator, -0.5, 0, 0.1, 1));
			addStaticObject(new Wall(simulator, 0, -1.5, 1.1, 0.1));
			addStaticObject(new Wall(simulator, -0.8, 1.5, 1.1, 0.1));
			addStaticObject(new Wall(simulator, 1.3, 0, 0.1, 2.1));
			addStaticObject(new Wall(simulator, -1.4, -0.5, 0.1, 1.1));
		} else if (fitnesssample == 2) {
			addPreys(newRandomPositionMeddiumEnvironment());
			addStaticObject(new Wall(simulator, -1.2, 0.2, 1, 0.1)); // horizontal
																		// west
			addStaticObject(new Wall(simulator, -0.7, 0.2, 0.1, 2.2)); // vertical
																		// west
			addStaticObject(new Wall(simulator, 1.2, 0.2, 1, 0.1)); // horinzontal
																	// east
			addStaticObject(new Wall(simulator, 0.7, 0.2, 0.1, 2.2)); // vertical
																		// south

		} else {
			addPreys(newRandomPositionDifficultEnvironment());
			addStaticObject(new Wall(simulator, 0, 1.4, 3, 0.1)); // horizontal
																	// north
			addStaticObject(new Wall(simulator, 1.5, 0, 0.1, 2.7)); // vertical
																	// left
			addStaticObject(new Wall(simulator, -1.5, 0, 0.1, 2.8)); // vertical
																		// right
		}

	}

	public void addPreys(Vector2d newRandomPositionDifficultEnvironment) {
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new IntensityPrey(simulator, "Prey " + i,
					newRandomPositionDifficultEnvironment, 0, PREY_MASS,
					PREY_RADIUS, randomIntensity()));
		}
	}

	protected Vector2d newRandomPosition() {
		if (fitnesssample == 0) {// simplestEnvironment
			return newRandomPositionSimplestEnvironment();

		} else if (fitnesssample == 1) { // easyEnvironment

			return newRandomPositionEasyEnvironment();
		}

		else if (fitnesssample == 2) {// MeddiumEnvironment
			return newRandomPositionMeddiumEnvironment();

		} else { // difficultEnvironment

			return newRandomPositionDifficultEnvironment();
		}
	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				prey.setIntensity(randomIntensity());
				prey.teleportTo(newRandomPosition());
				numberOfFoodSuccessfullyForaged++;
				preyEated = prey;
				change_PreyInitialDistance = true;
			}

			if (prey.getIntensity() < 9)
				prey.setColor(Color.BLACK);
			else if (prey.getIntensity() < 13)
				prey.setColor(Color.GREEN.darker());
			else
				prey.setColor(Color.RED);
		}
	}

	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
	}

	public boolean isToChange_PreyInitialDistance() {
		return change_PreyInitialDistance;
	}

	public Prey preyWithNewDistace() {

		return preyEated;
	}

	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

	public int randomIntensity() {
		Random random = simulator.getRandom();
		return (random.nextInt((MAX_PREY_INTENSITY - MIN_PREY_INTENSITY) + 1) + MIN_PREY_INTENSITY);
	}

	public Vector2d newRandomPosition(double maxX1, double minX1, double maxY1,
			double minY1, double maxX2, double minX2, double maxY2, double minY2) {

		if (firstPositionOfPreyWasAdded == false) {
			firstPositionOfPreyWasAdded = true;
			return new Vector2d(random.nextDouble() * (maxX1 - minX1) + minX1,
					random.nextDouble() * (maxY1 - minY1) + minY1);
		} else {
			firstPositionOfPreyWasAdded = false;
			return new Vector2d(random.nextDouble() * (maxX2 - minX2) + minX2,
					random.nextDouble() * (maxY2 - minY2) + minY2);
		}
	}

	public Vector2d newRandomPosition(double maxX1, double minX1, double maxY1,
			double minY1, double maxX2, double minX2, double maxY2,
			double minY2, double maxX3, double minX3, double maxY3,
			double minY3, double maxX4, double minX4, double maxY4, double minY4) {
		if (firstPositionOfPreyWasAdded == false) {

			firstPositionOfPreyWasAdded = true;
			return random.nextDouble() < 0.5 ? new Vector2d(random.nextDouble()
					* (maxX1 - minX1) + minX1, random.nextDouble()
					* (maxY1 - minY1) + minY1)

			: new Vector2d(random.nextDouble() * (maxX2 - minX2) + minX2,
					random.nextDouble() * (maxY2 - minY2) + minY2);
		} else {
			firstPositionOfPreyWasAdded = false;
			return random.nextDouble() < 0.5 ? new Vector2d(random.nextDouble()
					* (maxX3 - minX3) + minX3, random.nextDouble()
					* (maxY3 - minY3) + minY3)

			: new Vector2d(random.nextDouble() * (maxX4 - minX4) + minX4,
					random.nextDouble() * (maxY4 - minY4) + minY4);
		}
	}

	private Vector2d newRandomPositionEasyEnvironment() {
		return newRandomPosition(MAX_X_LIMIT_FOR_PREY_WESTWALL_EASYENV,
				MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV,
				MAX_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV,
				MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV,
				MAX_X_LIMIT_FOR_PREY_EASTWALL_EASYENV,
				MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV,
				MAX_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV,
				MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV);
	}

	private Vector2d newRandomPositionDifficultEnvironment() {
		return newRandomPosition(	 MAX_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV, MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV ,MAX_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV ,
		 MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV ,
		 MAX_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV,
		 MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV ,
		 MAX_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV,
		 MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV);
	}

	private Vector2d newRandomPositionMeddiumEnvironment() {
		return newRandomPosition(MAX_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV,
				MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV,
				MAX_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV,
				MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV,
				MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV,
				MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV,
				MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV,
				MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV,
				MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV,
				MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV,
				MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV,
				MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV,
				MAX_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV,
				MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV,
				MAX_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV,
				MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV);
	}

	private Vector2d newRandomPositionSimplestEnvironment() {
		return new Vector2d(random.nextDouble()
				* (MAX_X_LIMIT_FOR_PREY_PREYENV - MIN_X_LIMIT_FOR_PREY_PREYENV)
				+ MIN_X_LIMIT_FOR_PREY_PREYENV, random.nextDouble()
				* (MAX_Y_LIMIT_FOR_PREY_PREYENV - MIN_Y_LIMIT_FOR_PREY_PREYENV)
				+ MIN_Y_LIMIT_FOR_PREY_PREYENV);
	}
}
