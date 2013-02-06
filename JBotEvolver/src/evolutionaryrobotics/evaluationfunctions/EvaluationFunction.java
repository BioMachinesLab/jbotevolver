package evolutionaryrobotics.evaluationfunctions;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import simulation.Updatable;
import simulation.util.Arguments;

public abstract class EvaluationFunction implements Serializable, Updatable {
	protected double fitness;

	public EvaluationFunction(Arguments args) {}

	public double getFitness() {
		return fitness;
	}
	
	public static EvaluationFunction getEvaluationFunction(Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evaluation 'classname' not defined: "+arguments.toString());

		String evaluationName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(evaluationName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 1 && params[0] == Arguments.class) {
					return (EvaluationFunction) constructor.newInstance(arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown evaluation: " + evaluationName);
	}
}