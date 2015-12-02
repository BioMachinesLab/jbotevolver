package multiobjective;

import novelty.EvaluationResult;
import result.Result;

public class MOFitnessResult extends Result {
	
	private int chromosomeId;
	private EvaluationResult result;
	
	public MOFitnessResult(int chromosomeId, EvaluationResult result) {
		super();
		this.chromosomeId = chromosomeId;
		this.result = result;
	}
	
	public EvaluationResult getEvaluationResult() {
		return result;
	}

	public int getChromosomeId() {
		return chromosomeId;
	}
}