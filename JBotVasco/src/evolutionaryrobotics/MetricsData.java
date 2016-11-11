package evolutionaryrobotics;

public class MetricsData {
	private double timeInside_min = -1;
	private double timeInside_avg = -1;
	private double timeInside_max = -1;

	private int timeFirstTotalOccup = -1;

	private double numberDiffSpotsOccupied_min = -1;
	private double numberDiffSpotsOccupied_avg = -1;
	private double numberDiffSpotsOccupied_max = -1;

	private int reocupationTime = -1;
	private int generation = -1;

	public MetricsData(int generation) {
		this.generation = generation;
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

	public void setTimeFirstTotalOccup(int timeFirstTotalOccup) {
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

	public void setReocupationTime(int reocupationTime) {
		this.reocupationTime = reocupationTime;
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

	public int getTimeFirstTotalOccup() {
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

	public int getReocupationTime() {
		return reocupationTime;
	}

	public int getGeneration() {
		return generation;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MetricsData) {
			MetricsData data = (MetricsData) obj;
			return data.getTimeInside_min() == timeInside_min && data.getTimeInside_avg() == timeInside_avg
					&& data.getTimeInside_max() == timeInside_max
					&& data.getTimeFirstTotalOccup() == timeFirstTotalOccup
					&& data.getNumberDiffSpotsOccupied_min() == numberDiffSpotsOccupied_min
					&& data.getNumberDiffSpotsOccupied_avg() == numberDiffSpotsOccupied_avg
					&& data.getNumberDiffSpotsOccupied_max() == numberDiffSpotsOccupied_max
					&& data.getReocupationTime() == reocupationTime;
		} else {
			return false;
		}
	}
}
