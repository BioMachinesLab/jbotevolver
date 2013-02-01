package evolutionaryrobotics.parallel;

import result.Result;

public class SlaveResult extends Result {
	private int chromosomeId;
	private double fitness = 0;


	public SlaveResult(int chromosomeId, double fitness) {
		super();
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
