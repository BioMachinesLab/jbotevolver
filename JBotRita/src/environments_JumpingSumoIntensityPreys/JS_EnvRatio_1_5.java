package environments_JumpingSumoIntensityPreys;

import java.awt.Color;

import net.jafama.FastMath;
import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class JS_EnvRatio_1_5 extends JS_Environment {
	
	private static final double MAX_WIDTH_LIMIT_FOR_WALL = 2.3;
	private static final double MIN_WIDTH_LIMIT_FOR_WALL = 1.3;
	private Environment env;
	
	
	public JS_EnvRatio_1_5(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

	}

	public void setup(Environment env, Simulator simulator, double ratio) { // use this for LoopsOfEnvironmnt
		super.setup(simulator);
		this.env=env;
		init();
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		env=this;
		init();
	}

	public void init() {
		env.addStaticObject(new Wall(simulator, 0, 2.5, 5, 0.125)); // HorizontalWallNorth
		env.addStaticObject(new Wall(simulator, 2.5, 0, 0.125, 5)); // VerticalEast
		env.addStaticObject(new Wall(simulator, 0, -2.5, 5, 0.125)); // HorizontalSouth
		env.addStaticObject(new Wall(simulator, -2.5, 0, 0.125, 5)); // VerticalWest
		
		
		double b = random.nextDouble()
				* (MAX_WIDTH_LIMIT_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
				+ MIN_WIDTH_LIMIT_FOR_WALL;
			
		double a = b / FastMath.sqrtQuick(1.25);
		
		env.addStaticObject(new Wall(simulator, 0, 0, b * 2 - 0.4, 0.125)); //centerWall
			
		env.addPrey(new IntensityPrey(simulator, "Prey " + 0, new Vector2d(0, a), 0, PREY_MASS, PREY_RADIUS, 1));
		
		env.getRobots().get(0).setPosition(new Vector2d( 0, -a));
		
		if (random.nextDouble() < 0.5) {
			double width = b - 1;
			double new_a=a- width / FastMath.sqrtQuick(1.25);
			
		
			env.addStaticObject(new Wall(simulator, 0, new_a, width * 2-0.4 , 0.125)); //centerWall
			env.addStaticObject(new Wall(simulator, 0, -new_a, width * 2-0.4 , 0.125)); //centerWall
			
		
		}
		for(Robot r : env.getRobots()) {
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		}

	}
	
	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				numberOfFoodSuccessfullyForaged = 1;
				simulator.stopSimulation();
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
