package environment;

import java.util.LinkedList;
import java.util.Random;

import actuator.FakeActuator;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class CooperativePreyForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private double nestLimit;
	private double forageLimit;
	private double forbiddenArea;
	private int numberOfPreys;
	private LinkedList<Wall> walls;
	private int preysCaught = 0;
	private Random random;
	public double preyDistance = 0.15;
	private int robotsForaging;

	public CooperativePreyForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		walls = new LinkedList<Wall>();
		
		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 5.0;
		
		this.random = simulator.getRandom();

		if (arguments.getArgumentIsDefined("densityofpreys")) {
			double densityoffood = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int) (densityoffood * Math.PI * forageLimit * forageLimit + .5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 20;
		}

		robotsForaging = arguments.getArgumentAsIntOrSetDefault("robotsforaging", 2);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		}
		
		// Parede do mapa
		walls.add(new Wall(simulator, "topWall", 0, height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "bottomWall", 0, -height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "leftWall", -width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "rightWall", width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		for (Wall wall : walls) {
			addObject(wall);
		}
		
	}

	private Vector2d newRandomPosition() {
		double x = random.nextDouble() * (width - 0.1) - ((width-0.1)/2);
		double y = random.nextDouble() * (height - 0.1) - ((height-0.1)/2);
		return new Vector2d(x, y);
	}

	@Override
	public void update(double time) {
		for (Prey p : getPrey()) {
			int robotsNearPrey = 0;
			for(Robot r : getRobots()) {
				if(r.getPosition().distanceTo(p.getPosition()) < preyDistance){
					if(((FakeActuator)r.getActuatorByType(FakeActuator.class)).getValue() > 0.5)
						robotsNearPrey++;
				}
			}
			
			if(robotsNearPrey == robotsForaging) {
				placePrey(p);
				preysCaught++;
			}
		}
	}

	private void placePrey(Prey prey) {
		prey.teleportTo(newRandomPosition());
	}

	public int getPreysCaught() {
		return preysCaught;
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
	
	public LinkedList<Wall> getWalls() {
		return walls;
	}
	
}
