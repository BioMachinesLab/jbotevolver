package taskexecutor;

import java.util.LinkedList;
import evolutionaryrobotics.JBotEvolver;
import result.Result;
import simulation.util.Arguments;
import tasks.Task;

public class SequentialTaskExecutor extends TaskExecutor {

	private LinkedList<Task> tasksToDo = new LinkedList<Task>();
	private LinkedList<Task> tasksDone = new LinkedList<Task>();

	public SequentialTaskExecutor(JBotEvolver jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
	}

	@Override
	public void addTask(Task t) {
		synchronized (tasksToDo) {
			tasksToDo.add(t);
			tasksToDo.notifyAll();
		}
	}

	@Override
	public Result getResult() {
		synchronized (tasksDone) {
			while (tasksDone.isEmpty()) {
				try {
					tasksDone.wait();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			Task t = tasksDone.pollFirst();
			return t.getResult();
		}
	}

	@Override
	public void run() {

		while (true) {
			Task t;
			try {
				synchronized (tasksToDo) {
					while (tasksToDo.isEmpty()) {
						tasksToDo.wait();
					}
					t = tasksToDo.pollFirst();
				}
				t.run();
				synchronized (tasksDone) {
					tasksDone.add(t);
					tasksDone.notifyAll();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}