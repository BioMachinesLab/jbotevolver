package evolutionaryrobotics.populations;

import simulation.util.SimRandom;

public class AgeChromosomeListedPopulation extends ListedPopulation<AgeChromosome>{

	public AgeChromosomeListedPopulation(SimRandom random, int populationSize, int chromosomeLength) {
		super(random, populationSize, chromosomeLength);
	}

	@Override
	public AgeChromosome createNewIndividual() {
		double[] alleles  = new double[chromosomeLength]; 
		for (int j = 0; j < chromosomeLength; j++) {
			alleles[j] = random.nextGaussian() * 2 - 1;
		}
		currentId++;
		return new AgeChromosome(alleles, currentId);
	}

	@Override
	public void mutateWithRate(AgeChromosome toMutate, double mutRate) {
		double[] alleles = toMutate.getAlleles();
		for(int i = 0; i < alleles.length; i++){
			if (random.nextDouble() < mutRate) {
				alleles[i] = alleles[i] + random.nextGaussian();
				if (alleles[i] < -10)
					alleles[i] = -10;
				if (alleles[i] > 10)
					alleles[i] = 10;
				toMutate.setAge(i, Math.max(toMutate.getAge(i) - 1, 1));
			}
			else
				toMutate.setAge(i, toMutate.getAge(i) + 1);

		}
	}
}
