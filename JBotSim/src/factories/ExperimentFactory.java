package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import simulation.Simulator;
import simulation.util.Arguments;
import experiments.CoevolutionExperiment;
import experiments.Experiment;

public class ExperimentFactory extends Factory implements Serializable {

	public ExperimentFactory(Simulator simulator) {
		super(simulator);
	}

	public Experiment getExperiment(Arguments arguments,
			Arguments environmentArguments, Arguments robotArguments,
			Arguments controllerArguments) {
		if (arguments == null) {
			return new Experiment(simulator, arguments, environmentArguments,
					robotArguments, controllerArguments);
		}

		if (!arguments.getArgumentIsDefined("name")) {
			return new Experiment(simulator, arguments, environmentArguments,
					robotArguments, controllerArguments);
		} else {
			String name = arguments.getArgumentAsString("name");

			try {
				Constructor<?>[] constructors = Class.forName(name)
						.getDeclaredConstructors();
				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					return (Experiment) constructor.newInstance(simulator,
							arguments, environmentArguments, robotArguments,
							controllerArguments);
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

				throw new RuntimeException("Unknown experiment specified: " + name);

		}
	}

	public CoevolutionExperiment getCoevolutionExperiment(
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotArguments, Arguments controllerArguments) {
		return new CoevolutionExperiment(simulator, experimentArguments,
				environmentArguments, robotArguments, controllerArguments);
	}
}
