package environment;

import java.awt.Color;
import java.util.Random;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class RoundForageIntensityEnvironment extends EmptyEnviromentsWithFixPositions {
	protected static final double PREY_RADIUS = 0.025;
	protected static final double PREY_MASS = 1000;
	
	@ArgumentsAnnotation(name="max_prey_intensity", defaultValue="15.0")
	protected double max_prey_intensity;
	
	@ArgumentsAnnotation(name="min_prey_intensity", defaultValue="5.0")
	protected double min_prey_intensity;
			
	protected boolean change_PreyInitialDistance;
	
	protected Prey newPrey;
			
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="1")
	protected int numberOfPreys;
	
	protected int numberOfFoodSuccessfullyForaged = 0;
	protected Random random;
	
	

	public RoundForageIntensityEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.random = simulator.getRandom();
		numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments
				.getArgumentAsInt("numberofpreys") : 1;
				
		numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 1;
		max_prey_intensity   = arguments.getArgumentIsDefined("max_prey_intensity") ? arguments.getArgumentAsDouble("max_prey_intensity") : 15.0;
		min_prey_intensity   = arguments.getArgumentIsDefined("min_prey_intensity") ? arguments.getArgumentAsDouble("min_prey_intensity") : 5.0;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(i);
		}
	}
	
	protected void addPrey(int i){
		this.addPrey(new IntensityPrey(simulator, "Prey " + i,
				randomPosition(), 0, PREY_MASS, 0.1, randomIntensity()));
	}
		
	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				prey.setIntensity(randomIntensity());
				prey.teleportTo(randomPosition());
				numberOfFoodSuccessfullyForaged++;
				newPrey = prey;
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
		return newPrey;
	}

	public double randomIntensity() {
		return random.nextDouble()*(max_prey_intensity - min_prey_intensity) + min_prey_intensity;
	}
	
	protected Vector2d randomPosition() {
		return new Vector2d(random.nextDouble() * (width - wallsSafeMargin * 2)
				+ (-(width / 2 - wallsSafeMargin)), random.nextDouble()
				* (width - wallsSafeMargin * 2)
				+ (-(width / 2 - wallsSafeMargin)));
	}
	
}
	
	
