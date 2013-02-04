package factories;

import java.lang.reflect.Constructor;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;

public class EvolutionFactory extends Factory {
	
	public static Evolution getEvolution(JBotEvolver jBotEvolver, Arguments args) {
		if (!args.getArgumentIsDefined("name"))
			throw new RuntimeException("Evolution 'name' not defined: "+args.toString());

		String evolutionName = args.getArgumentAsString("name");

		try {
			Constructor<?>[] constructors = Class.forName(evolutionName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == Simulator.class
						&& params[1] == Arguments.class) {
					return (Evolution) constructor.newInstance(jBotEvolver,args);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		throw new RuntimeException("Unknown evolution: " + evolutionName);
	}
}