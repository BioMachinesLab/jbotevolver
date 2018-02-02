package environmentsJumpingSumo;

import java.awt.Color;
import java.util.Random;

import collisionHandling.JumpingCollisionManager;
import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.collisionhandling.SimpleCollisionManager;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JS_Environment extends Environment {
	protected static final double PREY_RADIUS = 0.025;
	protected static final double PREY_MASS = 1000;
	public static final int MAX_PREY_INTENSITY = 15;
	public static final int MIN_PREY_INTENSITY = 5;
			
	protected boolean change_PreyInitialDistance;
	
	protected Prey preyEated;
	
	private int fitnesssample = 0;
	
	@ArgumentsAnnotation(name="foragelimit", defaultValue="2.0")
	private double forageLimit;
	
	@ArgumentsAnnotation(name="forbiddenarea", defaultValue="5.0")
	private	double forbiddenArea;
	
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="1")
	protected int numberOfPreys;
	
	@ArgumentsAnnotation(name="densityofpreys", defaultValue="")

	protected int numberOfFoodSuccessfullyForaged = 0;
	protected Random random;
	
	protected Simulator simulator;

	public JS_Environment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;
		collisionManager = new JumpingCollisionManager(simulator);
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments
				.getArgumentAsDouble("foragelimit") : 2.5;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments
				.getArgumentAsDouble("forbiddenarea") :6.25;
		numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments
				.getArgumentAsInt("numberofpreys") : 1;
				
		if(arguments.getArgumentIsDefined("densityofpreys")){
			double densityoffood = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int)(densityoffood*Math.PI*forageLimit*forageLimit+.5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 1;
		}
	
		this.random = simulator.getRandom();
		fitnesssample = arguments.getArgumentAsInt("fitnesssample");
		fitnesssample = fitnesssample % 4;
		height=10;
		width=10;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
	}


	@Override
	public void update(double time) {
		
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
		return (random.nextInt((MAX_PREY_INTENSITY - MIN_PREY_INTENSITY) + 1) + MIN_PREY_INTENSITY);
	}
	
	public ClosePhysicalObjects getCloseWalls(){
		return getRobots().get(0).shape.getCloseWalls();
	}
	
}
	
	
