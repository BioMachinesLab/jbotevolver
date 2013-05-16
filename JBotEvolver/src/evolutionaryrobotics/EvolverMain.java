package evolutionaryrobotics;

import taskexecutor.TaskExecutor;
import evolutionaryrobotics.evolution.Evolution;

public class EvolverMain {

	public EvolverMain(String[] args) throws Exception {
		
		JBotEvolver jBotEvolver = new JBotEvolver(args);
		TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
		taskExecutor.start();
		Evolution evo = Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
		evo.executeEvolution();
		taskExecutor.stopTasks();
	}
}