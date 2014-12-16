package taskexecutor.tasks;

import evolutionaryrobotics.JBotEvolver;
import tasks.Task;

public abstract class JBotEvolverTask extends Task {
	
	protected JBotEvolver jBotEvolver;
	
	public JBotEvolverTask(JBotEvolver jBotEvolver) {
		this.jBotEvolver = jBotEvolver;
	}
	public JBotEvolver getJBotEvolver() {
		return jBotEvolver;
	}
}
