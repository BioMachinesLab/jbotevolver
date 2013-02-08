package evolutionaryrobotics.evolutionarystrategies;

import simulation.util.SimRandom;
import evolutionaryrobotics.populations.AgeChromosome;

public class AgeBasedMuLambdaES <E extends AgeChromosome> extends MuLambdaES<E>{

	public AgeBasedMuLambdaES(SimRandom random, short mu, short lambda,
			double mutationRate) {
		super(random, mu, lambda, mutationRate);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E mutate(E parent) {
		double[] parentAlleles = parent.getAlleles();
    	double[] newAlleles = new double[parentAlleles.length];
    	int[] ages = new int[parentAlleles.length];
    	//cannot instantiate generic types, must clone...
    	E child = null;
    	try {
			 child = (E) parent.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
    	for (int i = 0; i < newAlleles.length; i++) {        
            double allele = parentAlleles[i]; 
            int age = parent.getAge(i);
            double ageProb = 1 - (age - 1)/(AgeChromosome.MAX_AGE - 1);
            if (random.nextDouble() < mutationRate && random.nextDouble() < ageProb) {
            	allele = allele + random.nextGaussian();
                if (allele < -10)
                	allele = -10;
                if (allele > 10)
                	allele = 10;
                //decrease age
				age = Math.max(age - 1, 1);
            }
            
            newAlleles[i] = allele;
            ages[i] = age;
        }
    	child.setAllAges(ages);
    	child.setAlleles(newAlleles);
    	
    	return child;
	}

}
