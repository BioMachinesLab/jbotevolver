package extensions;

import java.util.Random;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleArenaEnvironment extends Environment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double wallThickness = 0.1;
	//6.28 radians = +- 2 pi
	protected double randomizeOrientationValue = 6.28;
	
	public SimpleArenaEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		//setupWalls(simulator, args);
	}

	@Override
	public void setup(Simulator simulator){
		super.setup(simulator);
		this.setupWalls(simulator);
		placeRobots(simulator);
	}
	
	protected void placeRobots(Simulator simulator) {
		Random random = simulator.getRandom();
		// place robots in a random position, with a random orientation, at the beginning of the simulation
		for(Robot r : this.robots){
			Vector2d randomPosition = generateRandomPosition(simulator, 
					this.width - (wallThickness + r.getRadius() * 2), 
					this.height - (wallThickness + r.getRadius() * 2));
			
			r.teleportTo(randomPosition);
			//robots current orientation +- a given offset
			double orientation = r.getOrientation() + (random.nextDouble()*2-1) * this.randomizeOrientationValue;
			r.setOrientation(orientation);
		}
	}
	
	protected Vector2d generateRandomPosition(Simulator simulator, double width, double height) {		
		Random random = simulator.getRandom();
		double x = random.nextDouble() * width - width/2;
		double y = random.nextDouble() * height - height/2;
		
		return new Vector2d(x,y); 
	}
	
	protected void setupWalls(Simulator simulator) {
		createHorizontalWalls(simulator);
		createVerticalWalls(simulator);
	}

	@Override
	public void update(double time) {
		
	}
	
	protected Wall createWall(Simulator simulator, double x, double y, double width, double height) {
		Wall w = new Wall(simulator,"wall",x,y,Math.PI,1,1,0,width,height,PhysicalObjectType.WALL);
		this.addObject(w);
		
		return w;
	}
	
	private void createHorizontalWalls(Simulator simulator) {
		createWall(simulator, 0, this.height/2 + wallThickness/2, width + wallThickness, wallThickness);

		createWall(simulator, 0, -this.height/2 - wallThickness/2,
				width + wallThickness, wallThickness);
	}
	
	private void createVerticalWalls(Simulator simulator) {
		createWall(simulator, -this.width/2 - wallThickness/2, 0, 
				wallThickness, height + wallThickness);
		
		createWall(simulator, width/2 + wallThickness/2, 0, wallThickness, height + wallThickness);
	}

}
