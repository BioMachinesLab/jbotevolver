package environment;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import net.jafama.FastMath;
import mathutils.Vector2d;
import sensors.SimplePreySharedSensor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;

public class SharedPreyForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private int numberOfPreys;
	private LinkedList<Wall> walls;
	private int preysCaught = 0;
	private Random random;
	public double preyDistance = 0.15;
	private Nest nest;
	private Simulator simulator;
	private boolean blind;
	private boolean half_blind;
	private boolean placed;
	private boolean closePreys;
	private double preyLocation;
	private double preyLocationRadius;
	private boolean twoClusters;
	
	public SharedPreyForageEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		this.random = simulator.getRandom();
		this.simulator =  simulator;
		placed = true;
		
		walls = new LinkedList<Wall>();
		numberOfPreys = args.getArgumentIsDefined("numberofpreys") ? args.getArgumentAsInt("numberofpreys") : 20;
		blind = args.getArgumentAsIntOrSetDefault("blindrobots", 0) == 1;
		half_blind = args.getArgumentAsIntOrSetDefault("halfblindrobots", 0) == 1;
		closePreys = args.getArgumentAsIntOrSetDefault("closepreys", 0) == 1;
		preyLocationRadius = args.getArgumentAsDoubleOrSetDefault("preylocationradius", 1);
		twoClusters = args.getArgumentAsIntOrSetDefault("twoclusters", 0) == 1;
		
		preyLocation = getRandomNumberBetween(0.5, (getWidth()/2-1));
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		int randomOption = random.nextInt(4);
		int randomOption2 = 0;
		
		if(twoClusters){
			
			int halfNumberOfPreys = numberOfPreys/2;
			
			for (int i = 0; i < halfNumberOfPreys; i++) {
				addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(randomOption), 0, PREY_MASS, PREY_RADIUS));
			}

			do{
				randomOption2 = random.nextInt(4);
			}while(randomOption == randomOption2);
			
			for (int i = halfNumberOfPreys; i < numberOfPreys; i++) {
				addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(randomOption2), 0, PREY_MASS, PREY_RADIUS));
			}
			
		}else{
			for (int i = 0; i < numberOfPreys; i++) {
				if(closePreys)
					addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(randomOption), 0, PREY_MASS, PREY_RADIUS));
				else
					addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
			}
		}
		
		// Parede do mapa
		walls.add(new Wall(simulator, "topWall", 0, height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "bottomWall", 0, -height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "leftWall", -width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "rightWall", width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		for (Wall wall : walls) {
			addObject(wall);
		}
		
		nest = new Nest(simulator, "Nest", 0, 0, 0.25);
		nest.setColor(Color.orange);
		addObject(nest);

		if(blind){
			double halfOfRobots = robots.size() / 2.0; 
			
			for (Robot r : robots) {
				if(r.getId() >= halfOfRobots){
					SimplePreySharedSensor sensor = (SimplePreySharedSensor)r.getSensorByType(SimplePreySharedSensor.class);
					sensor.setEnabled(false);
					sensor.setCutOff(0.0);
					r.setBodyColor(Color.GREEN);
				}
			}
		}
		
		if(half_blind){
			double halfOfRobots = robots.size() / 2.0; 
			
			for (Robot r : robots) {
				if(r.getId() >= halfOfRobots){
					SimplePreySharedSensor sensor = (SimplePreySharedSensor)r.getSensorByType(SimplePreySharedSensor.class);
					sensor.setEnabled(false);
					sensor.setCutOff(sensor.getRange()/2);
					r.setBodyColor(Color.GREEN);
				}
			}
		}
		
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble() * (width/5) + width/3.5;
		double angle = random.nextDouble() * 2 * Math.PI;
		return new Vector2d(radius * FastMath.cosQuick(angle), radius * FastMath.sinQuick(angle));
	}

	private Vector2d newRandomPosition(int option) {
		switch (option) {
			case 0:
				return new Vector2d(preyLocation + (random.nextDouble()*preyLocationRadius), preyLocation + (random.nextDouble()*preyLocationRadius));
			case 1:
				return new Vector2d(-preyLocation - (random.nextDouble()*preyLocationRadius), preyLocation + (random.nextDouble()*preyLocationRadius));
			case 2:
				return new Vector2d(-preyLocation - (random.nextDouble()*preyLocationRadius), -preyLocation - (random.nextDouble()*preyLocationRadius));
			case 3:
				return new Vector2d(preyLocation + (random.nextDouble()*preyLocationRadius), -preyLocation - (random.nextDouble()*preyLocationRadius));
			default:
				return new Vector2d(preyLocation + (random.nextDouble()*preyLocationRadius), preyLocation + (random.nextDouble()*preyLocationRadius));
		}
		
	}
	
	@Override
	public void update(double time) {
		for (Robot r : getRobots()) {
			PreyPickerActuator picker = (PreyPickerActuator)r.getActuatorByType(PreyPickerActuator.class);
			if(picker.isCarryingPrey() && r.getPosition().distanceTo(nest.getPosition()) < 0.15){
				picker.dropPrey().setPosition(1000, 1000);
				preysCaught++;
			}
		}
		
		if(preysCaught % numberOfPreys != 0)
			placed = false;
		
		if(preysCaught % numberOfPreys == 0 && !placed){
			preyLocation = getRandomNumberBetween(0.5, (getWidth()/2-1));
			placePreys();
		}
		
	}

	private double getRandomNumberBetween(double min, double max){
		return random.nextDouble()*(max-min) + min;
	}
	
	public int getPreysCaught() {
		return preysCaught;
	}

	public Nest getNest() {
		return nest;
	}

	private void placePreys() {
		int randomOption = random.nextInt(4);
		int randomOption2 = 0;
		
		if(twoClusters){
			int halfNumberOfPreys = numberOfPreys/2;
			
			for (int i = 0; i < halfNumberOfPreys; i++) {
				getPrey().get(i).teleportTo(newRandomPosition(randomOption));
			}
				
			do{
				randomOption2 = random.nextInt(4);
			}while(randomOption == randomOption2);
			
			for (int i = halfNumberOfPreys; i < numberOfPreys; i++) {
				getPrey().get(i).teleportTo(newRandomPosition(randomOption2));
			}
			placed = true;
			
		}else{
			for (Prey p : getPrey()) {
				if(closePreys)
					p.teleportTo(newRandomPosition(randomOption));
				else
					p.teleportTo(newRandomPosition());
			}
			placed = true;
		}
	}
	
}
