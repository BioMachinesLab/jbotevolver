package evolutionaryrobotics.neuralnetworks;

import java.io.Serializable;
import java.util.Comparator;

public class Chromosome implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected double[] alleles;
	protected double   fitness;
	protected int      id;
	protected boolean  fitnessSet = false;
		
	public Chromosome(double[] alleles, int id) {
		this.alleles = alleles;
		this.id      = id;
	}
	
	public void    setFitness(double fitness) {
		this.fitness = fitness; 
		this.fitnessSet = true;
	}
	
	public void setAlleles(double[] newAlleles){
		this.alleles = newAlleles;
	}
	
	public double  getFitness() {
		return fitness;
	}
	
	public double[] getAlleles() {
		return alleles;
	}

	public int      getID() {
		return id;
	}

	public boolean getFitnessSet() {
		return fitnessSet;
	}

	// Compare to get the chromosome in descending order
	public static class CompareChromosomeFitness implements Comparator<Chromosome> {
		public int compare(Chromosome arg0, Chromosome arg1) {			
			if (arg0.getFitness() < arg1.getFitness())
				return 1;
			if (arg0.getFitness() > arg1.getFitness())
				return -1;
			
			return 0;
		}
	}
	
	public Chromosome clone() throws CloneNotSupportedException{
		Chromosome newChromo = (Chromosome) super.clone();
		newChromo.alleles = alleles.clone();
		return newChromo;
	}

}
