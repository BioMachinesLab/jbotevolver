package factories;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import experiments.Experiment;

public class EvaluationFunctionFactory extends Factory implements Serializable {

	public EvaluationFunctionFactory(Simulator simulator) {
		super(simulator);
	}

	public EvaluationFunction getEvaluationFunction(Arguments arguments, Experiment experiment) {
		if (!arguments.getArgumentIsDefined("name"))
			throw new RuntimeException("Evaluation 'name' not defined: "+arguments.toString());

		String evaluationName = arguments.getArgumentAsString("name");

		try {
			Constructor<?>[] constructors = Class.forName(evaluationName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == Simulator.class && params[1] == Arguments.class) {
					return (EvaluationFunction) constructor.newInstance(simulator,arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown evaluation: " + evaluationName);
	}
}