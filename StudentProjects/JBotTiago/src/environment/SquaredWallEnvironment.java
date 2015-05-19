package environment;

import java.util.LinkedList;
import java.util.Random;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class SquaredWallEnvironment extends Environment {

	private LinkedList<Wall> walls;
	
	private Random random;
	
	@ArgumentsAnnotation(name="centralsquare", defaultValue="0")
	private boolean centralSquare;
	@ArgumentsAnnotation(name="squarewidth", defaultValue="0.3")
	private double squarelWidth;
	@ArgumentsAnnotation(name="squareheight", defaultValue="0.3")
	private double squareHeigth;
	
	public SquaredWallEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		walls = new LinkedList<Wall>();
		random = simulator.getRandom();
		
		centralSquare = args.getArgumentAsIntOrSetDefault("centralsquare", 0)==1;
		squarelWidth = args.getArgumentAsDoubleOrSetDefault("squarewidth", 0.3);
		squareHeigth = args.getArgumentAsDoubleOrSetDefault("squareheight", 0.3);;
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		width = random.nextDouble() + 1.5;
		height = random.nextDouble() + 1.5;

		// Parede do mapa
		walls.add(new Wall(simulator, "topWall", 0, height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "bottomWall", 0, -height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "leftWall", -width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "rightWall", width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		for (Wall wall : walls) {
			addObject(wall);
		}
		
		if(centralSquare){
			addObject(new Wall(simulator, "centralSquare", 0, 0, Math.PI, 1, 1, 0, squarelWidth, squareHeigth, PhysicalObjectType.WALL));
		}
		
		int numRobots = getRobots().size();
		
		for (Robot r : getRobots()) {
			r.setOrientation(random.nextDouble()*(2*Math.PI));
			
			double max = 0.5;
			
			if(numRobots > 1)
				placeMultipleRobots(r, max);
			else
				r.setPosition(random.nextDouble()*max-max/2,random.nextDouble()*max-max/2);
			
		}
		
	}

	private void placeMultipleRobots(Robot r, double max) {
		double minDist = Double.MAX_VALUE;
		
		do{
			r.setPosition(random.nextDouble()*max-max/2,random.nextDouble()*max-max/2);
			
			minDist = Double.MAX_VALUE;
			for (Robot neighbor : getRobots()) {
				if(r.getId() != neighbor.getId()){
					double dist = r.getPosition().distanceTo(neighbor.getPosition());
					if(dist < minDist)
						minDist = dist;
				}
			}
			
		}while(minDist == Double.MAX_VALUE || minDist < 0.2);
	}
	
	@Override
	public void update(double time) {
		
	}

}
