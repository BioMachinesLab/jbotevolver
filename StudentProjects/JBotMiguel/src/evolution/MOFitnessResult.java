package evolution;

import result.Result;

public class MOFitnessResult extends Result {
	
	private int chromosomeId;
	private double fitness = 0;
	private String objective;
	
	public MOFitnessResult(int chromosomeId, double fitness, String objective) {
		super();
		this.chromosomeId = chromosomeId;
		this.fitness = fitness;
		this.objective = objective;
	}
	
	public String getObjective() {
		return objective;
	}

	public double getFitness() {
		return fitness;
	}

	public int getChromosomeId() {
		return chromosomeId;
	}
}