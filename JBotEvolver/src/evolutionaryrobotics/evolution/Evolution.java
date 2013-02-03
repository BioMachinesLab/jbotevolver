package evolutionaryrobotics.evolution;

import evolutionaryrobotics.populations.Population;
import simulation.Simulator;
import simulation.util.Arguments;

public abstract class Evolution {
	
	protected Simulator simulator;
	protected Population population;
	
	public Evolution(Simulator simulator, Population population, Arguments args) {
		this.simulator = simulator;
		this.population = population;
	}
	
	public abstract void executeEvolution();
}