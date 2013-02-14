package evolutionaryrobotics;

import java.util.HashMap;

import result.Result;
import simulation.util.Arguments;
import tasks.Task;
import evolutionaryrobotics.util.DiskStorage;

public interface MainExecutor {

	public void submitTask(Task task);

	public Result getResult();

	public DiskStorage getDiskStorage();

	public void prepareArguments(HashMap<String, Arguments> arguments);

}
