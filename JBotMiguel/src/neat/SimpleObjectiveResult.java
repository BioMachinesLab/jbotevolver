package neat;

import result.Result;

public class SimpleObjectiveResult extends Result {

	protected int resultId;
	protected int resultSample;
	protected double fitness;
	protected int netHash;
	
	public SimpleObjectiveResult(int resultId, int resultSample, double fitness, int netHash) {
		super();
		this.resultId = resultId;
		this.resultSample = resultSample;
		this.fitness = fitness;
		this.netHash = netHash;
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
	
	public int getNetHash() {
		return netHash;
	}
}
