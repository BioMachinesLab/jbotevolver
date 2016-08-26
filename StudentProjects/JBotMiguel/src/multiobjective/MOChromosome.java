package multiobjective;

import java.io.Serializable;

import novelty.EvaluationResult;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class MOChromosome extends Chromosome implements Serializable{
	private static final long serialVersionUID = -4153621520941047445L;
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
