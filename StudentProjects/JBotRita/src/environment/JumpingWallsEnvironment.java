package environment;

import java.awt.Color;
import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.collisionhandling.knotsandbolts.RectangularShape;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpingWallsEnvironment extends Environment {
	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	

	
	@ArgumentsAnnotation(name="nestlimit", defaultValue="0.5")
	private double nestLimit;
	
	@ArgumentsAnnotation(name="foragelimit", defaultValue="2.0")
	private double forageLimit;
	
	@ArgumentsAnnotation(name="forbiddenarea", defaultValue="5.0")
	private	double forbiddenArea;
		

	private Nest nest;
	private Random random;
	private Simulator simulator;
	
	public JumpingWallsEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		this.simulator = simulator;
		nestLimit       = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit")       : .5;
		forageLimit     = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit")       : 2.0;
		forbiddenArea   = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea")       : 5.0;
		this.random = simulator.getRandom();
		
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		addStaticObject( new Wall( simulator, 0, 0.5, 1, 0.1));
		addStaticObject(new Wall( simulator, 0, -0.5, 1, 0.1));
		addStaticObject(new Wall( simulator, 0.5, 0, 0.1, 1));
		addStaticObject(new Wall( simulator, -0.5, 0, 0.1, 1));
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);	
		for(Robot r: getRobots()){
			r.teleportTo(newRandomPosition());
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}
			
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

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble()*(forageLimit-nestLimit)+nestLimit;
		double angle = random.nextDouble()*2*Math.PI;
		return new Vector2d(radius*Math.cos(angle),radius*Math.sin(angle));
	}
	
	@Override
	public void update(double time) {
//		if(time==0.0){
//			for(Robot r: getRobots())
//				r.teleportTo(newRandomPosition());
//		}
		// TODO Auto-generated method stub
	}
}
