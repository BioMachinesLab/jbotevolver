package evolutionaryrobotics.evolution;

import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;

/**
 * This dummy evolution allows you to just see
 * the results of a previously evolved population.
 * @author miguelduarte
 */
public class ViewEvolution extends Evolution {
	
	public ViewEvolution(JBotEvolver jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
	}
	
	@Override
	public void executeEvolution() {
		
	}
}