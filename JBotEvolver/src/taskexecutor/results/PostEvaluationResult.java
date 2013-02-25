package taskexecutor.results;

import result.Result;

public class PostEvaluationResult extends Result {
	private int run;
	private int fitnesssample;
	private double fitness = 0;

	public PostEvaluationResult(int run, int fitnesssample, double fitness) {
		super();
		this.fitness = fitness;
		this.run = run;
		this.fitnesssample = fitnesssample;
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
}