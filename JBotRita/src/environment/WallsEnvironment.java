package environment;

import java.util.Random;

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

public class WallsEnvironment extends Environment {
	private Vector2d   nestPosition = new Vector2d(0, 0);
	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private static final double MAX_X_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV = -2;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV = -1.7;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV =0;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV = -1.7;

	private static final double MAX_X_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = 0.5;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = 0.1;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = 0;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = -1.7;
	
	 final double MAX_X_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = 0.3;
		private static final double MIN_X_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = -1.7;
		private static final double MAX_Y_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = 0.7;
		private static final double MIN_Y_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = 0.3;

		
		private static final double MAX_X_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = 0.3;
		private static final double MIN_X_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = -1.7;
		private static final double MAX_Y_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = -1.8;
		private static final double MIN_Y_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = -2;

		
		
		private static final double MAX_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -0.3;
		private static final double MIN_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -1.4;
		private static final double MAX_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -0.2;
		private static final double MIN_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -1.4;
		
		
	@ArgumentsAnnotation(name="nestlimit", defaultValue="0.5")
	private double nestLimit;
	
	@ArgumentsAnnotation(name="foragelimit", defaultValue="2.0")
	private double forageLimit;
	
	@ArgumentsAnnotation(name="forbiddenarea", defaultValue="5.0")
	private	double forbiddenArea;
	
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="20")
	private int numberOfPreys;
	
	@ArgumentsAnnotation(name="densityofpreys", defaultValue="")
	private Nest nest;
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;
	
	private Simulator simulator;
	private boolean foodInCenter=false;
	private boolean change_PreyInitialDistance;
	
	private Prey prey;

	public WallsEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;
		
		nestLimit       = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit")       : .5;
		forageLimit     = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit")       : 2.0;
		forbiddenArea   = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")       : 5.0;
		
		this.random = simulator.getRandom();
		
		if(arguments.getArgumentIsDefined("densityofpreys")){
			double densityoffood = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int)(densityoffood*Math.PI*forageLimit*forageLimit+.5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 20;
		}
	}
	
	@Override
	public void setup(Simulator simulator) {
			super.setup(simulator);
				//addPrey(new Prey(simulator, "Prey "+0, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
			for(int i=0; i<3; i++){
				Vector2d position=  newRandomPositionSimplestEnvironment();
				addPrey(new Prey(simulator, "Prey "+i, position, 0, PREY_MASS, PREY_RADIUS));
				wallsForPrey(position);
			}
			
			addStaticObject(new Wall(simulator, -0.8, 0.2, 1.6, 0.1));
			addStaticObject(new Wall(simulator, -1.6, -0.7, 0.1, 1.9)); //evertical north
			addStaticObject(new Wall(simulator, -0.8, -1.6, 1.6, 0.1));
			addStaticObject(new Wall(simulator, 0, -0.7, 0.1, 1.9)); 
			
			
//			addStaticObject( new Wall( simulator, 0, 0.5, 1, 0.1));
//			addStaticObject(new Wall( simulator, 0, -0.5, 1, 0.1));
//			addStaticObject(new Wall( simulator, 0.5, 0, 0.1, 1));
//			addStaticObject(new Wall( simulator, -0.5, 0, 0.1, 1));
			nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
			addObject(nest);	
			
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*(forageLimit-nestLimit)+nestLimit+0.3;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	
	private void wallsForPrey(Vector2d position){
//		addStaticObject( new Wall( simulator, position.x+0.2, position.y, 0.1, 0.4));
//		addStaticObject(new Wall( simulator, position.x-0.2, position.y, 0.1, 0.4));
//		addStaticObject(new Wall( simulator, position.x, position.y+ 0.2, 0.4, 0.1));
//		addStaticObject(new Wall( simulator, position.x, position.y-0.2, 0.4, 0.1));
	
		addStaticObject(new Wall(simulator, -0.8, 0.2, 1.5, 0.1));
		addStaticObject(new Wall(simulator, -1.6, -0.7, 0.1, 1.9));
		addStaticObject(new Wall(simulator, -0.8, -1.6, 1.5, 0.1));
		addStaticObject(new Wall(simulator, 0, -0.7, 0.1, 1.9));
	}
	
	@Override
	public void update(double time) {
		change_PreyInitialDistance=false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
//			 double distance = nextPrey.getPosition().length();
			 double distance = nextPrey.getPosition().distanceTo(nestPosition);
			 if(nextPrey.isEnabled() && distance < 0.5){
//				 if(distance == 0){
//					 System.out.println("ERRO--- zero");
//				 }
				// Vector2d position= newRandomPosition();
				 Vector2d position= newRandomPositionSimplestEnvironment();
				 nextPrey.teleportTo(position);
				 numberOfFoodSuccessfullyForaged++;
				 prey=nextPrey;
				 change_PreyInitialDistance=true;
			 }
		}
//		for(Robot robot: robots){
//			PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class);
//			if (sensor != null && sensor.preyCarried() && robot.isInvolvedInCollison()){
//				PreyPickerActuator actuator = (PreyPickerActuator)robot.getActuatorByType(PreyPickerActuator.class);
//				if(actuator != null) {
//					Prey preyToDrop = actuator.dropPrey();
////					preyToDrop.teleportTo(position);
////					wallsForPrey(position);
//				}
//			}
//		}
		
	}
	
	private Vector2d newRandomPositionSimplestEnvironment() {
		return new Vector2d(random.nextDouble()
				* (MAX_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV - MIN_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV)
				+ MIN_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV, random.nextDouble()
				* (MAX_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV - MIN_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV)
				+ MIN_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV);
	}
	
	public int getNumberOfFoodSuccessfullyForaged() {
//		System.out.println("food foragin"+numberOfFoodSuccessfullyForaged);
		return numberOfFoodSuccessfullyForaged;
	}

	public boolean isToChange_PreyInitialDistance(){
		return change_PreyInitialDistance;
	}
	
	public Prey preyWithNewDistace(){
		
		return prey;
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
