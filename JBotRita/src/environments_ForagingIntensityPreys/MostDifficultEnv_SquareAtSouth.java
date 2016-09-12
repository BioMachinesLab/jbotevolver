package environments_ForagingIntensityPreys;


import java.awt.Color;
import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class MostDifficultEnv_SquareAtSouth extends ForagingIntensityPreysEnvironment {
	// MostDifficultEnvironment =4
		private static final double MAX_X_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV = -2;
		private static final double MIN_X_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV = -1.7;
		private static final double MAX_Y_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV = 0;
		private static final double MIN_Y_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV = -1.7;

		private static final double MAX_X_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = 0.5;
		private static final double MIN_X_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = 0.1;
		private static final double MAX_Y_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = 0;
		private static final double MIN_Y_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV = -1.7;

		private final double MAX_X_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = 0.3;
		private static final double MIN_X_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = -1.7;
		private static final double MAX_Y_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = 0.7;
		private static final double MIN_Y_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV = 0.3;

		private static final double MAX_X_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = 0.3;
		private static final double MIN_X_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = -1.7;
		private static final double MAX_Y_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = -1.8;
		private static final double MIN_Y_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV = -2;

		private static final double MAX_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -0.3;
		private static final double MIN_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -1.4;
		private static final double MAX_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -0.2;
		private static final double MIN_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV = -1.4;
	private boolean firstPositionOfPreyWasAdded =false;
	
	
	
	public MostDifficultEnv_SquareAtSouth(Simulator simulator, Arguments arguments) {
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
//		env.addStaticObject(new Wall(simulator, -0.8, 0.2, 1.6, 0.1));  //horizontal north
//		env.addStaticObject(new Wall(simulator, -1.6, -0.7, 0.1, 1.9)); // vertical west
//		env.addStaticObject(new Wall(simulator, -0.8, -1.6, 1.6, 0.1)); //horizontal south
//		env.addStaticObject(new Wall(simulator, 0, -0.7, 0.1, 1.9)); //vertical east
//		
//		
//		env.addStaticObject(new Wall(simulator, -0.8, 0.2, 1.6, 0.1));  //horizontal north
//		env.addStaticObject(new Wall(simulator, -1.6, -0.7, 0.1, 1.9)); // vertical west
//		env.addStaticObject(new Wall(simulator, -0.8, -1.6, 1.6, 0.1)); //horizontal south
//		env.addStaticObject(new Wall(simulator, 0, -0.7, 0.1, 1.9)); //vertical east
//		
		
		env.addStaticObject(new Wall(simulator, -1.2, 0.3, 2.4, 0.15));  //horizontal north
		env.addStaticObject(new Wall(simulator, -2.4, -1.05, 0.15, 2.85)); // vertical west
		env.addStaticObject(new Wall(simulator, -1.2, -2.4, 2.4, 0.15)); //horizontal south
		env.addStaticObject(new Wall(simulator, 0, -1.05, 0.15, 2.85)); //vertical east
		
		
		env.addStaticObject(new Wall(simulator, -1.2, 0.3, 2.4, 0.15));  //horizontal north
		env.addStaticObject(new Wall(simulator, -2.4, -1.05, 0.15, 2.85)); // vertical west
		env.addStaticObject(new Wall(simulator, -1.2, -2.4, 2.4, 0.15)); //horizontal south
		env.addStaticObject(new Wall(simulator, 0, -1.05, 0.15, 2.85)); //vertical east
		

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
			if (random.nextDouble() < 0.5) {
				return random.nextDouble() < 0.5 ? new Vector2d(
						random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV - MIN_X_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV) + MIN_X_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV,
						random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV - MIN_Y_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV) + MIN_Y_LIMIT_FOR_PREY_WESTWALL_MOSTDIFFICULTMENV)

				: new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV - MIN_X_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV) + MIN_X_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV,
						random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV - MIN_Y_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV) + MIN_Y_LIMIT_FOR_PREY_EASTWALL_MOSTDIFFICULTMENV);
			} else {
				return random.nextDouble() < 0.5 ? new Vector2d(
						random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV - MIN_X_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV) + MIN_X_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV,
						random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV - MIN_Y_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV) + MIN_Y_LIMIT_FOR_PREY_NORTHWALL_MOSTDIFFICULTMENV)
						: new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV - MIN_X_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV)
								+ MIN_X_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV, random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV - MIN_Y_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV)
								+ MIN_Y_LIMIT_FOR_PREY_SOUTHWALL_MOSTDIFFICULTMENV);
			}

		} else {
			firstPositionOfPreyWasAdded = false;
			return new Vector2d(random.nextDouble() * (MAX_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV - MIN_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV) + MIN_X_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV,
					random.nextDouble() * (MAX_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV - MIN_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV) + MIN_Y_LIMIT_FOR_PREY_INSIDEWALL_MOSTDIFFICULTMENV);
		}
	}

}
