package environment;

import java.util.LinkedList;
import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.RandomWallRobotController;

public class SharedStateForageEnvironment extends Environment {
	
	private static final double PREY_RADIUS = 0.1;
	private static final double PREY_MASS = 1000;
	private LinkedList<Wall> walls;
	private Random random;
	private boolean specialBeahvior;

	public SharedStateForageEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		
		walls = new LinkedList<Wall>();
		
		specialBeahvior = args.getArgumentAsIntOrSetDefault("specialbeahvior", 0) == 1;
		this.random = simulator.getRandom();
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
//		addPrey(new Prey(simulator, "Prey ", newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		
		Arguments programmedRobotArguments = simulator.getArguments().get("--programmedrobots");
		int programmesRobots = programmedRobotArguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
		
		for (int i = 0; i < programmesRobots; i++) {
			Robot robot = Robot.getRobot(simulator,programmedRobotArguments);
			robot.setController(new RandomWallRobotController(simulator,robot,programmedRobotArguments, specialBeahvior));
			robot.setPosition(newRandomPosition());
			robot.setOrientation(random.nextDouble()*(2*Math.PI));
			addRobot(robot);
		}
		
		// Parede do mapa
		walls.add(new Wall(simulator, "topWall", 0, height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "bottomWall", 0, -height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "leftWall", -width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "rightWall", width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		for (Wall wall : walls) {
			addObject(wall);
		}
		
		for (Robot r : getRobots()) {
			if(!r.getDescription().equals("prey")){
				r.setOrientation(random.nextDouble()*(2*Math.PI));
				
				double max = 0.5;
				r.setPosition(random.nextDouble()*max-max/2,random.nextDouble()*max-max/2);
			}	
		}
		
	}
	
	private Vector2d newRandomPosition() {
		double x = random.nextDouble() * (width - 0.3) - ((width-0.3)/2);
		double y = random.nextDouble() * (height - 0.3) - ((height-0.3)/2);
		return new Vector2d(x, y);
	}

	@Override
	public void update(double time) {
		
	}

}
