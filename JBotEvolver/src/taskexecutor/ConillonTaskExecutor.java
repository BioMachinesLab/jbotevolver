package taskexecutor;

import java.util.ArrayList;
import java.util.HashMap;

import result.Result;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import tasks.Task;
import client.Client;
import net.jafama.FastMath;
import comm.ClientPriority;
import evolutionaryrobotics.JBotEvolver;

public class ConillonTaskExecutor extends TaskExecutor {

	private Client client;
	private JBotEvolver jBotEvolver;
	private Arguments args;
	private boolean connected = false;

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
		FastMath.sinQuick(1);//Make sure that Jafama has been initialized
	}

	private void connect() {
		ClientPriority priority = getPriority(args
				.getArgumentAsIntOrSetDefault("priority", 10));

		serverPort = args.getArgumentAsIntOrSetDefault("serverport", 0);
		codePort = args.getArgumentAsIntOrSetDefault("codeport", 0);
		serverName = args.getArgumentAsStringOrSetDefault("server",
				"evolve.dcti.iscte.pt");
		
		String desc = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
		
		int totalTasks = args.getArgumentAsIntOrSetDefault("totaltasks",0);
		if(totalTasks > 0)
			client = new Client(desc,priority, serverName, serverPort, serverName, codePort, totalTasks);
		else
			client = new Client(desc,priority, serverName, serverPort, serverName, codePort);
		connected = true;
		//prepareArguments(jBotEvolver.getArguments());
	}

	@Override
	public void prepareArguments(HashMap<String, Arguments> arguments) {
		
		if(!connected)
			connect();

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
			//args.setArgument(name, completeArgs);
		}
	}
	
	@Override
	public void addTask(Task t) {
		if(!connected)
			connect();
		client.commit(t);
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
		}
	}

	@Override
	public Result getResult() {
		if(connected)
			return client.getNextResult();
		return null;
	}
	
	private ClientPriority getPriority(int priority) {
		return (priority < 2) ? ClientPriority.VERY_HIGH
				: (priority < 4) ? ClientPriority.HIGH
						: (priority < 7) ? ClientPriority.NORMAL
								: ClientPriority.LOW;
	}
}