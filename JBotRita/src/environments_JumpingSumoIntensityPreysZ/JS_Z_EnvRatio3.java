package environments_JumpingSumoIntensityPreysZ;

import java.awt.Color;

import mathutils.Vector2d;
import physicalobjects.IntensityPrey;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.Prey;
import simulation.physicalobjects.Wall;
import simulation.util.Arguments;
import environments_JumpingSumoIntensityPreys.JS_Environment;

public class JS_Z_EnvRatio3 extends JS_Environment {

	private static final double MAX_WIDTH_LIMIT_FOR_WALL = 2.4;
	private static final double MIN_WIDTH_LIMIT_FOR_WALL = 1.5;

	private Environment env;
	private double ratio1 = 6;
	private double ratio2 = 8;

	public JS_Z_EnvRatio3(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);

	}

	public void setup(Environment env, Simulator simulator, double ratio1,
			double ratio2) { // use this for LoopsOfEnvironmnt
		super.setup(simulator);
		this.env = env;
		this.ratio1 = ratio1;
		this.ratio2 = ratio2;
		init();
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		env = this;
		init();

	}

	public void init() {
		env.addStaticObject(new Wall(simulator, 0, 2.5, 5, 0.125,0.2)); // HorizontalWallNorth
		env.addStaticObject(new Wall(simulator, 2.5, 0, 0.125, 5,0.2)); // VerticalEast
		env.addStaticObject(new Wall(simulator, 0, -2.5, 5, 0.125,0.2)); // HorizontalSouth
		env.addStaticObject(new Wall(simulator, -2.5, 0, 0.125, 5,0.2)); // VerticalWest

		double b = random.nextDouble()
				* (MAX_WIDTH_LIMIT_FOR_WALL - MIN_WIDTH_LIMIT_FOR_WALL)
				+ MIN_WIDTH_LIMIT_FOR_WALL;

		double a = 6 * b / 8; // ratio 3
		double novoB = b - 0.7;

		double novoA = a - 6 * novoB / 8;

		novoB = b - 0.7 - 0.2;

		env.addStaticObject(new Wall(simulator, -(2.5 - (novoB + 2.5) / 2),
				-novoA, novoB + 2.5, 0.125)); // centerWall

		env.addStaticObject(new Wall(simulator, 2.5 - (novoB + 2.5) / 2, novoA,
				novoB + 2.5, 0.125)); // centerWall
		
		addPreyAndRobotPosition(0, a, 0);

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
				preyEated = prey;
				change_PreyInitialDistance = true;
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
