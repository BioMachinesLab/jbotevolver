package multiobjective;

import novelty.EvaluationResult;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class MOChromosome extends Chromosome{
	
	protected EvaluationResult evaluationResult;
	
	public MOChromosome(double[] alleles, int id) {
		super(alleles,id);
	}
	
	public EvaluationResult getEvaluationResult() {
		return evaluationResult;
	}
	
	public void setEvaluationResult(EvaluationResult evaluationResult) {
		this.evaluationResult = evaluationResult;
	}
}
