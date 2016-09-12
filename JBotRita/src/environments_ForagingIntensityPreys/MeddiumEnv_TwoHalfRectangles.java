package environments_ForagingIntensityPreys;


import java.awt.Color;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class MeddiumEnv_TwoHalfRectangles extends ForagingIntensityPreysEnvironment {
	private static final double MAX_X_LIMIT_FOR_PREY_CENTER_DIFFICULTENV = 0.3;
	private static final double MIN_X_LIMIT_FOR_PREY_CENTER_DIFFICULTENV = -0.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_CENTER_DIFFICULTENV = 0.3;
	private static final double MIN_Y_LIMIT_FOR_PREY_CENTER_DIFFICULTENV = -0.3;

	private static final double MAX_X_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV = 0.7;
	private static final double MIN_X_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV = 0.3;
	private static final double MIN_Y_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV = -0.3;

	
	
	
	private static final double MAX_X_LIMIT_FOR_PREY_LEFT_DIFFICULTENV = -1;
	private static final double MIN_X_LIMIT_FOR_PREY_LEFT_DIFFICULTENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_LEFT_DIFFICULTENV = 0.3;
	private static final double MIN_Y_LIMIT_FOR_PREY_LEFT_DIFFICULTENV = -0.3;
	
	private boolean firstPositionOfPreyWasAdded =false;
	
	
	
	public MeddiumEnv_TwoHalfRectangles(Simulator simulator, Arguments arguments) {
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
		env.addStaticObject(new Wall(simulator, -1.3, 1.9, 1.1, 0.1)); //horizontal left top
		env.addStaticObject(new Wall(simulator, 1.2, 1.9, 1.5, 0.1)); //horizontal right top
		env.addStaticObject(new Wall(simulator, -1.3, -1.9, 1.1, 0.1)); //horizontal left bottom
		env.addStaticObject(new Wall(simulator, 1.2, -1.9, 1.5, 0.1)); //horizontal right bottom
		env.addStaticObject(new Wall(simulator, 0.5, 0.0, 0.1, 3.9)); //id=2 vertical right (close to the id==1)					
        env.addStaticObject(new Wall(simulator, -0.8, 0.0, 0.1, 3.8)); //id=2 vertical right (close to the id==1)					
	
		
		
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
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_CENTER_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_CENTER_DIFFICULTENV) + MIN_X_LIMIT_FOR_PREY_CENTER_DIFFICULTENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_CENTER_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_CENTER_DIFFICULTENV) + MIN_Y_LIMIT_FOR_PREY_CENTER_DIFFICULTENV);
		} else {
			firstPositionOfPreyWasAdded = false;
			return random.nextDouble() < 0.5 ? new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV) + MIN_X_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV) + MIN_Y_LIMIT_FOR_PREY_RIGHT_DIFFICULTENV): new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_LEFT_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_LEFT_DIFFICULTENV) + MIN_X_LIMIT_FOR_PREY_LEFT_DIFFICULTENV,
							random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_LEFT_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_LEFT_DIFFICULTENV) + MIN_Y_LIMIT_FOR_PREY_LEFT_DIFFICULTENV) ;
		
		}
		
		
	}

}
