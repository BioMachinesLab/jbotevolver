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

public class EasyEnv_WallsSpreadAround extends ForagingIntensityPreysEnvironment {
	private static final double MAX_X_LIMIT_FOR_PREY_WESTWALL_EASYENV = -1.6;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV = -1.8;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV = 0;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV = -0.9;
	private static final double MAX_X_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1.8;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV = -0.8;
	private boolean firstPositionOfPreyWasAdded =false;
	
	
	
	public EasyEnv_WallsSpreadAround(Simulator simulator, Arguments arguments) {
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
		env.addStaticObject(new Wall(simulator, 0.4, 0.5, 0.3, 0.1));
		env.addStaticObject(new Wall(simulator, -0.5, 0, 0.1, 1));
		env.addStaticObject(new Wall(simulator, 0, -1.5, 1.1, 0.1));
		env.addStaticObject(new Wall(simulator, -0.8, 1.5, 1.1, 0.1));
		env.addStaticObject(new Wall(simulator, 1.3, 0, 0.1, 2.1));
		env.addStaticObject(new Wall(simulator, -1.4, -0.5, 0.1, 1.1));
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
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_WESTWALL_EASYENV - MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV) + MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV - MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV) + MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV);
		} else {
			firstPositionOfPreyWasAdded = false;
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_EASTWALL_EASYENV - MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV) + MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV - MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV) + MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV);
		}
	}

}
