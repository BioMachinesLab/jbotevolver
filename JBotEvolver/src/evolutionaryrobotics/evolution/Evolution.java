package evolutionaryrobotics.evolution;

import java.lang.reflect.Constructor;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;

public abstract class Evolution {
	
	protected JBotEvolver jBotEvolver;
	
	public Evolution(JBotEvolver jBotEvolver, Arguments args) {
		this.jBotEvolver = jBotEvolver;
	}
	
	public abstract void executeEvolution();

	public static Evolution getEvolution(JBotEvolver jBotEvolver, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evolution 'classname' not defined: "+arguments.toString());

		String evolutionName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(evolutionName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == JBotEvolver.class && params[1] == Arguments.class) {
					return (Evolution) constructor.newInstance(jBotEvolver,arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown evolution: " + evolutionName);
	}
}