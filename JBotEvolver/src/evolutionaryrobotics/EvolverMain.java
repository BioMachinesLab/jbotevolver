package evolutionaryrobotics;

import java.util.HashMap;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.util.DiskStorage;

public class EvolverMain {
	private DiskStorage diskStorage;
	private TaskExecutor taskExecutor;
	private HashMap<String, Arguments> arguments;
	private JBotEvolver jBot;

	public EvolverMain(String[] args) throws Exception {
		jBot = new JBotEvolver(args);
		arguments = jBot.getArguments();
		getEvolution().executeEvolution();
		evolutionFinished();
	}

	public Evolution getEvolution() {
		if (arguments.get("--executor") != null) {
			taskExecutor = TaskExecutor.getTaskExecutor(jBot,arguments.get("--executor"));
			taskExecutor.setDaemon(true);
			taskExecutor.start();
		}
		if (arguments.get("--output") != null) {
			diskStorage = new DiskStorage(arguments.get("--output")
					.getCompleteArgumentString());
			try {
				diskStorage.start();
				diskStorage.saveCommandlineArguments(arguments);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		return Evolution.getEvolution(jBot, taskExecutor, arguments.get("--evolution"));
	}

	public void evolutionFinished() {
		taskExecutor.stopTasks();
	}

	public static void main(String[] args) throws Exception {
		new EvolverMain(args);
	}
}