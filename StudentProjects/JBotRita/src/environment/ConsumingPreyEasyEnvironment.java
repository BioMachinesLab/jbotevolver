package environment;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class ConsumingPreyEasyEnvironment extends ConsumingPreyEnvironment{
	private static final double MAX_X_LIMIT_FOR_PREY_WESTWALL_EASYENV = -1.6;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV = -1.8;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV = 0;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV = -0.9;
	private static final double MAX_X_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1.8;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV = 1;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV = -0.8;
	protected boolean preyAddedBefore_onRightSide=false;
	
	public ConsumingPreyEasyEnvironment(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
	
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		for (int i = 0; i < numberOfPreys; i++) {
			addPrey(new IntensityPrey(simulator, "Prey " + i,
					newRandomPosition(), 0, PREY_MASS,
					PREY_RADIUS, randomIntensity()));
		}
	
//		// primeiro de lado do ninho horizontal
		addStaticObject(new Wall(simulator, 0.4, 0.5, 0.3, 0.1));

		addStaticObject(new Wall(simulator, -0.5, 0, 0.1, 1));

//		// exterior ao ninho horizontal menorr

		addStaticObject(new Wall(simulator, 0, -1.5, 1.1, 0.1));
		addStaticObject(new Wall(simulator, -0.8, 1.5, 1.1, 0.1));
//
//		// exterior ao ninho vertical menores
		addStaticObject(new Wall(simulator, 1.3, 0, 0.1, 2.1));
		addStaticObject(new Wall(simulator, -1.4, -0.5, 0.1, 1.1));
	
	
	}
	
	@Override
	protected Vector2d newRandomPosition() {
		if(preyAddedBefore_onRightSide==false){
			preyAddedBefore_onRightSide=true;
			return new Vector2d(
					 random.nextDouble()
							* (MAX_X_LIMIT_FOR_PREY_EASTWALL_EASYENV - MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV)
							+ MIN_X_LIMIT_FOR_PREY_EASTWALL_EASYENV, random.nextDouble()
							* (MAX_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV -MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV) +MIN_Y_LIMIT_FOR_PREY_EASTWALL_EASYENV);
		}
		else{
			preyAddedBefore_onRightSide=false;
			return new Vector2d(random.nextDouble()
					* (MAX_X_LIMIT_FOR_PREY_WESTWALL_EASYENV - MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV)
					+ MIN_X_LIMIT_FOR_PREY_WESTWALL_EASYENV, random.nextDouble()
					* (MAX_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV -MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV) +MIN_Y_LIMIT_FOR_PREY_WESTWALL_EASYENV);

		}
		
	

					
	}
}
