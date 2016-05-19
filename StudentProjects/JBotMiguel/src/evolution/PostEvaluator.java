package evolution;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import simulation.util.Arguments;
import evolutionaryrobotics.populations.Population;

public abstract class PostEvaluator implements Serializable {
	
	public PostEvaluator(Arguments args) {}

    public abstract void processPopulation(Population pop);
    
    public static PostEvaluator getPostEvaluator(Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evaluation 'classname' not defined: "+arguments.toString());

		String name = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(name).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 1 && params[0] == Arguments.class) {
					return (PostEvaluator) constructor.newInstance(arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown postevaluator: " + name);
	}
    
}