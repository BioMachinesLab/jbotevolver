package evolutionaryrobotics.evolution;

import simulation.util.Arguments;
import simulation.util.Factory;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;

public abstract class Evolution {

	protected JBotEvolver jBotEvolver;
	protected TaskExecutor taskExecutor;

	public Evolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		this.taskExecutor = taskExecutor;
		this.jBotEvolver = jBotEvolver;
	}

	public abstract void executeEvolution();
	
	public static Evolution getEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evolution 'classname' not defined: "
					+ arguments.toString());

		return (Evolution) Factory.getInstance(
				arguments.getArgumentAsString("classname"), jBotEvolver, taskExecutor, arguments);
	}
}