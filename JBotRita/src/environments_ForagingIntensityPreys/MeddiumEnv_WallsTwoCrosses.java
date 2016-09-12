package environments_ForagingIntensityPreys;


import java.awt.Color;
import java.util.Random;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Nest;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class MeddiumEnv_WallsTwoCrosses extends ForagingIntensityPreysEnvironment {
	private static final double MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.1;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.5;

	private static final double MAX_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = -0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = 0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = 0.1;
	//
	private static final double MAX_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.1;

	private static final double MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -0.1;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -0.5;
	private boolean firstPositionOfPreyWasAdded =false;
	
	
	
	public MeddiumEnv_WallsTwoCrosses(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
	
	}

	public void setup(Environment env, Simulator simulator){  // when we want to use this env in other EnvMain -> (forLoopsOfEnvironmnt) 
		super.setup(simulator);
        init(env);
	}

	@Override
	public void setup(Simulator simulator) {   
		super.setup(simulator);
		init(this);	
	}

	public void init(Environment env){   		
		for (int i = 0; i < numberOfPreys; i++)
			env.addPrey(new IntensityPrey(simulator, "Prey " + i,
					newRandomPosition(), 0, PREY_MASS,
				PREY_RADIUS, randomIntensity()));
		env.addStaticObject(new Wall(simulator, -1.2, 0.2, 1, 0.1)); // horizontal
																	// west
		env.addStaticObject(new Wall(simulator, -0.7, 0.2, 0.1, 2.2)); // vertical
																	// west
		env.addStaticObject(new Wall(simulator, 1.2, 0.2, 1, 0.1)); // horinzontal
																// east
		env.addStaticObject(new Wall(simulator, 0.7, 0.2, 0.1, 2.2)); // vertical
		
	}

	@Override
	public void update(double time) {
		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
			if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
				prey.setIntensity(randomIntensity());
				prey.teleportTo(newRandomPosition());
				numberOfFoodSuccessfullyForaged++;
				preyEated = prey;
				change_PreyInitialDistance = true;
			}
			if (prey.getIntensity() < 9)
				prey.setColor(Color.BLACK);
			else if (prey.getIntensity() < 13)
				prey.setColor(Color.GREEN.darker());
			else
				prey.setColor(Color.RED);
		}
		
	}

	public Vector2d newRandomPosition() {
		if (firstPositionOfPreyWasAdded == false) {

			firstPositionOfPreyWasAdded = true;
			return random.nextDouble() < 0.5 ? new Vector2d(random.nextDouble()
					* (MAX_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV) + MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV, random.nextDouble()
					* (MAX_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV - MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV) + MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV)

			: new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV) + MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV - MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV) + MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV);
		} else {
			firstPositionOfPreyWasAdded = false;
			return random.nextDouble() < 0.5 ? new Vector2d(random.nextDouble()
					* (MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV) + MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV, random.nextDouble()
					* (MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV - MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV) + MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV)

			: new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV) + MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV - MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV) + MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV);
		}
	}

}
