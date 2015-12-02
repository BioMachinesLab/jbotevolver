package evolutionaryrobotics.evolution;

import simulation.util.Arguments;
import simulation.util.Factory;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.populations.Population;

public abstract class Evolution {

	protected JBotEvolver jBotEvolver;
	protected TaskExecutor taskExecutor;
	protected boolean executeEvolution = true;
	protected boolean evolutionFinished = false;
	protected boolean supressMessages = false;
	

	public Evolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		this.taskExecutor = taskExecutor;
		this.jBotEvolver = jBotEvolver;
		supressMessages = args.getArgumentAsIntOrSetDefault("supressmessages", 0) == 1;
	}

	public abstract void executeEvolution();
	public abstract Population getPopulation();
	public void stopEvolution() {
		executeEvolution = false;
	}
	
	public boolean isEvolutionFinished() {
		return evolutionFinished;
	}
	
	public synchronized static Evolution getEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evolution 'classname' not defined: "
					+ arguments.toString());

		return (Evolution) Factory.getInstance(
				arguments.getArgumentAsString("classname"), jBotEvolver, taskExecutor, arguments);
	}
	
	protected void print(String s) {
		if(!supressMessages)
			System.out.print(s);
	}
}