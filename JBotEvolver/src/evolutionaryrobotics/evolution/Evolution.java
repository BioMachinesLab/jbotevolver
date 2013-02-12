package evolutionaryrobotics.evolution;

import simulation.util.Arguments;
import simulation.util.Factory;
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
		
		return (Evolution)Factory.getInstance(arguments.getArgumentAsString("classname"),jBotEvolver,arguments);
	}
}