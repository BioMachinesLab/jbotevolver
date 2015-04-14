package environment;

import java.util.LinkedList;
import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import camera.CameraTracker;

public class CameraTrackerEnvironment extends Environment {
	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	
	private LinkedList<Wall> walls;
	private Random random;
	private int preysCaught = 0;
	private int numberOfPreys;
	private double consumingDistance = 0.15;
	private int lag;
	
	
	public CameraTrackerEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.random = simulator.getRandom();
		
		walls = new LinkedList<Wall>();
		numberOfPreys = args.getArgumentAsIntOrSetDefault("numberofpreys", 1);
		lag = args.getArgumentAsIntOrSetDefault("lag", 0);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		for (int i = 0; i < numberOfPreys; i++)
			addPrey(new Prey(simulator, "Prey_"+i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		
		// Parede do mapa
		walls.add(new Wall(simulator, "topWall", 0, height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "bottomWall", 0, -height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "leftWall", -width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "rightWall", width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		for (Wall wall : walls) 
			addObject(wall);
		
		for (Robot r : getRobots()) {
			r.setOrientation(random.nextDouble()*(2*Math.PI));
			
			double max = 0.5;
			r.setPosition(random.nextDouble()*max-max/2,random.nextDouble()*max-max/2);	
		}
		
		CameraTracker tracker = new CameraTracker(simulator, lag);
		simulator.addCallback(tracker);
	}
	
	private Vector2d newRandomPosition() {
		double x = random.nextDouble() * (width - 0.1) - ((width-0.1)/2);
		double y = random.nextDouble() * (height - 0.1) - ((height-0.1)/2);
		return new Vector2d(x, y);
	}
	
	@Override
	public void update(double time) {
		for (Prey p : getPrey()) {
			for(Robot r : getRobots()) {
				if(r.getPosition().distanceTo(p.getPosition()) < consumingDistance){
					placePrey(p);
					preysCaught++;
				}
			}
		}
	}
	
	private void placePrey(Prey prey) {
		prey.teleportTo(newRandomPosition());
	}

	public int getPreysCaught() {
		return preysCaught;
	}
	
}
