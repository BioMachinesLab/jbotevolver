package taskexecutor.results;

import result.Result;

public class NEATPostEvaluationResult extends Result {
	private int run;
	private int generation;
	private int fitnesssample;
	private int sample = 0;
	private double fitness = 0;

	public NEATPostEvaluationResult(int run, int generation, int fitnesssample, double fitness) {
		super();
		this.generation = generation;
		this.fitness = fitness;
		this.run = run;
		this.fitnesssample = fitnesssample;
	}
	
	public NEATPostEvaluationResult(int run, int generation, int fitnesssample, double fitness, int sample) {
		this(run,generation,fitnesssample,fitness);
		this.sample = sample;
	}

	public double getFitness() {
		return fitness;
	}

	public int getFitnesssample() {
		return fitnesssample;
	}
	
	public int getRun() {
		return run;
	}
	
	public int getGeneration() {
		return generation;
	}
	
	public int getSample() {
		return sample;
	}
}
