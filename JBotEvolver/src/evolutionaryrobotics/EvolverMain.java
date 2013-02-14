package evolutionaryrobotics;

import java.util.HashMap;

import result.Result;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.tasks.GenerationalTask;
import tasks.Task;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.util.DiskStorage;

public class EvolverMain implements MainExecutor {
	private DiskStorage diskStorage;
	private TaskExecutor taskExecutor;
	private HashMap<String, Arguments> arguments;
	private JBotEvolver jBot;

	public EvolverMain(String[] args) throws Exception {
		jBot = new JBotEvolver(args);
		arguments = jBot.getArguments();
		getEvolution().executeEvolution();

	}

	public Evolution getEvolution() {
		if (arguments.get("--executor") != null) {
			taskExecutor = TaskExecutor.getTaskExecutor(this, jBot,
					arguments.get("--executor"));
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
		return Evolution.getEvolution(this, jBot, arguments.get("--evolution"));
	}

	@Override
	public void submitTask(Task task) {
		taskExecutor.addTask(task);
	}

	@Override
	public Result getResult() {
		return taskExecutor.getResult();
	}

	public void evolutionFinished() {
		taskExecutor.stopTasks();
	}

	@Override
	public DiskStorage getDiskStorage() {
		return diskStorage;
	}

	public static void main(String[] args) throws Exception {

		new EvolverMain(args);

	}

	@Override
	public void prepareArguments(HashMap<String, Arguments> arguments) {
		taskExecutor.prepareArguments(arguments);

	}

}