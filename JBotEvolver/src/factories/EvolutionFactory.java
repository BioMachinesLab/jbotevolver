package factories;

import java.lang.reflect.Constructor;
import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.populations.Population;
import factories.Factory;

public class EvolutionFactory extends Factory {
	
	private Population population;

	public EvolutionFactory(Simulator simulator, Population population) {
		super(simulator);
		this.population = population;
	}

	public Evolution getEvolution(Arguments args) {
		if (!args.getArgumentIsDefined("name"))
			throw new RuntimeException("Evolution 'name' not defined: "+args.toString());

		String evolutionName = args.getArgumentAsString("name");

		try {
			Constructor<?>[] constructors = Class.forName(evolutionName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 3 && params[0] == Simulator.class
						&& params[1] == Population.class && params[2] == Arguments.class) {
					return (Evolution) constructor.newInstance(simulator,population,args);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		throw new RuntimeException("Unknown evolution: " + evolutionName);
	}
}