package environmentsJumpingSumo;

import java.awt.Color;

import net.jafama.FastMath;
import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class JS_EnvWithoutRatio extends JS_Environment {
	
	private static final double MAX_WIDTH_LIMIT_FOR_WALL = 2.2;
	private static final double MIN_WIDTH_LIMIT_FOR_WALL = -2.2;
	private static final double MAX_HEIGHT_LIMIT_FOR_WALL = 1;
	private static final double MIN_HEIGHT_LIMIT_FOR_WALL = -1;

	private Environment env;
	
	
	public JS_EnvWithoutRatio(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

	}

	public void setup(Environment env, Simulator simulator) { // use this for LoopsOfEnvironmnt
		super.setup(simulator);
		this.env=env;
		init(env);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		env=this;
		init(this);
		
	}

	public void init(Environment env) {
		env.addStaticObject(new Wall(simulator, 0, 2.5, 5, 0.125)); // HorizontalWallNorth
		env.addStaticObject(new Wall(simulator, 2.5, 0, 0.125, 5)); // VerticalEast
		env.addStaticObject(new Wall(simulator, 0, -2.5, 5, 0.125)); // HorizontalSouth
		env.addStaticObject(new Wall(simulator, -2.5, 0, 0.125, 5)); // VerticalWest
		
		double width_Prey = random.nextDouble()
				* (MAX_WIDTH_LIMIT_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
				+ MIN_WIDTH_LIMIT_FOR_WALL;
		
		double width_Robot = random.nextDouble()
				* (MAX_WIDTH_LIMIT_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
				+ MIN_WIDTH_LIMIT_FOR_WALL;
		
		double height = random.nextDouble()
				* (MAX_HEIGHT_LIMIT_FOR_WALL - MIN_HEIGHT_LIMIT_FOR_WALL)
				+ MIN_HEIGHT_LIMIT_FOR_WALL;
		
		
		double height_For_Prey = random.nextDouble()
				* (2.1 - height)
				+ height;
		
		
		double height_For_Robot = random.nextDouble()
				* (height - (-2.1))
				+ (-2.1);
		
		env.addStaticObject(new Wall(simulator, 0, height, 5, 0.125)); //centerWall
		env.addPrey(new IntensityPrey(simulator, "Prey " + 0,
				
				
				new Vector2d(width_Prey, height_For_Prey+0.2), 0, PREY_MASS, PREY_RADIUS,
				randomIntensity()));
		
		env.getRobots().get(0).setPosition(new Vector2d( width_Robot, height_For_Robot-0.2));
	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				// prey.setIntensity(randomIntensity());
				// prey.teleportTo(newRandomPosition());
				numberOfFoodSuccessfullyForaged = 1;
				preyEated = prey;
				change_PreyInitialDistance = true;
			}
			if (prey.getIntensity() <= 0)
				prey.setColor(Color.WHITE);
			else if (prey.getIntensity() < 9)
				prey.setColor(Color.BLACK);
			else if (prey.getIntensity() < 13)
				prey.setColor(Color.GREEN.darker());
			else
				prey.setColor(Color.RED);
		}

	}

}
