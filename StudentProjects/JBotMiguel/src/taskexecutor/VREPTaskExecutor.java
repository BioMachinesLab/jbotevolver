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
	
	private String remoteIps[];
	private int remoteInstances[];
	
	public VREPTaskExecutor(JBotEvolver jBotEvolver, Arguments args){
		super(jBotEvolver, args);
		this.instances = args.getArgumentAsIntOrSetDefault("instances", 1);
		this.ip = args.getArgumentAsStringOrSetDefault("ip", "127.0.0.1");
		this.scene = args.getArgumentAsStringOrSetDefault("scene", "hexa_mapelites2.ttt");
		this.headless = args.getArgumentAsIntOrSetDefault("headless", 1) == 1;
		
		if(args.getArgumentIsDefined("remote")) {
			
			String arg = args.getArgumentAsString("remote");
			String[] split = arg.split(",");
			
			remoteIps = new String[split.length/2];
			remoteInstances = new int[split.length/2];
			
			int index = 0;
			instances = 0;
			
			for(int i = 0 ; i < split.length ; ) {
				remoteIps[index] = split[i++];
				remoteInstances[index] = Integer.parseInt(split[i++]);
				instances+=remoteInstances[index];
				index++;
			}
			
		}
		
		executor = Executors.newFixedThreadPool(instances);
		
		if(remoteIps == null){
			try {
				Runtime rt = Runtime.getRuntime();
				rt.exec("killall vrep");
				Thread.sleep(1000);//wait to kill
			} catch(Exception e){
				e.printStackTrace();
			}
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
			
			if(remoteInstances != null) {
				
				for(int r = 0 ; r < remoteIps.length ; r++) {
					for(int i = 0 ; i < remoteInstances[r] ; i++) {
						clients[i] = initContainer(remoteIps[r],DEFAULT_PORT+i,false);
					}
				}
				
			} else {
			
				for(int i = 0 ; i < instances ; i++) {
					String cmd = "vrep/vrep"+(i+1)+".app/Contents/MacOS/vrep "+(headless? "-h":"")+" ../../../"+scene; 
					Process pr = rt.exec(cmd);
				}
				
				Thread.sleep(5000);//wait for vrep
				
				for(int i = 0 ; i < instances ; i++) {
					clients[i] = initContainer(this.ip,DEFAULT_PORT+i, i==0);
				}
			}
			
			setup = true;
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected VREPContainer initContainer(String ip, int port, boolean kill) {
		VREPContainer c = new VREPContainer();
		c.vrep = new remoteApi();
		
		if(kill)
			c.vrep.simxFinish(-1);
		
		c.ip = ip;
		c.port = port;
		c.clientId = c.vrep.simxStart(c.ip,c.port,true,true,5000,5);
		c.vrep.simxClearStringSignal(c.clientId, "toClient", remoteApi.simx_opmode_oneshot);
		availableClients.push(c);
		
		return c;
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
		
		while(obj == null) {
			
			Future<Result> callable = null;
		
			synchronized(list) {
				while(callable == null) {
					callable = list.pop();
				}
			}
			try {
				obj = callable.get();
			} catch(Exception e) {e.printStackTrace();}
		
		}
		
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
					availableClients.wait();
				}
				
				//get the VREP conn
				container = availableClients.pop();
			}
			
			VREPTask vt = (VREPTask)t;
			vt.setVREP(container);
			
			t.run();

			Result r = t.getResult();
			
			VREPResult vrepR = (VREPResult)r;
			
			synchronized(availableClients) {	
				//return the VREP conn
				if(vrepR.getValues() != null) {
					availableClients.push(container);
					availableClients.notify();
				} else {
					//we lost a worker, resubmit task and notify others
					addTask(this.t);
					instances--;
					availableClients.notify();
					return null;
				}
			}
			return r;
		}
	}
}