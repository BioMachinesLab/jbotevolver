package taskexecutor;

import java.util.LinkedList;

import simulation.util.Arguments;
import evolutionaryrobotics.Task;

public class SequentialTaskExecutor extends TaskExecutor {
	
	private LinkedList<Task> tasksToDo = new LinkedList<Task>();
	private LinkedList<Task> tasksDone = new LinkedList<Task>();
	
	public SequentialTaskExecutor(Arguments args) {
		super(args);
	}

	@Override
	public void addTask(Task t) {
		tasksToDo.add(t);
		notifyAll();
	}

	@Override
	public synchronized Object getResult() {
		while(tasksDone.isEmpty()) {
			try {
				wait();
			} catch(Exception e){e.printStackTrace();}
		}
		
		Task t = tasksDone.pollFirst();
		return t.getResult();
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				while(tasksToDo.isEmpty())
					wait();
			}catch(Exception e) {e.printStackTrace();}
			
			Task t = tasksToDo.pollFirst();
			t.run();
			tasksDone.add(t);
		}
	}
}