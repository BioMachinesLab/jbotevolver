package taskexecutor;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import evolutionaryrobotics.JBotEvolver;
import result.Result;
import simulation.util.Arguments;
import tasks.Task;

public class ParallelTaskExecutor extends TaskExecutor {
	
	private ExecutorService executor;
	private LinkedList<Future<Result>> list = new LinkedList<Future<Result>>();
	
	public ParallelTaskExecutor(JBotEvolver jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
		int numberThreads = args.getArgumentAsIntOrSetDefault("threads", Runtime.getRuntime().availableProcessors());
		executor = Executors.newFixedThreadPool(numberThreads);
	}

	@Override
	public void addTask(Task t) {
		synchronized(list) {
			Future<Result> submit = executor.submit(new JBotCallable(t));
			list.add(submit);
		}
	}

	@Override
	public synchronized Result getResult() {
		
		Result obj = null;
		Future<Result> callable;
		
		synchronized(list) {
			callable = list.pop();
		}
		try {
			obj = callable.get();
		} catch(Exception e) {e.printStackTrace();}
		return obj;
	}

	@Override
	public void run() {}
	
	@Override
	public void stopTasks() {
		super.stopTasks();
		executor.shutdownNow();
	}
	
	private class JBotCallable implements Callable<Result> {
		
		private Task t;
		
		public JBotCallable(Task t) {
			this.t = t;
		}

		@Override
		public Result call() throws Exception {
			t.run();
			return t.getResult();
		}
	}
}