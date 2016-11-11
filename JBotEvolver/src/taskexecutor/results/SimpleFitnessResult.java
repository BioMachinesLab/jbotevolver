package taskexecutor.results;

import result.Result;

public class SimpleFitnessResult extends Result {
	private static final long serialVersionUID = -5533742497053277839L;
	private int chromosomeId;
	private double fitness = 0;

	public SimpleFitnessResult(int taskId, int chromosomeId, double fitness) {
		super(taskId);
		this.chromosomeId = chromosomeId;
		this.fitness = fitness;
	}

	public double getFitness() {
		return fitness;
	}

	public int getChromosomeId() {
		return chromosomeId;
	}
}
