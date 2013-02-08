package evolutionaryrobotics.ga;

import simulation.util.SimRandom;
import evolutionaryrobotics.populations.AgeChromosome;

public class AgeMicrobialGeneticAlgorithm extends MicrobialGeneticAlgorithm<AgeChromosome>{


	public AgeMicrobialGeneticAlgorithm(SimRandom random, double mutationProb,
			double recombinationRate) {
		super(random, mutationProb, recombinationRate);
	}


	@Override
	public AgeChromosome recombine(AgeChromosome e1, AgeChromosome e2) {
		AgeChromosome winner = binaryTournament(e1, e2);
		AgeChromosome loser = winner == e1 ? e2 : e1;
		double[] winnerAlleles = winner.getAlleles(), loserAlleles = loser.getAlleles();
		for(int i = 0; i < winnerAlleles.length; i++){
			if(random.nextDouble() < recombinationRate && winner.getAge(i) >= loser.getAge(i)){
				loserAlleles[i] = winnerAlleles[i];
				//gets the age of the winner allele.
				loser.setAge(i, winner.getAge(i));
			}
		}

		return loser;
	}

	

	@Override
	public void mutate(AgeChromosome element) {
		double[] alleles = element.getAlleles();
		for (int j = 0; j < alleles.length; j++) {         
			double allele = alleles[j];
			//mutation prob defined by age.
			double ageProb = 1 - (element.getAge(j) - 1)/(AgeChromosome.MAX_AGE - 1);
			if (random.nextDouble() < mutationProb && random.nextDouble() < ageProb) {
				allele = allele + random.nextGaussian();
				if (allele < -10)
					allele = -10;
				if (allele > 10)
					allele = 10;
				//decrease age.
				element.setAge(j, Math.max(element.getAge(j) - 1, 1));
				alleles[j] = allele; 
			}
			else
				element.setAge(j, element.getAge(j) + 1);			          
		}
	}

}
