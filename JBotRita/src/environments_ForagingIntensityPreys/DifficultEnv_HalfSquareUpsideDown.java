package environments_ForagingIntensityPreys;


import java.awt.Color;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class DifficultEnv_HalfSquareUpsideDown extends ForagingIntensityPreysEnvironment {
	
	private static final double MAX_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1;
	private static final double MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV = -1;
	private static final double MAX_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 0;
	private static final double MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = -1.2;

	private static final double MAX_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = 1;
	private static final double MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -1;
	private static final double MAX_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -1.6;
	private static final double MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -1.8;
	private boolean firstPositionOfPreyWasAdded =false;
	
	
	
	public DifficultEnv_HalfSquareUpsideDown(Simulator simulator, Arguments arguments) {
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
		env.addStaticObject(new Wall(simulator, 0, -1.4, 3.1, 0.1)); // south
	    env.addStaticObject(new Wall(simulator, 1.5, 0, 0.1, 2.8)); // vertical left
        env.addStaticObject(new Wall(simulator, -1.5, 0, 0.1, 2.8)); // vertical right
		
	}

	@Override
	protected Vector2d newRandomPosition() {

		if (firstPositionOfPreyWasAdded == true) {
			firstPositionOfPreyWasAdded = false;
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV) + MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV) + MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV);
		} else {
			firstPositionOfPreyWasAdded = true;
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV) + MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV) + MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV);
		}
	}

}
