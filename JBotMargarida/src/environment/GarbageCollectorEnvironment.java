package environment;

import java.util.Random;

import phisicalObjects.IntensityPrey;
import actuators.IntensityPreyPickerActuator;
import mathutils.Vector2d;
import net.jafama.FastMath;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class GarbageCollectorEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1000;
	public static final double PREY_INTENSITY = 10;
	@ArgumentsAnnotation(name = "nestlimit", defaultValue = "0.5")
	private double nestLimit;

	@ArgumentsAnnotation(name = "numberofpreys", defaultValue = "20")
	private int numberOfPreys;
	private Nest nest;
	private int numberOfGarbageSuccessfullyForaged = 0;
	private Random random;

	private Simulator simulator;
	@ArgumentsAnnotation(name = "forageradius", defaultValue = "2")
	private double forageRadius;

	public GarbageCollectorEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;

		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments
				.getArgumentAsDouble("nestlimit") : .5;

		this.random = simulator.getRandom();
		forageRadius = arguments.getArgumentAsDoubleOrSetDefault(
				"forageradius", 2);

		numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments
				.getArgumentAsInt("numberofpreys") : 20;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new IntensityPrey(simulator, "Prey " + i,
					newRandomPosition(), 0, PREY_MASS, PREY_RADIUS,
					PREY_INTENSITY));
		}
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
		
		for (Robot r : robots)
			r.setOrientation(random.nextDouble()*(2*Math.PI));
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble() * (width / 5) + width / 3.5;
		double angle = random.nextDouble() * 2 * Math.PI;
		return new Vector2d(radius * FastMath.cosQuick(angle), radius
				* FastMath.sinQuick(angle));
	}

	@Override
	public void update(double time) {

		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				prey.setIntensity(PREY_INTENSITY);
				prey.teleportTo(newRandomPosition());
			}
		}

		for (Robot robot : robots) {
			IntensityPreyCarriedSensor sensor = (IntensityPreyCarriedSensor) robot
					.getSensorByType(IntensityPreyCarriedSensor.class);
			double distanceToNest = robot.getPosition().distanceTo(
					nest.getPosition());
			if (sensor != null && sensor.preyCarried()
					&& distanceToNest < nest.getRadius()) {
				IntensityPreyPickerActuator actuator = (IntensityPreyPickerActuator) robot
						.getActuatorByType(IntensityPreyPickerActuator.class);
				if (actuator != null) {
					actuator.dropPrey();
					numberOfGarbageSuccessfullyForaged++;
				}
			}
		}
	}

	public double getForageRadius() {
		return forageRadius;
	}

	public int getNumberOfGarbageSuccessfullyForaged() {
		return numberOfGarbageSuccessfullyForaged;
	}

	public double getNestRadius() {
		return nestLimit;
	}

	public Nest getNest() {
		return nest;
	}

}
