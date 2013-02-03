package evolutionaryrobotics.evolution;

import evolutionaryrobotics.populations.Population;
import simulation.Simulator;
import simulation.util.Arguments;

/**
 * This dummy evolution allows you to just see
 * the results of a previously evolved population.
 * @author miguelduarte
 */
public class ViewOnlyEvolution extends Evolution {
	
	public ViewOnlyEvolution(Simulator simulator, Population population, Arguments args) {
		super(simulator, population, args);
	}
	
	@Override
	public void executeEvolution() {
		
	}
}