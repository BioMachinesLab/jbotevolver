package taskexecutor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.jafama.FastMath;
import result.Result;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import taskexecutor.tasks.JBotEvolverTask;
import tasks.Task;
import client.Client;
import comm.ClientPriority;
import evolutionaryrobotics.JBotEvolver;

public class ConillonTaskExecutor extends TaskExecutor {

	private Client client;
	private JBotEvolver jBotEvolver;
	private Arguments args;
	private boolean connected = false;
	private boolean submitting = false;
	
	private ArrayList<Task> taskBuffer = new ArrayList<Task>();

	@ArgumentsAnnotation(name="serverport", defaultValue="0")
	private int serverPort;
	@ArgumentsAnnotation(name="codeport", defaultValue="0")
	private int codePort;
	@ArgumentsAnnotation(name="server", defaultValue="evolve.dcti.iscte.pt")
	private String serverName;
	
	public ConillonTaskExecutor(JBotEvolver jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
		this.jBotEvolver = jBotEvolver;
		this.args = args;
		//Make sure that Jafama has been initialized
		//so that it is not initialized every time in each worker
		FastMath.sinQuick(1);
		
		serverPort = args.getArgumentAsIntOrSetDefault("serverport", 0);
		codePort = args.getArgumentAsIntOrSetDefault("codeport", 0);
		serverName = args.getArgumentAsStringOrSetDefault("server",
				"evolve.dcti.iscte.pt");
		
	}

	private synchronized void connect() {
		
		while(!connected){
			ClientPriority priority = getPriority(args
					.getArgumentAsIntOrSetDefault("priority", 10));
	
			String desc = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
			
			int totalTasks = args.getArgumentAsIntOrSetDefault("totaltasks",0);
			
			if(client != null) {
				client.disconnect();
			}
			
			if(totalTasks > 0)
				client = new Client(desc,priority, serverName, serverPort, serverName, codePort, totalTasks);
			else
				client = new Client(desc,priority, serverName, serverPort, serverName, codePort);
			
			connected = true;
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void addTask(Task t) {
		
		if(!submitting) {
			//when we get here, the mode changed from "getting results" to "submitting", so it's
			//a brand new batch of tasks. we can clear the buffer of tasks and start adding new tasks
			submitting = true;
			taskBuffer.clear();
		}

		//the task should be added to the buffer before it is prepared
		//since the ClientId will be different if we need to reconnect to conillon
		Task copiedTask = copy(t);
		taskBuffer.add(copiedTask); 
		
		try {
			if(!connected)
				connect();
			
			//an error might have occured before we submit a task
			checkForConillonError();
			
			prepareTask(t);
			client.commit(t);
			
			//an error might have occured during the submission, and the exception
			//might have been contained in the Client code, so we need to make sure
			//everything went ok
			checkForConillonError();
		
		} catch(Exception e) {
			checkForConillonError();
		}
	}
	
	/*
	 * We need to prepare the classes for Conillon. The a package name
	 * has to be appended to the beginning of the original name. This
	 * function is fast (I timed it at 0-1ms on an i7 processor), so it
	 * shouldn't add a significant overhead. This is done on each copy
	 * of JBotEvolver that is created for each task. Alternatively, we
	 * could just do it on the constructor for the original JBotEvolver
	 * instance, but that creates problems if we want to run that instance
	 * locally (for instance, to preview the current generation in the GUI). 
	 */
	private void prepareTask(Task t) {
		
		HashMap<String, Arguments> arguments = ((JBotEvolverTask)t).getJBotEvolver().getArguments();
		
		for (String name : arguments.keySet()) {
			Arguments args = arguments.get(name);
			ArrayList<String> classNames = new ArrayList<String>();
			String completeArgs = Arguments.replaceAndGetArguments("classname",
					args.getCompleteArgumentString(), "__PLACEHOLDER__",
					classNames);
			for (int i = 0; i < classNames.size(); i++) {
				String packageName = client.getPackageName(classNames.get(i));
				classNames.set(i, packageName + classNames.get(i));
			}
			completeArgs = Arguments.repleceTagByStrings("classname",
					completeArgs, "__PLACEHOLDER__", classNames);
			arguments.put(name, new Arguments(completeArgs));
		}
	}
	
	@Override
	public void setTotalNumberOfTasks(int nTasks) {
		if(!connected)
			connect();
		client.setTotalNumberOfTasks(nTasks);
	}
	
	@Override
	public void setDescription(String desc) {
		if(!connected)
			connect();
		client.setDesc(desc);
	}

	@Override
	public void stopTasks() {
		if(connected) {
			client.cancelAllTasks();
			client.disconnect();
			connected = false;
		}
	}

	@Override
	public Result getResult() {
		
		submitting = false;
		
		if(connected) {
			if(client.getReturnedException() != null) {
				checkForConillonError();
				return getResult();
			}
			
			Result r = client.getNextResult();
			
			if(r == null && client.getReturnedException() != null) {
				checkForConillonError();
				//try again to get the result after the connection has been established 
				return getResult();
			}
			
			//we got a good result, so we should remove this task from the buffer
			//to prevent it from getting resubmitted
			removeTaskFromBuffer(r.getTaskId());
			
			return r;
		}
		return null;
	}
	
	private ClientPriority getPriority(int priority) {
		return (priority < 2) ? ClientPriority.VERY_HIGH
				: (priority < 4) ? ClientPriority.HIGH
						: (priority < 7) ? ClientPriority.NORMAL
								: ClientPriority.LOW;
	}
	
	//As we receive results from Conillon, we won't have to
	//resubmit those tasks in order to recover from an error
	private void removeTaskFromBuffer(int taskId) {
		
		Iterator<Task> i = taskBuffer.iterator();
		
		while(i.hasNext()) {
			Task t = i.next();
			if(t.getId() == taskId) {
				i.remove();
				return;
			}
		}
		
	}
	
	private Task copy(Task t) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(t);
	
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (Task) ois.readObject();
		}catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void checkForConillonError() {
		
		if(client.getReturnedException() != null) {
		
			connected = false;
			connect();
			submitting = true;
			
			Iterator<Task> i = taskBuffer.iterator();
			
			//we remove the tasks from the buffer to a temporary array
			//since they will be added again in the "addTask" method
			ArrayList<Task> tempArray = new ArrayList<Task>();
			
			while(i.hasNext()) {
				Task t = i.next();
				i.remove();
				tempArray.add(t);
			}
			
			//resubmit tasks that were lost when Conillon went down
			for(Task t : tempArray)
				addTask(t);
		}
	}
	
}