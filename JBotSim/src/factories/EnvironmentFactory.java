package factories;

import java.io.Serializable;

import simulation.Simulator;
import simulation.environment.BeeEnvironment;
import simulation.environment.ClutteredMazeEnvironment;
import simulation.environment.DoubleLightPoleEnvironment;
import simulation.environment.Environment;
import simulation.environment.FoodWaterEnvironment;
import simulation.environment.GroupedPreyEnvironment;
import simulation.environment.LightPoleEnvironment;
import simulation.environment.MazeEnvironment;
import simulation.environment.NoLandmarksEnvironment;
import simulation.environment.RoundForageEnvironment;
import simulation.environment.TMazeEnvironment;
import simulation.environment.TwoNestCenterDeployPreysEnvironment;
import simulation.environment.TwoNestForageEnvironment;
import simulation.environment.TwoNestSimpleForageEnvironment;
import simulation.environment.TwoRoomsEnvironment;
import simulation.util.Arguments;

public class EnvironmentFactory extends Factory implements Serializable {

	public EnvironmentFactory(Simulator simulator) {
		super(simulator);
	}

	public Environment getEnvironment(Arguments arguments) {
		if (arguments == null)
			return new Environment(simulator, 10, 10);

		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("Environment 'name' not defined: "
					+ arguments.toString());
		}

		String environmentName = arguments.getArgumentAsString("name");

		if (environmentName.equalsIgnoreCase("DoubleLightPole")) {
			return new DoubleLightPoleEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("LightPole")) {
			return new LightPoleEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("RoundForage")) {
			return new RoundForageEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("TwoNestForageEnvironment")) {
			return new TwoNestForageEnvironment(simulator, arguments);
		}
		if (environmentName.equalsIgnoreCase("TwoNestSimpleForageEnvironment")) {
			return new TwoNestSimpleForageEnvironment(simulator, arguments);
		}
		if (environmentName
				.equalsIgnoreCase("TwoNestCenterDeployPreysEnvironment")) {
			return new TwoNestCenterDeployPreysEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("GroupedPrey")) {
			return new GroupedPreyEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("Bee")) {
			return new BeeEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("FoodWater")) {
			return new FoodWaterEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("NoLandmarks")) {
			return new NoLandmarksEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("Maze")) {
			return new MazeEnvironment(simulator, arguments);
		}

		if (environmentName.equalsIgnoreCase("TMaze")) {
			return new TMazeEnvironment(simulator, arguments);
		}
		
		if (environmentName.equalsIgnoreCase("ClutteredMaze")) {
			return new ClutteredMazeEnvironment(simulator, arguments);
		}
		
		if (environmentName.equalsIgnoreCase("TwoRooms")) {
			return new TwoRoomsEnvironment(simulator, arguments);
		}

		throw new RuntimeException("Unknown environment: " + environmentName);
	}

	public String getAvailableEnvironments() {
		return "DoubleLightPole, RoundForage, GroupedPrey, NoLandmarks, Maze";
	}
}
