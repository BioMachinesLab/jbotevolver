package taskexecutor;

import java.util.ArrayList;
import java.util.HashMap;
import result.Result;
import simulation.util.Arguments;
import tasks.Task;
import client.Client;
import comm.ClientPriority;
import evolutionaryrobotics.JBotEvolver;

public class ConillonTaskExecutor extends TaskExecutor {

	private Client client;

	public ConillonTaskExecutor(JBotEvolver jBotEvolver, Arguments args) {
		super(jBotEvolver, args);

		ClientPriority priority = getPriority(args
				.getArgumentAsIntOrSetDefault("priority", 10));

		int serverPort = args.getArgumentAsIntOrSetDefault("serverport", 0);
		int codePort = args.getArgumentAsIntOrSetDefault("codeport", 0);
		String serverName = args.getArgumentAsStringOrSetDefault("server",
				"evolve.dcti.iscte.pt");

		client = new Client("desc",priority, serverName, serverPort, serverName, codePort);

		//prepareArguments(jBotEvolver.getArguments());
	}

	@Override
	public void prepareArguments(HashMap<String, Arguments> arguments) {

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
		client.commit(t);
	}

	@Override
	public void stopTasks() {
		client.cancelAllTasks();
		client.disconnect();
	}

	@Override
	public Result getResult() {
		return client.getNextResult();
	}

	private ClientPriority getPriority(int priority) {
		return (priority < 2) ? ClientPriority.VERY_HIGH
				: (priority < 4) ? ClientPriority.HIGH
						: (priority < 7) ? ClientPriority.NORMAL
								: ClientPriority.LOW;
	}
}