package environment;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.Wall;
import simulation.physicalobjects.collisionhandling.SimpleCollisionManager;
import simulation.util.Arguments;

public class ConsumingPreyMeddiumEnvironment extends ConsumingPreyEnvironment {

	private static final double MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.1;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV = -0.5;
	
	private static final double MAX_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = -0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = 0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV = 0.1;
//	
	private static final double MAX_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV =0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.5;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV = 0.1;
	
	private static final double MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 0.9;
	private static final double MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = 1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -0.1;
	private static final double MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV = -0.5;
	private boolean preyAddedBefore_onLeftSide=false;

	
	public ConsumingPreyMeddiumEnvironment(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		addStaticObject(new Wall(simulator, -1.2, 0.2, 1, 0.1));
		addStaticObject(new Wall(simulator, -0.7, 0.2, 0.1, 2.2));
		addStaticObject(new Wall(simulator, 1.2, 0.2, 1, 0.1));
		addStaticObject(new Wall(simulator, 0.7, 0.2, 0.1, 2.2));
		
	}

	@Override
	protected Vector2d newRandomPosition() {
		if(preyAddedBefore_onLeftSide==false){
			preyAddedBefore_onLeftSide=true;
			return random.nextDouble() <0.5 ? new Vector2d(
				random.nextDouble()
						* (MAX_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV)
						+ MIN_X_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV, random.nextDouble()
						* (MAX_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV -MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV) +MIN_Y_LIMIT_FOR_PREY_WESTNORTHWALL_MEDDIUMENV)
				: new Vector2d(random.nextDouble()
						* (MAX_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV - MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV)
						+ MIN_X_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV, random.nextDouble()
						* (MAX_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV -MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV) + MIN_Y_LIMIT_FOR_PREY_WESTSOUTHWALL_MEDDIUMENV) ;
		
		}else
			preyAddedBefore_onLeftSide=false;
			return random.nextDouble() <0.5 ? new Vector2d(
					random.nextDouble()
							* (MAX_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV -MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV )
							+MIN_X_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV, random.nextDouble()
							* (MAX_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV- MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV)
							+ MIN_Y_LIMIT_FOR_PREY_EASTSOUTHWALL_MEDDIUMENV):
				new Vector2d(random.nextDouble()
								* (MAX_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV -MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV )
								+MIN_X_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV, random.nextDouble()
								* (MAX_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV- MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV)
								+ MIN_Y_LIMIT_FOR_PREY_EASTNORTHWALL_MEDDIUMENV);
	
	}
}
