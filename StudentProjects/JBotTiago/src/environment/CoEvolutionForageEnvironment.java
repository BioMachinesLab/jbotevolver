package environment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import net.jafama.FastMath;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class CoEvolutionForageEnvironment extends Environment {

	private static final double PREY_RADIUS = 0.025;
	private static final double PREY_MASS = 1;
	private double nestLimit;
	private double forageLimit;
	private double forbiddenArea;
	private int numberOfPreys;
	private LinkedList<Nest> nests;
	private LinkedList<Wall> walls;
	private int numberOfFoodForagedNestA = 0;
	private int numberOfFoodForagedNestB = 0;
	private Random random;
	private boolean startInNest = false;

	public CoEvolutionForageEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

		walls = new LinkedList<Wall>();
		nests = new LinkedList<Nest>();

		nestLimit = arguments.getArgumentIsDefined("nestlimit") ? arguments.getArgumentAsDouble("nestlimit") : .5;
		forageLimit = arguments.getArgumentIsDefined("foragelimit") ? arguments.getArgumentAsDouble("foragelimit") : 2.0;
		forbiddenArea = arguments.getArgumentIsDefined("forbiddenarea") ? arguments.getArgumentAsDouble("forbiddenarea") : 5.0;
		
		startInNest = arguments.getArgumentAsIntOrSetDefault("startinnest", 0) == 1;

		this.random = simulator.getRandom();

		if (arguments.getArgumentIsDefined("densityofpreys")) {
			double densityoffood = arguments.getArgumentAsDouble("densityofpreys");
			numberOfPreys = (int) (densityoffood * Math.PI * forageLimit * forageLimit + .5);
		} else {
			numberOfPreys = arguments.getArgumentIsDefined("numberofpreys") ? arguments.getArgumentAsInt("numberofpreys") : 20;
		}

	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new Prey(simulator, "Prey " + i, newRandomPosition(), 0, PREY_MASS, PREY_RADIUS));
		}

		nests.add(new Nest(simulator, "NestA", width/2-0.5, height/2-0.5, nestLimit));
		nests.add(new Nest(simulator, "NestB", -width/2+0.5, -height/2+0.5, nestLimit));
		for (Nest nest : nests) {
			addObject(nest);
		}

		// Parede do mapa
		walls.add(new Wall(simulator, "topWall", 0, height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "bottomWall", 0, -height/2, Math.PI, 1, 1, 0, width, 0.05, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "leftWall", -width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		walls.add(new Wall(simulator, "rightWall", width/2, 0, Math.PI, 1, 1, 0, 0.05, height, PhysicalObjectType.WALL));
		for (Wall wall : walls) {
			addObject(wall);
		}
		
		if(startInNest) {
			for (Robot r : robots) {
				if(r.getDescription().equalsIgnoreCase("teama"))
					r.teleportTo(new Vector2d(width/2-0.5, height/2-0.5));
				else
					r.teleportTo(new Vector2d(-width/2+0.5, -height/2+0.5));
				
				r.setOrientation(random.nextDouble() * 2*Math.PI);
			}
		}
	}

	private Vector2d newRandomPosition() {
		double radius = random.nextDouble() * (width/3);
		double angle = random.nextDouble() * 2 * Math.PI;
		return new Vector2d(radius * FastMath.cosQuick(angle), radius * FastMath.sinQuick(angle));
	}

	@Override
	public void update(double time) {
		for (Nest nest : nests) {
			Iterator<Prey> i = getPrey().iterator();

			while (i.hasNext()) {
				Prey nextPrey = (Prey) (i.next());
				double distance = nextPrey.getPosition().distanceTo(nest.getPosition());
				if (distance < nestLimit && nextPrey.getHolder() != null) {
					
					Robot robot = nextPrey.getHolder();
					PreyPickerActuator actuator = (PreyPickerActuator) robot.getActuatorByType(PreyPickerActuator.class);
					if (actuator != null) 
						actuator.dropPrey();
					
					nextPrey.teleportTo(newRandomPosition());
					
					if (distance == 0) {
						System.out.println("ERRO--- zero");
					} else if (nest.getName().equalsIgnoreCase("NestA")) {
						if(robot.getDescription().equals("teama"))
							numberOfFoodForagedNestA++;
						else
							numberOfFoodForagedNestA--;
					}else{
						if(robot.getDescription().equals("teamb"))
							numberOfFoodForagedNestB++;
						else
							numberOfFoodForagedNestB--;
					}
				}
			}
		}

		for (Robot robot : robots) {
			PreyCarriedSensor sensor = (PreyCarriedSensor) robot.getSensorByType(PreyCarriedSensor.class);
			if (sensor != null && sensor.preyCarried() && robot.isInvolvedInCollison()) {
				PreyPickerActuator actuator = (PreyPickerActuator) robot.getActuatorByType(PreyPickerActuator.class);
				if (actuator != null) {
					actuator.dropPrey();
				}
			}
		}
	}

	public int getNumberOfFoodForagedNestA() {
		return numberOfFoodForagedNestA;
	}

	public int getNumberOfFoodForagedNestB() {
		return numberOfFoodForagedNestB;
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

	public LinkedList<Nest> getNests() {
		return nests;
	}
}
