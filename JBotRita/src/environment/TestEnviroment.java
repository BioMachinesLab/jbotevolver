package environment;

import java.awt.Color;

import environments_ForagingIntensityPreys.ForagingIntensityPreysEnvironment;
import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import robots.JumpingSumo;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class TestEnviroment extends ForagingIntensityPreysEnvironment {
	private static final double MAX_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.5;
	private static final double MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.8;
	private static final double MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.6;

	private static final double MAX_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = 1;
	private static final double MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -1;
	private static final double MAX_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = 1;
	private static final double MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -0.5;
	private boolean firstPositionOfPreyWasAdded =false;
	private JumpingSumo robot;
	
	
	public TestEnviroment(Simulator simulator, Arguments arguments) {
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
					preyPosition(), 0, PREY_MASS,
				PREY_RADIUS,2));
		
		env.addStaticObject(new Wall(simulator, 0, 1.4, 3.1, 0.1)); // north
	
	    env.addStaticObject(new Wall(simulator, 1.5, 0, 0.1, 2.8)); // vertical left
        
	    env.addStaticObject(new Wall(simulator, -1.5, 0, 0.1, 2.8)); // vertical right
		robot=(JumpingSumo) env.getRobots().get(0);

	}

	
	@Override
	public void update(double time) {

		change_PreyInitialDistance = false;
		for (Prey nextPrey : simulator.getEnvironment().getPrey()) {
			IntensityPrey prey = (IntensityPrey) nextPrey;
		
				if (nextPrey.isEnabled() && prey.getIntensity() <= 0) {
					simulator.stopSimulation();
					numberOfFoodSuccessfullyForaged = 1;
				}
				if (prey.getIntensity() <= 0)
					prey.setColor(Color.WHITE);
				else if (prey.getIntensity() < 2)
					prey.setColor(Color.BLACK);
				else if (prey.getIntensity() < 13)
					prey.setColor(Color.GREEN.darker());
				else
					prey.setColor(Color.RED);
		}
		
	}

	public Vector2d preyPosition() {
			return new Vector2d(0,1.7);
	}

}
