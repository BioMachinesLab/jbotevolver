package neatCompatibilityImplementation;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.encog.ml.CalculateScore;

import evolutionaryrobotics.JBotEvolver;

import simulation.util.Arguments;
import taskexecutor.TaskExecutor;

public abstract class MOEvaluation<E> implements CalculateScore, Serializable, MOStatistics<E>{

	protected int numberOfObjectives;
	
	public MOEvaluation(Arguments args) {}
	
	public abstract void setupObjectives(HashMap<String, Arguments> arguments);
	
	public abstract void setupEvolution(JBotEvolver jBotEvolver, 
			TaskExecutor taskExecutor, int numberOfSamples);

	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}
	
	public static MOEvaluation getEvaluationFunction(Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("MOEvaluation 'classname' not defined: "+arguments.toString());

		String evaluationName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(evaluationName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 1 && params[0] == Arguments.class) {
					return (MOEvaluation) constructor.newInstance(arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown evaluation: " + evaluationName);
	}
	
	public abstract String getObjectiveKey(int id);
	
}
