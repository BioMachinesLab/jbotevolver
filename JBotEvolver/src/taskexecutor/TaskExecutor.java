package taskexecutor;

import java.lang.reflect.Constructor;

import evolutionaryrobotics.JBotEvolver;

import result.Result;
import simulation.util.Arguments;
import tasks.Task;

public abstract class TaskExecutor extends Thread {
	
	public TaskExecutor(JBotEvolver jBotEvolver, Arguments args) {}
	
	public abstract void addTask(Task t);
	public abstract Result getResult();
	public abstract void run();

	public static TaskExecutor getTaskExecutor(JBotEvolver jBotEvolver, Arguments arguments) {
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Evolution 'name' not defined: "+arguments.toString());

		String executorName = arguments.getArgumentAsString("classname");

		try {
			Constructor<?>[] constructors = Class.forName(executorName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == JBotEvolver.class && params[1] == Arguments.class) {
					return (TaskExecutor) constructor.newInstance(jBotEvolver,arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown TaskExecutor: " + executorName);
	}
}