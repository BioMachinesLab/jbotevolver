package evolutionaryrobotics.evolution;

import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;

public abstract class Evolution {
	
	protected JBotEvolver jBotEvolver;
	
	public Evolution(JBotEvolver jBotEvolver, Arguments args) {
		this.jBotEvolver = jBotEvolver;
	}
	
	public abstract void executeEvolution();
}