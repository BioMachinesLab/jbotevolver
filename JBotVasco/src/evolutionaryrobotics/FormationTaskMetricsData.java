package evolutionaryrobotics;

import java.util.List;

public class FormationTaskMetricsData extends MetricsData {
	private double timeInside_min = 0, timeInside_avg = 0, timeInside_max = 0;
	private double timeFirstTotalOccup = 0;
	private double numberDiffSpotsOccupied_min = 0, numberDiffSpotsOccupied_avg = 0, numberDiffSpotsOccupied_max = 0;
	private double reocupationTime_min = 0, reocupationTime_avg = 0, reocupationTime_max = 0;

	public FormationTaskMetricsData(int generation) {
		super(generation);
	}

	public FormationTaskMetricsData() {
		super(-1);
	}

	// Setters
	public void setTimeInside_min(double timeInside_min) {
		this.timeInside_min = timeInside_min;
	}

	public void setTimeInside_avg(double timeInside_avg) {
		this.timeInside_avg = timeInside_avg;
	}

	public void setTimeInside_max(double timeInside_max) {
		this.timeInside_max = timeInside_max;
	}

	public void setTimeFirstTotalOccup(double timeFirstTotalOccup) {
		this.timeFirstTotalOccup = timeFirstTotalOccup;
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

	// Getters
	public double getTimeInside_min() {
		return timeInside_min;
	}

	public double getTimeInside_avg() {
		return timeInside_avg;
	}

	public double getTimeInside_max() {
		return timeInside_max;
	}

	public double getTimeFirstTotalOccup() {
		return timeFirstTotalOccup;
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FormationTaskMetricsData) {
			FormationTaskMetricsData data = (FormationTaskMetricsData) obj;
			return data.getGeneration() == getGeneration() && data.getTimeInside_min() == timeInside_min
					&& data.getTimeInside_avg() == timeInside_avg && data.getTimeInside_max() == timeInside_max
					&& data.getTimeFirstTotalOccup() == timeFirstTotalOccup
					&& data.getNumberDiffSpotsOccupied_min() == numberDiffSpotsOccupied_min
					&& data.getNumberDiffSpotsOccupied_avg() == numberDiffSpotsOccupied_avg
					&& data.getNumberDiffSpotsOccupied_max() == numberDiffSpotsOccupied_max
					&& data.getReocupationTime_min() == reocupationTime_min
					&& data.getReocupationTime_avg() == reocupationTime_avg
					&& data.getReocupationTime_max() == reocupationTime_max;
		} else {
			return false;
		}
	}

	@Override
	public void combineMetricsData(List<MetricsData> metricsData) {
		double sum_timeInside_avg = timeInside_avg;
		double sum_timeFirstTotalOccup = timeFirstTotalOccup;
		double sum_numberDiffSpotsOccupied_avg = numberDiffSpotsOccupied_avg;
		double sum_reocupationTime_avg = reocupationTime_avg;

		double metricsCount = 1;
		for (MetricsData m : metricsData) {
			if (m instanceof FormationTaskMetricsData) {
				FormationTaskMetricsData formData = (FormationTaskMetricsData) m;
				// Minimums calculations
				if (formData.getTimeInside_min() < timeInside_min) {
					timeInside_min = formData.getTimeInside_min();
				}
				if (formData.getNumberDiffSpotsOccupied_min() < numberDiffSpotsOccupied_min) {
					numberDiffSpotsOccupied_min = formData.getNumberDiffSpotsOccupied_min();
				}
				if (formData.getReocupationTime_min() < reocupationTime_min) {
					reocupationTime_min = formData.getReocupationTime_min();
				}

				// Averages calculations
				sum_timeInside_avg += formData.getTimeInside_avg();
				sum_timeFirstTotalOccup += formData.timeFirstTotalOccup;
				sum_numberDiffSpotsOccupied_avg += formData.getNumberDiffSpotsOccupied_avg();
				sum_reocupationTime_avg += formData.getReocupationTime_avg();

				// Maximums calculations
				if (formData.getTimeInside_max() > timeInside_max) {
					timeInside_max = formData.getTimeInside_max();
				}
				if (formData.getNumberDiffSpotsOccupied_max() > numberDiffSpotsOccupied_max) {
					numberDiffSpotsOccupied_max = formData.getNumberDiffSpotsOccupied_max();
				}
				if (formData.getReocupationTime_max() > reocupationTime_max) {
					reocupationTime_max = formData.getReocupationTime_max();
				}

				metricsCount++;
			}
		}

		timeInside_avg = sum_timeInside_avg / metricsCount;
		timeFirstTotalOccup = sum_timeFirstTotalOccup / metricsCount;
		numberDiffSpotsOccupied_avg = sum_numberDiffSpotsOccupied_avg / metricsCount;
		reocupationTime_avg = sum_reocupationTime_avg / metricsCount;
	}
}
