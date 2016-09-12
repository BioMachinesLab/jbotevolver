package environment;

import java.util.ArrayList;
import java.util.Random;

import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpingEnvironment extends Environment{
	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	
	@ArgumentsAnnotation(name="nestlimit", defaultValue="0.5")
	private double nestLimit;
	
	@ArgumentsAnnotation(name="foragelimit", defaultValue="2.0")
	private double forageLimit;
	
	@ArgumentsAnnotation(name="forbiddenarea", defaultValue="5.0")
	private	double forbiddenArea;
		
	@ArgumentsAnnotation(name="numberofpreys", defaultValue="10")
	private int numberOfPreys;
	
	@ArgumentsAnnotation(name="densityofpreys", defaultValue="")
	private Nest nest;
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;
	private Simulator simulator;
	
	public JumpingEnvironment(Simulator simulator, Arguments arguments) {
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
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 10;
		}
	}
	
	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*(forageLimit-nestLimit)+nestLimit;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
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


	public double getNestRadius() {
		return nestLimit;
	}
	
	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}

	@Override
	public void update(double time) {
		// TODO Auto-generated method stub
		
	}
}
