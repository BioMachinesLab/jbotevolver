package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.environment.RoundForageEnvironment;
import simulation.util.Arguments;

public class EnvironmentFactory extends Factory implements Serializable {

	public static Environment getEnvironment(Simulator simulator, Arguments arguments) {

		if (!arguments.getArgumentIsDefined("classname")) {
			throw new RuntimeException("Environment 'classname' not defined: "
					+ arguments.toString());
		}

		String environmentName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(environmentName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == Simulator.class
						&& params[1] == Arguments.class) {
					return (Environment) constructor.newInstance(simulator, arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		throw new RuntimeException("Unknown environment: " + environmentName);
	}
}