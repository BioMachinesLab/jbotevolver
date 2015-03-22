package environment;

import java.util.LinkedList;
import java.util.Random;

import net.jafama.FastMath;
import mathutils.Vector2d;
import sensors.IntruderSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.RandomWallRobotController;

public class SharedSensorForageEnvironment extends Environment {
	
	private LinkedList<Wall> walls;
	private Random random;
	private boolean specialBeahvior;
	private boolean dynamicPreys;
	public double preyDistance = 0.15;
	private LinkedList<Robot> preys;
	private int preysCaught = 0;
	private Arguments args;
	private double currentNumberOfPredators;

	public SharedSensorForageEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.args = args;
		
		walls = new LinkedList<Wall>();
		preys = new LinkedList<Robot>();
		
		specialBeahvior = args.getArgumentAsIntOrSetDefault("specialbeahvior", 0) == 1;
		dynamicPreys = args.getArgumentAsIntOrSetDefault("dynamicpreys", 0) == 1;
		
		this.random = simulator.getRandom();
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		int numberOfProgrammedRobots = 0;
		Arguments programmedRobotArguments = simulator.getArguments().get("--programmedrobots");
		
		if(dynamicPreys){
			
			int sample = args.getArgumentAsInt("fitnesssample");
			
			for (Robot r : getRobots()) {
				if(!r.getDescription().equals("prey")){
					r.setOrientation(random.nextDouble()*(2*Math.PI));
					
					double max = 0.5;
					r.setPosition(random.nextDouble()*max-max/2,random.nextDouble()*max-max/2);
				}	
			}
			
			switch (sample) {
				case 0:
					currentNumberOfPredators = 5;
					numberOfProgrammedRobots = 2;
					changeNumberOfPredators(currentNumberOfPredators);
					
					break;
				case 1:
					currentNumberOfPredators = 5;
					numberOfProgrammedRobots = 5;
					changeNumberOfPredators(currentNumberOfPredators);
					
					break;
				case 2:
					currentNumberOfPredators = 5;
					numberOfProgrammedRobots = 10;
					changeNumberOfPredators(currentNumberOfPredators);
					break;
				case 3:
					currentNumberOfPredators = 10;
					numberOfProgrammedRobots = 2;
					changeNumberOfPredators(currentNumberOfPredators);
					break;
				case 4:
					currentNumberOfPredators = 10;
					numberOfProgrammedRobots = 5;
					changeNumberOfPredators(currentNumberOfPredators);
					break;
				case 5:
					currentNumberOfPredators = 10;
					numberOfProgrammedRobots = 10;
					changeNumberOfPredators(currentNumberOfPredators);
					break;
				case 6:
					currentNumberOfPredators = 20;
					numberOfProgrammedRobots = 2;
					changeNumberOfPredators(currentNumberOfPredators);
					break;
				case 7:
					currentNumberOfPredators = 20;
					numberOfProgrammedRobots = 5;
					changeNumberOfPredators(currentNumberOfPredators);
					break;
				case 8:
					currentNumberOfPredators = 20;
					numberOfProgrammedRobots = 10;
					changeNumberOfPredators(currentNumberOfPredators);
					break;
				default:
					Arguments robotsArguments = simulator.getArguments().get("--robots");
					currentNumberOfPredators = robotsArguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
					numberOfProgrammedRobots = programmedRobotArguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
					break;
			}

		}else {
			//Don't divide on the fitness function when == 0
			currentNumberOfPredators = 0;
			numberOfProgrammedRobots = programmedRobotArguments.getArgumentAsIntOrSetDefault("numberofrobots", 1);
		}
		
		for (int i = 0; i < numberOfProgrammedRobots; i++) {
			Robot prey = Robot.getRobot(simulator,programmedRobotArguments);
			prey.setController(new RandomWallRobotController(simulator,prey,programmedRobotArguments, specialBeahvior));
			prey.setPosition(newRandomPosition());
//			prey.setOrientation(random.nextDouble()*(2*Math.PI));
			addRobot(prey);
			preys.add(prey);
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

	/**
	 * 
	 * Robots that have an id bigger or equal to the 'number' received are disable and place far away from the arena.
	 * 
	 * @param number
	 */
	private void changeNumberOfPredators(double number) {
		for (Robot r : getRobots()) {
			if(!r.getDescription().equals("prey")){
				if(r.getId() >= number){
					r.setPosition(50, 50);
					r.setEnabled(false);
				}else{
					IntruderSensor intruder = (IntruderSensor)r.getSensorByType(IntruderSensor.class);
					intruder.setNumberOfRecivingRobots((int)number);
				}
				
			}
		}
	}
	
	private Vector2d newRandomPosition() {
		double radius = random.nextDouble() * (width/5) + height/4;
		double angle = random.nextDouble() * 2 * Math.PI;
		return new Vector2d(radius * FastMath.cosQuick(angle), radius * FastMath.sinQuick(angle));
	}

	@Override
	public void update(double time) {
		
		for (Robot p : preys) {
			for(Robot r : getRobots()) {
				if(!r.getDescription().equals("prey")){
					if(r.getPosition().distanceTo(p.getPosition()) < preyDistance){
						placePrey(p);
						preysCaught++;
					}
				}
			}
		}
	}

	private void placePrey(Robot prey) {
		Vector2d newPosition =  newRandomPosition();
		prey.setPosition(newPosition.x, newPosition.y);
	}

	public int getPreysCaught() {
		return preysCaught;
	}
	
	public LinkedList<Robot> getPreys() {
		return preys;
	}
	
	public double getCurrentNumberOfPredators() {
		return currentNumberOfPredators;
	}
}
