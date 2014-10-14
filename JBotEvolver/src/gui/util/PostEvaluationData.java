package gui.util;

public class PostEvaluationData {

	private String folder;
	private int bestChromosomeNumber;
	private double bestChromosomeFitness;
	private double setupAverage;
	
	public PostEvaluationData(String folder, int bestChromosomeNumber,double bestChromosomeFitness, double setupAverage) {
		this.folder = folder;
		this.bestChromosomeNumber = bestChromosomeNumber;
		this.bestChromosomeFitness = bestChromosomeFitness;
		this.setupAverage = setupAverage;
	}

	public String getFolder() {
		return folder;
	}
	
	public int getBestChromosomeNumber() {
		return bestChromosomeNumber;
	}
	
	public double getBestChromosomeFitness() {
		return bestChromosomeFitness;
	}
	
	public double getSetupAverage() {
		return setupAverage;
	}
	
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	public void setBestChromosomeNumber(int bestChromosomeNumber) {
		this.bestChromosomeNumber = bestChromosomeNumber;
	}
	
	public void setBestChromosomeFitness(double bestChromosomeFitness) {
		this.bestChromosomeFitness = bestChromosomeFitness;
	}
	
	public void setSetupAverage(double setupAverage) {
		this.setupAverage = setupAverage;
	}
	
	@Override
	public String toString() {
		return folder + "\nBest: " + bestChromosomeNumber + ", Fitness: " + bestChromosomeFitness + ", Average: " + setupAverage;
	}
	
}
