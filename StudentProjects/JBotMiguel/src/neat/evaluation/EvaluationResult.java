package neat.evaluation;

public class EvaluationResult {
	
	private int evalId;
	private double fitness;
	
	public EvaluationResult(int evalId, double fitness) {
		this.evalId = evalId;
		this.fitness = fitness;
	}
	
	public int getEvalId() {
		return evalId;
	}
	
	public double getFitness() {
		return fitness;
	}
}
