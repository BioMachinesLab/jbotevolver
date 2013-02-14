package taskexecutor;

import java.util.HashMap;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.MainExecutor;
import result.Result;
import simulation.util.Arguments;
import simulation.util.Factory;
import tasks.Task;

public abstract class TaskExecutor extends Thread {

	public TaskExecutor(MainExecutor executor, JBotEvolver jBotEvolver,
			Arguments args) {
	}

	public abstract void addTask(Task t);

	public abstract Result getResult();

	public void run() {
	}

	public void stopTasks() {
	}

	public static TaskExecutor getTaskExecutor(MainExecutor executor,
			JBotEvolver jBotEvolver, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evolution 'name' not defined: "
					+ arguments.toString());

		return (TaskExecutor) Factory.getInstance(
				arguments.getArgumentAsString("classname"), executor,
				jBotEvolver, arguments);
	}

	public void prepareArguments(HashMap<String, Arguments> arguments) {
		
	}
}