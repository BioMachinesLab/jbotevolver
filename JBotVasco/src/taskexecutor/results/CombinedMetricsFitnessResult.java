package taskexecutor.results;

import evolutionaryrobotics.MetricsData;

public class CombinedMetricsFitnessResult extends SimpleFitnessResult {
	private static final long serialVersionUID = 5830382270562689069L;
	private MetricsData data;
	private int generation;

	public CombinedMetricsFitnessResult(int taskId, int chromosomeId, double fitness, int generation,
			MetricsData data) {
		super(taskId, chromosomeId, fitness);
		this.data = data;
		this.generation = generation;
	}

	public MetricsData getMetricsData() {
		return data;
	}

	public int getGeneration() {
		return generation;
	}
}
