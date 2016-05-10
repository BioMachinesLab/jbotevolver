package taskexecutor;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import result.Result;
import simulation.util.Arguments;
import tasks.Task;
import coppelia.remoteApi;
import evolutionaryrobotics.JBotEvolver;

public class VREPTaskExecutor extends TaskExecutor{

	private static int DEFAULT_PORT = 19996;
	
	private String scene;
	private String ip;
	private int instances;
	private VREPContainer[] clients;
	private boolean headless;
	private Stack<VREPContainer> availableClients = new Stack<VREPContainer>();
	
	private ExecutorService executor;
	private LinkedList<Future<Result>> list = new LinkedList<Future<Result>>();
	private boolean setup = false;
	
	public VREPTaskExecutor(JBotEvolver jBotEvolver, Arguments args){
		super(jBotEvolver, args);
		this.instances = args.getArgumentAsIntOrSetDefault("instances", 1);
		this.ip = args.getArgumentAsStringOrSetDefault("ip", "127.0.0.1");
		this.scene = args.getArgumentAsStringOrSetDefault("scene", "hexa_mapelites2.ttt");
		this.headless = args.getArgumentAsIntOrSetDefault("headless", 1) == 1;
		executor = Executors.newFixedThreadPool(instances);
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec("killall vrep");
			Thread.sleep(1000);//wait to kill
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public int getInstances() {
		return instances;
	}
	
	@Override
	public void run() {
		
		try {
			
			Runtime rt = Runtime.getRuntime();
			
			clients = new VREPContainer[instances];
			
			for(int i = 0 ; i < instances ; i++) {
				String cmd = "vrep/vrep"+(i+1)+".app/Contents/MacOS/vrep "+(headless? "-h":"")+" ../../../"+scene; 
				Process pr = rt.exec(cmd);
			}
			
			Thread.sleep(5000);//wait for vrep
			
			for(int i = 0 ; i < instances ; i++) {
				clients[i] = new VREPContainer();
				clients[i].vrep = new remoteApi();
				
				if(i == 0)
					clients[i].vrep.simxFinish(-1);
				
				clients[i].ip = this.ip;
				clients[i].port = DEFAULT_PORT+i;
				clients[i].clientId = clients[i].vrep.simxStart(clients[i].ip,clients[i].port,true,false,5000,5);
				clients[i].vrep.simxStartSimulation(clients[i].clientId,clients[i].vrep.simx_opmode_oneshot);
				availableClients.push(clients[i]);
			}
			
			setup = true;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stopTasks() {
		for(VREPContainer c : clients) {
			//???
		}
		
		try {
			Runtime rt = Runtime.getRuntime();
			rt.exec("killall vrep");
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void addTask(Task t) {
		while(!setup) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		synchronized(list) {
			Future<Result> submit = executor.submit(new VREPJBotCallable(t));
			list.add(submit);
		}
	}

	@Override
	public Result getResult() {
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
	
	public class VREPContainer {
		remoteApi vrep;
		int clientId;
		String ip;
		int port;
	}
	
	private class VREPJBotCallable implements Callable<Result> {
		
		private Task t;
		
		public VREPJBotCallable(Task t) {
			this.t = t;
		}

		@Override
		public Result call() throws Exception {
			
			VREPContainer container = null;
			
			synchronized(availableClients) {
				
				while(availableClients.size()==0) {
					System.out.println("Size: "+availableClients.size());
					availableClients.wait();
				}
				
				//get the VREP conn
				container = availableClients.pop();
			}
			
			VREPTask vt = (VREPTask)t;
			vt.setVREP(container);
			
			t.run();

			Result r = t.getResult();
			
			synchronized(availableClients) {	
				//return the VREP conn
				availableClients.push(container);
				availableClients.notify();
			}
			return r;
		}
	}
}