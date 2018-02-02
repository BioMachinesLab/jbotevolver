package environmentsJumpingSumo;


import java.awt.Color;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class OtherEnv extends JS_Environment {
	protected static final double MAX_X_LIMIT_FOR_PREY_PREYENV = 1.8;
	protected static final double MIN_X_LIMIT_FOR_PREY_PREYENV = -1.8;
	protected static final double MAX_Y_LIMIT_FOR_PREY_PREYENV = 1.8;
	protected static final double MIN_Y_LIMIT_FOR_PREY_PREYENV = 1.5;

	public OtherEnv(Simulator simulator, Arguments arguments) {
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
		
		for (int i = 0; i < numberOfPreys; i++)
			env.addPrey(new IntensityPrey(simulator, "Prey " + i,
					newRandomPosition(), 0, PREY_MASS, PREY_RADIUS,
					1));
		
		env.addStaticObject(new Wall(simulator, 0, 4, 8, 0.125)); // HorizontalWallNorth
		env.addStaticObject(new Wall(simulator, 4, 0, 0.125, 8)); // VerticalEast
		env.addStaticObject(new Wall(simulator, 0, -4, 8, 0.125)); // HorizontalSouth
		env.addStaticObject(new Wall(simulator, -4, 0, 0.125, 8)); // VerticalWest
		
		for(Robot r : env.getRobots()) {
			
			r.setOrientation(0
					);
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
