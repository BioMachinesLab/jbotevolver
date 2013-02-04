package taskexecutor;

import evolutionaryrobotics.Task;
import simulation.util.Arguments;

public abstract class TaskExecutor {
	
	public TaskExecutor(Arguments args) {}
	
	public abstract void addTask(Task t);
	public abstract Object getResult();
	public abstract void run();

}
