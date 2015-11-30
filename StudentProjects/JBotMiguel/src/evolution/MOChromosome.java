package evolution;

import java.util.HashMap;

import evolutionaryrobotics.neuralnetworks.Chromosome;

public class MOChromosome extends Chromosome{
	
	protected HashMap<String,Double> objectiveFitness = new HashMap<String, Double>();
	
	public MOChromosome(double[] alleles, int id) {
		super(alleles,id);
	}
	
	public double getFitness(String objective) {
		return objectiveFitness.get(objective);
	}
	
	public void setFitness(String objective, double fitness) {
		objectiveFitness.put(objective,fitness);
	}
}
