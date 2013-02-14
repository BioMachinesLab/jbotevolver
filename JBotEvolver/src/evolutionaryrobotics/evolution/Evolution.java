package evolutionaryrobotics.evolution;

import simulation.util.Arguments;
import simulation.util.Factory;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.MainExecutor;

public abstract class Evolution {

	protected JBotEvolver jBotEvolver;
	protected MainExecutor executor;

	public Evolution(MainExecutor executor, JBotEvolver jBotEvolver,
			Arguments args) {
		this.executor = executor;
		this.jBotEvolver = jBotEvolver;
	}

	public abstract void executeEvolution();
	
	public static Evolution getEvolution(MainExecutor executor,
			JBotEvolver jBotEvolver, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evolution 'classname' not defined: "
					+ arguments.toString());

		return (Evolution) Factory.getInstance(
				arguments.getArgumentAsString("classname"), executor,
				jBotEvolver, arguments);
	}
}