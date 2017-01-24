package evolutionaryrobotics;

import java.util.List;

public class FormationTaskMetricsData extends MetricsData {
	private static final long serialVersionUID = 6939285682274531099L;
	private double timeInside_min = 0, timeInside_avg = 0, timeInside_max = 0;
	private double timeFirstTotalOccup_min = 0, timeFirstTotalOccup_avg = 0, timeFirstTotalOccup_max = 0;
	private double numberDiffSpotsOccupied_min = 0, numberDiffSpotsOccupied_avg = 0, numberDiffSpotsOccupied_max = 0;
	private double reocupationTime_min = 0, reocupationTime_avg = 0, reocupationTime_max = 0;
	private boolean isFaultInjectionActive = false;

	public FormationTaskMetricsData(int generation) {
		super(generation);
	}

	public FormationTaskMetricsData() {
		super(-1);
	}

	// Setters
	@Override
	public void setGeneration(int generation) {
		this.generation = generation;
	}

	public void setTimeInside_min(double timeInside_min) {
		this.timeInside_min = timeInside_min;
	}

	public void setTimeInside_avg(double timeInside_avg) {
		this.timeInside_avg = timeInside_avg;
	}

	public void setTimeInside_max(double timeInside_max) {
		this.timeInside_max = timeInside_max;
	}

	public void setTimeFirstTotalOccup_min(double timeFirstTotalOccup_min) {
		this.timeFirstTotalOccup_min = timeFirstTotalOccup_min;
	}

	public void setTimeFirstTotalOccup_avg(double timeFirstTotalOccup_avg) {
		this.timeFirstTotalOccup_avg = timeFirstTotalOccup_avg;
	}

	public void setTimeFirstTotalOccup_max(double timeFirstTotalOccup_max) {
		this.timeFirstTotalOccup_max = timeFirstTotalOccup_max;
	}

	public void setNumberDiffSpotsOccupied_min(double numberDiffSpotsOccupied_min) {
		this.numberDiffSpotsOccupied_min = numberDiffSpotsOccupied_min;
	}

	public void setNumberDiffSpotsOccupied_avg(double numberDiffSpotsOccupied_avg) {
		this.numberDiffSpotsOccupied_avg = numberDiffSpotsOccupied_avg;
	}

	public void setNumberDiffSpotsOccupied_max(double numberDiffSpotsOccupied_max) {
		this.numberDiffSpotsOccupied_max = numberDiffSpotsOccupied_max;
	}

	public void setReocupationTime_min(double reocupationTime_min) {
		this.reocupationTime_min = reocupationTime_min;
	}

	public void setReocupationTime_avg(double reocupationTime_avg) {
		this.reocupationTime_avg = reocupationTime_avg;
	}

	public void setReocupationTime_max(double reocupationTime_max) {
		this.reocupationTime_max = reocupationTime_max;
	}

	public void setFaultInjectionActive(boolean isFaultInjectionActive) {
		this.isFaultInjectionActive = isFaultInjectionActive;
	}

	// Getters
	@Override
	public int getGeneration() {
		return generation;
	}

	public double getTimeInside_min() {
		return timeInside_min;
	}

	public double getTimeInside_avg() {
		return timeInside_avg;
	}

	public double getTimeInside_max() {
		return timeInside_max;
	}

	public double getTimeFirstTotalOccup_min() {
		return timeFirstTotalOccup_min;
	}

	public double getTimeFirstTotalOccup_avg() {
		return timeFirstTotalOccup_avg;
	}

	public double getTimeFirstTotalOccup_max() {
		return timeFirstTotalOccup_max;
	}

	public double getNumberDiffSpotsOccupied_min() {
		return numberDiffSpotsOccupied_min;
	}

	public double getNumberDiffSpotsOccupied_avg() {
		return numberDiffSpotsOccupied_avg;
	}

	public double getNumberDiffSpotsOccupied_max() {
		return numberDiffSpotsOccupied_max;
	}

	public double getReocupationTime_min() {
		return reocupationTime_min;
	}

	public double getReocupationTime_avg() {
		return reocupationTime_avg;
	}

	public double getReocupationTime_max() {
		return reocupationTime_max;
	}

