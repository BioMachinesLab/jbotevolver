package neatCompatibilityImplementation;


import java.util.Random;

import org.encog.ml.MLMethod;

import result.Result;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.JBotEvolverTask;

public class SimpleProcessingTask extends JBotEvolverTask {

	private double score = 0;
	private Random random;
	protected MLMethod method;
	protected SingleObjective objective;
	
	public SimpleProcessingTask(SingleObjective objective, MLMethod method, long seed) {
		super(null);
		this.method = method;
		this.random = new Random(seed);
		this.objective = objective;
	}
	
	@Override
	public void run() {
		objective.computeObjective(method);
		score = objective.getFitness();
	}
	
	@Override
	public Result getResult() {
		SimpleObjectiveResult fr = new SimpleObjectiveResult(objective.getId(), 1, score);
		return fr;
	}
}
