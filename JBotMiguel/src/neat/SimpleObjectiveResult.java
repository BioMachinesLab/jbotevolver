package neat;

import result.Result;

public class SimpleObjectiveResult extends Result {

	protected int resultId;
	protected int resultSample;
	protected double fitness;
	protected long threadId;
	
	public SimpleObjectiveResult(int resultId, int resultSample, double fitness, long threadId) {
		super();
		this.resultId = resultId;
		this.resultSample = resultSample;
		this.fitness = fitness;
		this.threadId = threadId;
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
	
	public long getThreadId() {
		return threadId;
	}
}
