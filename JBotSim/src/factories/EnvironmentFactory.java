package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.util.Arguments;

public class EnvironmentFactory extends Factory implements Serializable {

	public EnvironmentFactory(Simulator simulator) {
		super(simulator);
	}

	public Environment getEnvironment(Arguments arguments) {

		if (!arguments.getArgumentIsDefined("name")) {
			throw new RuntimeException("Environment 'name' not defined: "
					+ arguments.toString());
		}

		String environmentName = arguments.getArgumentAsString("name");

		try {
			Constructor<?>[] constructors = Class.forName(environmentName)
					.getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				return (Environment) constructor.newInstance(simulator,
						arguments);

			}

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		throw new RuntimeException("Unknown environment: " + environmentName);
	}

	public String getAvailableEnvironments() {
		return "DoubleLightPole, RoundForage, GroupedPrey, NoLandmarks, Maze";
	}
}
