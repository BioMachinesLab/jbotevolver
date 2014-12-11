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

public class RoundForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	
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

	public RoundForageEnvironment(Simulator simulator, Arguments arguments) {
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
		for(int i = 0; i < numberOfPreys; i++ ){
			addPrey(new Prey(simulator, "Prey "+i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		}
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);	
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*(forageLimit-nestLimit)+nestLimit;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	
	@Override
	public void update(double time) {
//		nest.shape.getClosePrey().update(time, teleported);
//		CloseObjectIterator i = nest.shape.getClosePrey().iterator();
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			 double distance = nextPrey.getPosition().length();
			 if(nextPrey.isEnabled() && distance < nestLimit){
				 if(distance == 0){
					 System.out.println("ERRO--- zero");
				 }
				 nextPrey.teleportTo(newRandomPosition());
				 numberOfFoodSuccessfullyForaged++;
			 }
		}
		
		for(Robot robot: robots){
			PreyCarriedSensor sensor = (PreyCarriedSensor)robot.getSensorByType(PreyCarriedSensor.class);
			if (sensor != null && sensor.preyCarried() && robot.isInvolvedInCollison()){
				PreyPickerActuator actuator = (PreyPickerActuator)robot.getActuatorByType(PreyPickerActuator.class);
				if(actuator != null) {
					Prey preyToDrop = actuator.dropPrey();
					preyToDrop.teleportTo(newRandomPosition());
				}
			}
		}
	}
	
	public int getNumberOfFoodSuccessfullyForaged() {
		return numberOfFoodSuccessfullyForaged;
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
