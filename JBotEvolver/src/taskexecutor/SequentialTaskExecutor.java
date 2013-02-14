package taskexecutor;

import java.util.LinkedList;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.MainExecutor;
import result.Result;
import simulation.util.Arguments;
import tasks.Task;

public class SequentialTaskExecutor extends TaskExecutor {

	private LinkedList<Task> tasksToDo = new LinkedList<Task>();
	private LinkedList<Task> tasksDone = new LinkedList<Task>();

	public SequentialTaskExecutor(MainExecutor executor,
			JBotEvolver jBotEvolver, Arguments args) {
		super(executor, jBotEvolver, args);
	}

	@Override
	public void addTask(Task t) {
		synchronized (tasksToDo) {
			tasksToDo.add(t);
		}
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public Result getResult() {
		synchronized (this) {
			while (tasksDone.isEmpty()) {
				try {
					wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		Task t;
		synchronized (tasksDone) {
			t = tasksDone.pollFirst();
		}
		return t.getResult();
	}

	@Override
	public void run() {

		while (true) {
			try {
				synchronized (this) {
					while (tasksToDo.isEmpty())
						wait();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			Task t;

			synchronized (tasksToDo) {
				t = tasksToDo.pollFirst();
			}

			t.run();

			synchronized (this) {
				tasksDone.add(t);
				notifyAll();
			}
		}
	}
}