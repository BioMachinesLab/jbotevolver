package taskexecutor.results;

import result.Result;

public class SimpleCoEvolutionFitnessResult extends Result {
	private int chromosomeIdA;
	private int chromosomeIdB;
	private double fitness = 0;
	
	
	public SimpleCoEvolutionFitnessResult(int chromosomeIdA, int chromosomeIdB, double fitness) {
		super();
		this.chromosomeIdA = chromosomeIdA;
		this.chromosomeIdB = chromosomeIdB;
		this.fitness = fitness;
	}

	public int getChromosomeIdA() {
		return chromosomeIdA;
	}
	
	public int getChromosomeIdB() {
		return chromosomeIdB;
	}
	
	public double getFitness() {
		return fitness;
	}
	
}
