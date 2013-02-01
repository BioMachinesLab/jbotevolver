package evolutionaryrobotics.populations;

import java.util.Arrays;

import evolutionaryrobotics.neuralnetworks.Chromosome;

public class AgeChromosome extends Chromosome implements Cloneable{

	private static final long serialVersionUID = -9057834772237907982L;

	public static final int MAX_AGE = 300;
	private int age[];


	public AgeChromosome(double[] alleles, int id) {
		super(alleles, id);
		age = new int[alleles.length];
		Arrays.fill(age, 1);
	}

	public int getAge(int index){
		return age[index];
	}

	public void setAge(int index, int newAge){
		age[index] = newAge;
	}
	
	public void setAllAges(int[] newAges){
		this.age = newAges;
	}
	
	public void setAlleles(double[] newAlleles){
		this.alleles = newAlleles;
	}

	public AgeChromosome clone() throws CloneNotSupportedException{
		AgeChromosome c = (AgeChromosome) super.clone();
		c.age = age.clone();
		return c;
	}
	
	public boolean equals(Object obj){
	        if (obj == null)
	            return false;
	        if (obj == this)
	            return true;
	        if (obj.getClass() != getClass())
	            return false;

	        AgeChromosome c = (AgeChromosome) obj;
	        return this.age == c.age && this.id == c.id && this.alleles == c.alleles;
	}
}
