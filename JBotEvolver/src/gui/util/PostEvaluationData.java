package gui.util;

public class PostEvaluationData {

	private String folder;
	private int bestFitnessNumber;
	private double bestFitness;
	private double averageFitness;
	
	public PostEvaluationData(String folder, int bestFitnessNumber, double bestFitness, double averageFitness) {
		this.folder = folder;
		this.bestFitnessNumber = bestFitnessNumber;
		this.bestFitness = ((int)(bestFitness*100))/100.0;
		this.averageFitness = ((int)(averageFitness*100))/100.0;
	}

	public String getFolder() {
		return folder;
	}
	
	public int getBestFitnessNumber() {
		return bestFitnessNumber;
	}
	
	public double getBestFitness() {
		return bestFitness;
	}
	
	public double getAverageFitness() {
		return averageFitness;
	}
	
	public void setFolder(String folder) {
		this.folder = folder;
	}
	
	@Override
	public String toString() {
		return folder + "\nBest: " + bestFitnessNumber + ", Fitness: " + bestFitness + ", Average: " + averageFitness;
	}
	
}
