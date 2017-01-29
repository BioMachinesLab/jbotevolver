package enviromentsSwarms;

import java.awt.Color;


import environments_JumpingSumoIntensityPreys2.copy.JS_Environment;
import net.jafama.FastMath;
import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;

public class FireEnv extends JS_Environment {

//	private static final double MAX_WIDTH_LIMIT_FOR_WALL = 5;//4
//	private static final double MIN_WIDTH_LIMIT_FOR_WALL = 3.875; //3.1
	private static final double WIDTH_HEIGHT_WALLSURRONDED = 12.5;
	private static final double PREY_INTENSITY=5.0;
	private int numberOfWalls=6;
	private Environment env;
	private double ratio;
	private double intensityOfPrey=0.0;
	

	public FireEnv(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
	}

	public void setup(Environment env, Simulator simulator, double ratio) { // use this for LoopsOfEnvironmnt
		super.setup(simulator);
		this.env = env;
		this.ratio=ratio;
		init();
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		env = this;
		init();
	}

	public void init() {
		height=WIDTH_HEIGHT_WALLSURRONDED;
		width=WIDTH_HEIGHT_WALLSURRONDED;
		//env.addStaticObject(new Wall(simulator, 0, WIDTH_HEIGHT_WALLSURRONDED/2, WIDTH_HEIGHT_WALLSURRONDED, 0.625)); // HorizontalWallNorth
		//env.addStaticObject(new Wall(simulator, WIDTH_HEIGHT_WALLSURRONDED/2, 0, 0.625, WIDTH_HEIGHT_WALLSURRONDED)); // VerticalEast
		//env.addStaticObject(new Wall(simulator, 0, -WIDTH_HEIGHT_WALLSURRONDED/2, WIDTH_HEIGHT_WALLSURRONDED, 0.25)); // HorizontalSouth
		//env.addStaticObject(new Wall(simulator, -WIDTH_HEIGHT_WALLSURRONDED/2, 0, 0.625, WIDTH_HEIGHT_WALLSURRONDED)); // VerticalWest
		
		
		double rangeBetweenWalls=WIDTH_HEIGHT_WALLSURRONDED/(numberOfWalls-1);
		for(int i=0; i<numberOfWalls; i++){
			env.addStaticObject(new Wall(simulator, 0, 	WIDTH_HEIGHT_WALLSURRONDED/2-rangeBetweenWalls*i, WIDTH_HEIGHT_WALLSURRONDED, 0.2)); // Horizon
			env.addStaticObject(new Wall(simulator, WIDTH_HEIGHT_WALLSURRONDED/2-rangeBetweenWalls*i, 0, 0.2, WIDTH_HEIGHT_WALLSURRONDED)); // VerticalWest
			if(i<numberOfWalls-1)
				env.getRobots().get(i).setPosition(WIDTH_HEIGHT_WALLSURRONDED/2-rangeBetweenWalls*i-rangeBetweenWalls/2,-WIDTH_HEIGHT_WALLSURRONDED/2+rangeBetweenWalls/2);
		}
		
		double randomX_Position = random.nextInt(numberOfWalls-1);
		double randomY_Position = random.nextInt(numberOfWalls-2);
		
		
		double wallX_Selected=WIDTH_HEIGHT_WALLSURRONDED/2 - rangeBetweenWalls*randomX_Position;
		double wallY_Selected=WIDTH_HEIGHT_WALLSURRONDED/2 - rangeBetweenWalls*randomY_Position;
		
		double fireX_Position=wallX_Selected-rangeBetweenWalls+(wallX_Selected-(wallX_Selected-rangeBetweenWalls))/2;
		double fireY_Position=wallY_Selected-rangeBetweenWalls+(wallY_Selected-(wallY_Selected-rangeBetweenWalls))/2;
		
		env.addPrey(new IntensityPrey(simulator, "Prey " + 0, new Vector2d( fireX_Position, fireY_Position), 0, PREY_MASS, 0.25, 1));
		//env.addPrey(new IntensityPrey(simulator, "Prey " + 0, new Vector2d( 0, -2.5), 0, PREY_MASS, 0.25, PREY_INTENSITY));
		
		for(Robot r : env.getRobots()) {
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
			
		}
	}

	public void addPreyAndRobotPosition(double x, double y,
			double randomYPosition) {

		env.addPrey(new IntensityPrey(simulator, "Prey " + 0, new Vector2d(x, y
				+ randomYPosition), 0, PREY_MASS, PREY_RADIUS,
				1));

		env.getRobots().get(0)
				.setPosition(new Vector2d(x, -y + randomYPosition));

	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			
			intensityOfPrey=prey.getIntensity();
			if (nextPrey.isEnabled() && intensityOfPrey <= 0) {
				simulator.stopSimulation();
				numberOfFoodSuccessfullyForaged = 1;
				preyEated = prey;
				change_PreyInitialDistance = true;
			}
			if (intensityOfPrey <= 0)
				prey.setColor(Color.WHITE);
			else if (intensityOfPrey < 9)
				prey.setColor(Color.BLACK);
			else if (intensityOfPrey < 13)
				prey.setColor(Color.GREEN.darker());
			else
				prey.setColor(Color.RED);
		}

	}
	
	public double getPercentOfIntensity(){
		return (PREY_INTENSITY-intensityOfPrey)/PREY_INTENSITY;
	}

}
