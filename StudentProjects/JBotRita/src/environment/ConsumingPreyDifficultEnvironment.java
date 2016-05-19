package environment;

import physicalobjects.IntensityPrey;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;

public class ConsumingPreyDifficultEnvironment extends ConsumingPreyEnvironment {
	private static final double MAX_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.5;
	private static final double MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV  = -1.5;
	private static final double MAX_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.8;
	private static final double MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV = 1.6;
	
	private static final double MAX_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = 1;
	private static final double MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -1;
	private static final double MAX_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = 1;
	private static final double MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV = -0.5;

	private boolean top=false;
	private int primeiro=0;

	public ConsumingPreyDifficultEnvironment(Simulator simulator,
			Arguments arguments) {
		super(simulator, arguments);
		// TODO Auto-generated constructor stub
	}



	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		addStaticObject(new Wall(simulator, 0, 1.4 , 3, 0.1));
		addStaticObject(new Wall(simulator, 1.5, 0, 0.1, 2.7));
		addStaticObject(new Wall(simulator, -1.5, 0, 0.1, 2.8));
	
	}

	@Override
	protected Vector2d newRandomPosition() {
		if(!top){
			top=true;
			return new Vector2d(random.nextDouble()
				* ( MAX_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV  -  MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV )
				+  MIN_X_LIMIT_FOR_PREY_TOP_DIFFICULTENV , random.nextDouble()
				* ( MAX_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV  -  MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV )
				+  MIN_Y_LIMIT_FOR_PREY_TOP_DIFFICULTENV );
		}
		else{
			top=false;
			return new Vector2d(random.nextDouble()
					* (MAX_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV - MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV)
					+ MIN_X_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV, random.nextDouble()
					* (MAX_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV - MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV)
					+ MIN_Y_LIMIT_FOR_PREY_BOTTOM_DIFFICULTENV);
		}
	}
}

