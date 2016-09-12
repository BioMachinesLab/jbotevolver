package environments_ForagingIntensityPreys;


import java.awt.Color;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class MeddiumEnv_Z extends ForagingIntensityPreysEnvironment {
	private static final double MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.5;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -1;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = 1.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = 1;

	private static final double MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 0.5;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 1;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -1;
	
	
	private boolean firstPositionOfPreyWasAdded =false;
	
	
	
	public MeddiumEnv_Z(Simulator simulator, Arguments arguments) {
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
        env.addStaticObject(new Wall(simulator, 0, 0, 4, 0.1)); // horizontal center in the forbidden area
        env.addStaticObject(new Wall(simulator, -1, 2, 2, 0.1)); // horizontal center in the forbidden area
        env.addStaticObject(new Wall(simulator, 1, -2, 2, 0.1)); // horizontal center in the forbidden area
        env.addStaticObject(new Wall(simulator, 0, 0, 0.1, 4)); // vertical center in the forbidden area

	
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
		if (firstPositionOfPreyWasAdded == true) {

			firstPositionOfPreyWasAdded = false;
			return  new Vector2d(random.nextDouble()
					* (MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV) + MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV, random.nextDouble()
					* (MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV - MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV) + MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV);
		} else {
			firstPositionOfPreyWasAdded = true;
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV) + MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV - MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV) + MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV);

			
		}
	}
}
