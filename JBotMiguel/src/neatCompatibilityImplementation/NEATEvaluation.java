package neatCompatibilityImplementation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.encog.ml.CalculateScore;
import org.encog.neural.neat.NEATNetwork;

import simulation.Simulator;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public abstract class NEATEvaluation extends EvaluationFunction implements CalculateScore, Serializable {
	
	protected ControllersStatistics<NEATNetwork> statsManager;
	
	public NEATEvaluation(Arguments args) {
		super(args);
		statsManager = new ControllersStatistics<NEATNetwork>();
	}

	@Override
	public void update(Simulator simulator) {}
	
	public Map<NEATNetwork, TaskStatistics> getObjectivesStatistics() {
		return this.statsManager.getObjectivesStatistics();
	}
	
	public void resetStatistics() {
		this.statsManager.resetStatistics();
	}
	
	public abstract void setupObjectives(HashMap<String, Arguments> arguments);
	
	public abstract void setupEvolution(JBotEvolver jBotEvolver, 
			TaskExecutor taskExecutor, int numberOfSamples, long generationalSeed);
	
	public abstract String getObjectiveKey(int id);

}