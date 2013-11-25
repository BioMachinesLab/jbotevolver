package neatCompatibilityImplementation;

import result.Result;

public class SimpleObjectiveResult extends Result {

	protected int resultId;
	protected int resultSample;
	protected double fitness;
	
	public SimpleObjectiveResult(int resultId, int resultSample, double fitness) {
		super();
		this.resultId = resultId;
		this.resultSample = resultSample;
		this.fitness = fitness;
	}

	public double getFitness() {
		return fitness;
	}

	public int getResultSample(){
		return this.resultSample;
	}

	public int getResultId(){
		return this.resultId;
	}
}
