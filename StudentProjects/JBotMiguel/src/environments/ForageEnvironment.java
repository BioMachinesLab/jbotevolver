package environments;

import java.util.Random;

import actuators.MultipleWheelPreyPickerActuator;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class ForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	
	@ArgumentsAnnotation(name="foragelimit", defaultValue="2.0")
	private double forageLimit;
	
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="20")
	private int numberOfPreys;
	
	@ArgumentsAnnotation(name="densityofpreys", defaultValue="")
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;
	
	private Arguments args;
	
	private double nestLimit;

	public ForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.args = arguments;
		forageLimit     = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit")       : 2.0;
		nestLimit = forageLimit/4.0;
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		if(simulator.getRobots().size() == 1) {
			Robot r = simulator.getRobots().get(0);
			r.setPosition(0, 0);
			r.setOrientation(0);
		}
		
		this.random = simulator.getRandom();
		
		if(args.getArgumentIsDefined("densityofpreys")){
			double densityoffood = args.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int)(densityoffood*Math.PI*forageLimit*forageLimit+.5);
		} else {
			numberOfPreys = args.getArgumentIsDefined("numberofpreys") ? args.getArgumentAsInt("numberofpreys") : 20;
		}
		
		for(int i = 0; i < numberOfPreys; i++ ){
			addPrey(new Prey(simulator, "Prey "+i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		}
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*(forageLimit-nestLimit)+nestLimit*1.1;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	
	@Override
	public void update(double time) {
		
		for(Robot robot: robots){
			MultipleWheelPreyPickerActuator actuator =
					(MultipleWheelPreyPickerActuator)robot.getActuatorByType(MultipleWheelPreyPickerActuator.class);
			if(actuator != null) {
				if(actuator.getNumberOfPreysCarried() > 0) {
					Prey preyToDrop = actuator.dropPrey();
					preyToDrop.teleportTo(newRandomPosition());
					numberOfFoodSuccessfullyForaged++;
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
}