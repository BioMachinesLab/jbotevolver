package taskexecutor;

import java.lang.reflect.Constructor;

import result.Result;
import simulation.util.Arguments;
import tasks.Task;

public abstract class TaskExecutor extends Thread {
	
	public TaskExecutor(Arguments args) {}
	
	public abstract void addTask(Task t);
	public abstract Result getResult();
	public abstract void run();

	public static TaskExecutor getTaskExecutor(Arguments arguments) {
		if (!arguments.getArgumentIsDefined("name"))
			throw new RuntimeException("Evolution 'name' not defined: "+arguments.toString());

		String executorName = arguments.getArgumentAsString("name");

		try {
			Constructor<?>[] constructors = Class.forName(executorName).getDeclaredConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 1 && params[0] == Arguments.class) {
					return (TaskExecutor) constructor.newInstance(arguments);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		throw new RuntimeException("Unknown TaskExecutor: " + executorName);
	}
}