package environmentsJumpingSumo;

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

public class JS_EasiestEnv_JustPreys2 extends JS_Environment {
	protected static final double MAX_X_LIMIT_FOR_PREY_PREYENV = 1.8;
	protected static final double MIN_X_LIMIT_FOR_PREY_PREYENV = -1.8;
	protected static final double MAX_Y_LIMIT_FOR_PREY_PREYENV = 1.8;
	protected static final double MIN_Y_LIMIT_FOR_PREY_PREYENV = 1.5;
	protected static final int WIDTH_HEIGHT_WALLSURRONDED =10;
	
	
	public JS_EasiestEnv_JustPreys2(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
		
	}

	public void setup(Environment env, Simulator simulator) { // when we want to use this env in other EnvMain -> (forLoopsOfEnvironmnt)
		super.setup(simulator);
		init(env);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		init(this);
	}

	public void init(Environment env) {
		
		int radius = 4;
		
		double y= random.nextDouble()*(radius-radius/2)+radius/2;
		double x=FastMath.sqrtQuick(radius- y);
		if(random.nextDouble()<0.5)
			x=-x;
	
		
			
		env.addPrey(new IntensityPrey(simulator, "Prey ", 
					new Vector2d(-x,y), 0, PREY_MASS, PREY_RADIUS,
					1));
		
		env.addStaticObject(new Wall(simulator, 0, WIDTH_HEIGHT_WALLSURRONDED/2, WIDTH_HEIGHT_WALLSURRONDED, 0.08)); // HorizontalWallNorth
		env.addStaticObject(new Wall(simulator, WIDTH_HEIGHT_WALLSURRONDED/2, 0, 0.08, WIDTH_HEIGHT_WALLSURRONDED)); // VerticalEast
		env.addStaticObject(new Wall(simulator, 0, -WIDTH_HEIGHT_WALLSURRONDED/2, WIDTH_HEIGHT_WALLSURRONDED, 0.08)); // HorizontalSouth
		env.addStaticObject(new Wall(simulator, -WIDTH_HEIGHT_WALLSURRONDED/2, 0, 0.08, WIDTH_HEIGHT_WALLSURRONDED)); // VerticalWest
		
	
		for(Robot r : env.getRobots()) {
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
			//System.out.println(Math.toDegrees(r.getOrientation()));
		}
	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {

				numberOfFoodSuccessfullyForaged=1;
				simulator.stopSimulation();
				preyEated = prey;
				change_PreyInitialDistance = true;
			}
			 if (prey.getIntensity() <=0)
					prey.setColor(Color.WHITE);	
				else if (prey.getIntensity() < 9)
					prey.setColor(Color.BLACK);
				else if (prey.getIntensity() < 13)
					prey.setColor(Color.GREEN.darker());
				else
					prey.setColor(Color.RED);
		}

	}

	public Vector2d newRandomPosition() {
	
		return new Vector2d(random.nextDouble()
				* (MAX_X_LIMIT_FOR_PREY_PREYENV - MIN_X_LIMIT_FOR_PREY_PREYENV)
				+ MIN_X_LIMIT_FOR_PREY_PREYENV, random.nextDouble()
				* (MAX_Y_LIMIT_FOR_PREY_PREYENV - MIN_Y_LIMIT_FOR_PREY_PREYENV)
				+ MIN_Y_LIMIT_FOR_PREY_PREYENV);

	}

}
