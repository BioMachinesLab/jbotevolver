package environment;

import java.awt.Color;
import java.util.Random;

import mathutils.Vector2d;
import phisicalObjects.IntensityPrey;
import sensors.IntensityPreyCarriedSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import actuators.IntensityPreyPickerActuator;

public class GarbageCollectorEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1000;
	public static final int MAX_PREY_INTENSITY = 15;
	public static final int MIN_PREY_INTENSITY = 5;
	
	private double preyRadius;
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
	@ArgumentsAnnotation(name="scaledarena", defaultValue="0")
	private boolean scaledArena;
	
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
				
		scaledArena = arguments.getArgumentAsIntOrSetDefault("scaledarena", 0) == 1;
		
		if(scaledArena)
			preyRadius = PREY_RADIUS * 10;
		else
			preyRadius = PREY_RADIUS;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			
			addPrey(new IntensityPrey(simulator, "Prey " + i,
					newRandomPosition(), 0, PREY_MASS, preyRadius,
					randomIntensity()));
			
		}
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);

		for (Robot r : robots)
			r.setOrientation(random.nextDouble() * (2 * Math.PI));
	}

	private Vector2d newRandomPosition() {
		double x = random.nextDouble() * (width - 0.3) - ((width-0.3) / 2);
		double y = random.nextDouble() * (height - 0.3) - ((height-0.3) / 2);
		return new Vector2d(x,y);
	}

	@Override
	public void update(double time) {

		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;

			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {

				prey.setIntensity(randomIntensity());

				prey.teleportTo(newRandomPosition());

			}
			
			if (prey.getIntensity()< 9)
				prey.setColor(Color.BLACK);
			else if (prey.getIntensity() < 13)
				prey.setColor(Color.GREEN.darker());
			else 
				prey.setColor(Color.RED);
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

	public int randomIntensity(){
		Random random = simulator.getRandom();
		return (random.nextInt((MAX_PREY_INTENSITY-MIN_PREY_INTENSITY)+1) + MIN_PREY_INTENSITY);
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
