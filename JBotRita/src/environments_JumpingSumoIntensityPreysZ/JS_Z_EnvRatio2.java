package environments_JumpingSumoIntensityPreysZ;

import java.awt.Color;

import mathutils.Vector2d;
import net.jafama.FastMath;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;
import environments_JumpingSumoIntensityPreys.JS_Environment;

public class JS_Z_EnvRatio2 extends JS_Environment {

	private static final double MAX_WIDTH_LIMIT_FOR_WALL = 2.3;
	private static final double MIN_WIDTH_LIMIT_FOR_WALL = 0.9;

	private static final double MAX_HEIGHT_LIMIT_FOR_WALL = 1;
	private static final double MIN_HEIGHT_LIMIT_FOR_WALL = -1;
	double ratio = 3;
	private Environment env;

	public JS_Z_EnvRatio2(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

	}

	public void setup(Environment env, Simulator simulator, double ratio) { // use
																			// this
																			// for
																			// LoopsOfEnvironmnt
		super.setup(simulator);
		this.env = env;
		this.ratio = ratio;
		init();
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		env = this;

		init();

	}

	public void init() {
		env.addStaticObject(new Wall(simulator, 0, 2.5, 5, 0.125, 0.2)); // HorizontalWallNorth
		env.addStaticObject(new Wall(simulator, 2.5, 0, 0.125, 5, 0.2)); // VerticalEast
		env.addStaticObject(new Wall(simulator, 0, -2.5, 5, 0.125, 0.2)); // HorizontalSouth
		env.addStaticObject(new Wall(simulator, -2.5, 0, 0.125, 5, 0.2)); // VerticalWest

		// double b = random.nextDouble()
		// * (MAX_WIDTH_LIMIT_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
		// + MIN_WIDTH_LIMIT_FOR_WALL;

		double b = 2.3;

		double a = b / FastMath.sqrtQuick(ratio); 

		env.addStaticObject(new Wall(simulator, 0, 0, b * 2 - 0.4, 0.125)); // centerWall

		addPreyAndRobotPosition(0, a, 0);

		if (random.nextDouble() < 0.5) {
			double width = 2.5 - b - 0.3;
			// env.addStaticObject(new Wall(simulator, width / 2 + 2.5 -
			// width,0, width, 0.125)); // rightWall
			env.addStaticObject(new Wall(simulator, -2.5 + width / 2, 0, width,
					0.125)); // leftWall
		}

	}

	public void addPreyAndRobotPosition(double x, double y,
			double randomYPosition) {

		env.addPrey(new IntensityPrey(simulator, "Prey " + 0, new Vector2d(x, y
				+ randomYPosition), 0, PREY_MASS, PREY_RADIUS,
				randomIntensity()));

		env.getRobots().get(0)
				.setPosition(new Vector2d(x, -y + randomYPosition));

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
			else if (prey.getIntensity() < 9)
				prey.setColor(Color.BLACK);
			else if (prey.getIntensity() < 13)
				prey.setColor(Color.GREEN.darker());
			else
				prey.setColor(Color.RED);
		}

	}

}
