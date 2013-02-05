package taskexecutor;

import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import simulation.util.Arguments;
import tasks.Task;

public class ParallelTaskExecutor extends TaskExecutor {
	
	private ExecutorService executor;
	private LinkedList<Future<Object>> list = new LinkedList<Future<Object>>();
	
	public ParallelTaskExecutor(Arguments args) {
		super(args);
		int numberThreads = args.getArgumentAsIntOrSetDefault("threads", Runtime.getRuntime().availableProcessors());
		executor = Executors.newFixedThreadPool(numberThreads);
	}

	@Override
	public synchronized void addTask(Task t) {
		Future<Object> submit = executor.submit(new JBotCallable(t));
	    list.add(submit);
	}

	@Override
	public synchronized Object getResult() {
		
		Object obj = null;
		
		try {
			Future<Object> callable = list.pop();
			obj = callable.get();
		} catch(Exception e) {e.printStackTrace();}
		
		return obj;
	}

	@Override
	public void run() {}
	
	private class JBotCallable implements Callable<Object> {
		
		private Task t;
		
		public JBotCallable(Task t) {
			this.t = t;
		}

		@Override
		public Object call() throws Exception {
			t.run();
			return t.getResult();
		}
	}
}