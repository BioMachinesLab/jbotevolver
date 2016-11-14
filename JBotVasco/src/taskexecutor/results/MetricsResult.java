package taskexecutor.results;

import evolutionaryrobotics.MetricsData;
import result.Result;

public class MetricsResult extends Result {
	private static final long serialVersionUID = 368640558636146682L;
	private MetricsData data;
	private int generation;
	private int chromosomeId;
	private int run;

	public MetricsResult(int taskId, int run, int chromosomeId, int generation, MetricsData data) {
		super(taskId);
		this.run = run;
		this.chromosomeId = chromosomeId;
		this.data = data;
		this.generation = generation;
	}

	public int getChromosomeId() {
		return chromosomeId;
	}

	public MetricsData getMetricsData() {
		return data;
	}

	public int getGeneration() {
		return generation;
	}

	public int getRun() {
		return run;
	}
}
