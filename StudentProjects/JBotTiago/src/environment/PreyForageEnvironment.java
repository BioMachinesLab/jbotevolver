package environment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import net.jafama.FastMath;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class PreyForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private double nestLimit;
	private double forageLimit;
	private double forbiddenArea;
	private int numberOfPreys;
	private	Nest nest;
	private LinkedList<Wall> walls;
	private int numberOfFoodForaged = 0;
	private Random random;
	private boolean hidePreys;

	public PreyForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		walls = new LinkedList<Wall>();
		
		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 5.0;
		hidePreys = arguments.getArgumentAsIntOrSetDefault("hidepreys", 0) == 1;

		this.random = simulator.getRandom();

		if (arguments.getArgumentIsDefined("densityofpreys")) {
			double densityoffood = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int) (densityoffood * Math.PI * forageLimit * forageLimit + .5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 20;
		}

	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		}

		nest = new Nest(simulator, "NestA", 0, 0, nestLimit);
		addObject(nest);
		
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble() * (width/5) + width/4;
		double angle = random.nextDouble() * 2 * Math.PI;
		return new Vector2d(radius * FastMath.cosQuick(angle), radius * FastMath.sinQuick(angle));
	}

	@Override
	public void update(double time) {
		Iterator<Prey> i = getPrey().iterator();

		while (i.hasNext()) {
			Prey nextPrey = (Prey) (i.next());
			double distance = nextPrey.getPosition().distanceTo(nest.getPosition());
			if (distance < nestLimit && nextPrey.getHolder() != null) {
				
				Robot robot = nextPrey.getHolder();
				PreyPickerActuator actuator = (PreyPickerActuator) robot.getActuatorByType(PreyPickerActuator.class);
				if (actuator != null) 
					actuator.dropPrey();
				
				if(hidePreys)
					nextPrey.teleportTo(new Vector2d(-10, 10));
				else
					nextPrey.teleportTo(newRandomPosition());
				
				if (distance == 0) {
					System.out.println("ERRO--- zero");
				} else 
					numberOfFoodForaged++;
			}
		}

		for (Robot robot : robots) {
			PreyCarriedSensor sensor = (PreyCarriedSensor) robot.getSensorByType(PreyCarriedSensor.class);
			if (sensor != null && sensor.preyCarried() && robot.isInvolvedInCollison()) {
				PreyPickerActuator actuator = (PreyPickerActuator) robot.getActuatorByType(PreyPickerActuator.class);
				if (actuator != null) {
					actuator.dropPrey();
				}
			}
		}
	}

	public int getNumberOfFoodForaged() {
		return numberOfFoodForaged;
	}
	
	public Nest getNest() {
		return nest;
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
	
}