	public boolean isFaultInjectionActive() {
		return isFaultInjectionActive;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormationTaskMetricsData) {
			FormationTaskMetricsData data = (FormationTaskMetricsData) obj;
			return data.getGeneration() == getGeneration() && data.getTimeInside_min() == timeInside_min
					&& data.getTimeInside_avg() == timeInside_avg && data.getTimeInside_max() == timeInside_max
					&& data.getTimeFirstTotalOccup_min() == timeFirstTotalOccup_min
					&& data.getTimeFirstTotalOccup_avg() == timeFirstTotalOccup_avg
					&& data.getTimeFirstTotalOccup_max() == timeFirstTotalOccup_max
					&& data.getNumberDiffSpotsOccupied_min() == numberDiffSpotsOccupied_min
					&& data.getNumberDiffSpotsOccupied_avg() == numberDiffSpotsOccupied_avg
					&& data.getNumberDiffSpotsOccupied_max() == numberDiffSpotsOccupied_max
					&& data.getReocupationTime_min() == reocupationTime_min
					&& data.getReocupationTime_avg() == reocupationTime_avg
					&& data.getReocupationTime_max() == reocupationTime_max
					&& data.isFaultInjectionActive() == isFaultInjectionActive;
		} else {
			return false;
		}
	}

	@Override
	public void combineMetricsData(List<MetricsData> metricsData) {
		double metricsCount = 1;
		double sum_timeInside_avg = timeInside_avg;
		double sum_timeFirstTotalOccup_avg = timeFirstTotalOccup_avg;
		double sum_numberDiffSpotsOccupied_avg = numberDiffSpotsOccupied_avg;

		double reocupationCount = 0;
		double sum_reocupationTime_avg = reocupationTime_avg;
		if (isFaultInjectionActive) {
			reocupationCount++;
		} else {
			reocupationTime_min = Double.MAX_VALUE;
			reocupationTime_avg = 0;
			reocupationTime_max = Double.MIN_VALUE;
		}

		for (MetricsData m : metricsData) {
			if (m instanceof FormationTaskMetricsData) {
				FormationTaskMetricsData formData = (FormationTaskMetricsData) m;
				// Minimums calculations
				if (formData.getTimeInside_min() < timeInside_min) {
					timeInside_min = formData.getTimeInside_min();
				}

				if (formData.getTimeFirstTotalOccup_min() < timeFirstTotalOccup_min) {
					timeFirstTotalOccup_min = formData.getTimeFirstTotalOccup_min();
				}

				if (formData.getNumberDiffSpotsOccupied_min() < numberDiffSpotsOccupied_min) {
					numberDiffSpotsOccupied_min = formData.getNumberDiffSpotsOccupied_min();
				}

				// Averages calculations
				sum_timeInside_avg += formData.getTimeInside_avg();
				sum_timeFirstTotalOccup_avg += formData.getTimeFirstTotalOccup_avg();
				sum_numberDiffSpotsOccupied_avg += formData.getNumberDiffSpotsOccupied_avg();

				// Maximums calculations
				if (formData.getTimeInside_max() > timeInside_max) {
					timeInside_max = formData.getTimeInside_max();
				}

				if (formData.getTimeFirstTotalOccup_max() > timeFirstTotalOccup_max) {
					timeFirstTotalOccup_max = formData.getTimeFirstTotalOccup_max();
				}

				if (formData.getNumberDiffSpotsOccupied_max() > numberDiffSpotsOccupied_max) {
					numberDiffSpotsOccupied_max = formData.getNumberDiffSpotsOccupied_max();
				}

				metricsCount++;

				if (formData.isFaultInjectionActive) {
					if (formData.getReocupationTime_min() < reocupationTime_min) {
						reocupationTime_min = formData.getReocupationTime_min();
					}

					if (formData.getReocupationTime_max() > reocupationTime_max) {
						reocupationTime_max = formData.getReocupationTime_max();
					}

					sum_reocupationTime_avg += formData.getReocupationTime_avg();
					reocupationCount++;
				}
			}
		}

		timeInside_avg = sum_timeInside_avg / metricsCount;
		timeFirstTotalOccup_avg = sum_timeFirstTotalOccup_avg / metricsCount;
		numberDiffSpotsOccupied_avg = sum_numberDiffSpotsOccupied_avg / metricsCount;

		if (reocupationCount > 0) {
			reocupationTime_avg = sum_reocupationTime_avg / reocupationCount;
		} else {
			reocupationTime_min = 0;
			reocupationTime_avg = 0;
			reocupationTime_max = 0;
		}
	}
}
