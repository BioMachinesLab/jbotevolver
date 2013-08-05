package simulation.environment;

import java.util.Random;

import controllers.RandomRobotController;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.DifferentialDriveRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class RoundRobotForageEnvironment extends Environment {
	private double nestLimit;
	private double forageLimit;
	private double forbiddenArea;
	private Nest nest;
	private Wall topWall;
	private Wall bottomWall;
	private Wall leftWall;
	private Wall rightWall;
	private int numberOfFoodSuccessfullyForaged = 0;
	private Random random;

	public RoundRobotForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments
				.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments
				.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments
				.getArgumentAsDouble("forbiddenarea") : 5.0;

		this.random = simulator.getRandom();
		
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		nest = new Nest(simulator, "Nest", 0, 0, nestLimit);
		addObject(nest);
//		Parede do mapa
		topWall = new Wall(simulator, "topWall", 0, forageLimit, Math.PI, 1, 1, 0, 4, 0.05, PhysicalObjectType.WALL);
		addObject(topWall);
		bottomWall = new Wall(simulator, "bottomWall", 0, -forageLimit, Math.PI, 1, 1, 0, 4, 0.05, PhysicalObjectType.WALL);
		addObject(bottomWall);
		leftWall = new Wall(simulator, "leftWall", -forageLimit, 0, Math.PI, 1, 1, 0, 0.05, 4.05, PhysicalObjectType.WALL);
		addObject(leftWall);
		rightWall = new Wall(simulator, "rightWall", forageLimit, 0, Math.PI, 1, 1, 0, 0.05, 4.05, PhysicalObjectType.WALL);
		addObject(rightWall);
		
		/*Pre-programados
		Arguments programmedRobotArguments = simulator.getArguments().get("--programmedrobots");
		
		Robot r = Robot.getRobot(simulator,programmedRobotArguments);
		r.setController(new RandomRobotController(simulator,r,programmedRobotArguments));
		addRobot(r);
		Robot r2 = Robot.getRobot(simulator,programmedRobotArguments);
		r2.setController(new RandomRobotController(simulator,r2,programmedRobotArguments));
		addRobot(r2);
		*/
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble() * (forageLimit - nestLimit) + nestLimit;
		double angle = random.nextDouble() * 2 * Math.PI;
		return new Vector2d(radius * Math.cos(angle), radius * Math.sin(angle));
	}

	@Override
	public void update(double time) {
			
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
