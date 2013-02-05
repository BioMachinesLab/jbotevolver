package taskexecutor;

import comm.ClientPriority;
import simulation.util.Arguments;
import tasks.Task;
import client.Client;

public class ConillonExecutor extends TaskExecutor {
	
	private Client client;
	
	public ConillonExecutor(Arguments args) {
		super(args);
		
		ClientPriority priority = getPriority(args.getArgumentAsIntOrSetDefault("priority", 10));
		
		int serverPort = args.getArgumentAsIntOrSetDefault("serverport",0);
		int codePort = args.getArgumentAsIntOrSetDefault("codeport",0);
		String serverName = args.getArgumentAsStringOrSetDefault("server","evolve.dcti.iscte.pt");
		
		client = new Client(priority, serverName, serverPort, serverName, codePort);
	}
	
	@Override
	public void addTask(Task t) {
		client.commit(t);
	}

	@Override
	public Object getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	private ClientPriority getPriority(int priority) {
			return
				(priority < 2) ? ClientPriority.VERY_HIGH :
				(priority < 4) ? ClientPriority.HIGH :
				(priority < 7) ? ClientPriority.NORMAL :
				ClientPriority.LOW;
	}
}