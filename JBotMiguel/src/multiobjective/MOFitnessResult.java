package multiobjective;

import novelty.EvaluationResult;
import result.Result;

public class MOFitnessResult extends Result {
	
	private MOChromosome chromosome;
	private EvaluationResult result;
	
	public MOFitnessResult(int taskId, MOChromosome chromosome, EvaluationResult result) {
		super(taskId);
		this.chromosome = chromosome;
		this.result = result;
	}
	
	public EvaluationResult getEvaluationResult() {
		return result;
	}

	public int getChromosomeId() {
		return chromosome.getID();
	}
	
	public MOChromosome getChromosome() {
		return chromosome;
	}
}