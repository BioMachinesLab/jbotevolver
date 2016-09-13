package environments_ForagingIntensityPreys;


import java.awt.Color;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class DifficultEnv_WallsMazze extends ForagingIntensityPreysEnvironment {
	// DifficultEnvironment=3
	private static final double MAX_X_LIMIT_FOR_PREY_NORTH_DIFFICULTENV = -0.3;
	private static final double MIN_X_LIMIT_FOR_PREY_NORTH_DIFFICULTENV = -0.7;
	private static final double MAX_Y_LIMIT_FOR_PREY_NORTH_DIFFICULTENV = 0.4;
	private static final double MIN_Y_LIMIT_FOR_PREY_NORTH_DIFFICULTENV = -0.4;

	private static final double MAX_X_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV = -1.2;
	private static final double MIN_X_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV = -1.4;
	private static final double MAX_Y_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV = -0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV = -0.7;

	
	private boolean firstPositionOfPreyWasAdded =false;
	
	public DifficultEnv_WallsMazze(Simulator simulator, Arguments arguments) {
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
		env.addStaticObject(new Wall(simulator, 0, 2, 4, 0.1)); // horizontal north in the forbidden area
		env.addStaticObject(new Wall(simulator, 2, 0, 0.1, 4)); // vertical east in the forbidden area
		env.addStaticObject(new Wall(simulator, 0, -2, 4, 0.1));  // horizontal south in the forbidden area
		env.addStaticObject(new Wall(simulator, -2, 0, 0.1, 4)); // vertical west in the forbidden area
		env.addStaticObject(new Wall(simulator, -1.4, 0.5, 1.2, 0.1)); //id=1 horizontal close to the vertical west in the forbidden area						
		env.addStaticObject(new Wall(simulator, -0.8, -0.2, 0.1, 1.5)); //id=2 vertical right (close to the id==1)					
		env.addStaticObject(new Wall(simulator, 0.1, -0.9, 1.9, 0.1)); //id=3 horizontal (close to the id==2)
		env.addStaticObject(new Wall(simulator, 1, 0.2, 0.1, 2.1)); // id =4 vertical right (close to the id==3)	
		env.addStaticObject(new Wall(simulator, 0, 1.2, 2, 0.1));// horizontal in top (begins with the id==4) 	
		
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
	
	@Override
	protected Vector2d newRandomPosition() {
		if (firstPositionOfPreyWasAdded == false) {
			firstPositionOfPreyWasAdded = true;
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_NORTH_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_NORTH_DIFFICULTENV) + MIN_X_LIMIT_FOR_PREY_NORTH_DIFFICULTENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_NORTH_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_NORTH_DIFFICULTENV) + MIN_Y_LIMIT_FOR_PREY_NORTH_DIFFICULTENV);
		} else {
			firstPositionOfPreyWasAdded = false;
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV) + MIN_X_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV) + MIN_Y_LIMIT_FOR_PREY_SOUTH_DIFFICULTENV);
		}
	}
	

}
