package evolutionaryrobotics.populations;

import evolutionaryrobotics.neuralnetworks.Chromosome;
import simulation.util.SimRandom;

public class ChromosomeListedPopulation extends ListedPopulation<Chromosome>{

	public ChromosomeListedPopulation(SimRandom random, int populationSize,
			int chromosomeLength) {
		super(random, populationSize, chromosomeLength);
	}

	@Override
	public Chromosome createNewIndividual() {
		double[] alleles  = new double[chromosomeLength]; 
		for (int j = 0; j < chromosomeLength; j++) {
			alleles[j] = random.nextGaussian() * 2 - 1;
		}
		currentId++;
		return new Chromosome(alleles, currentId);
	}

	@Override
	public void mutateWithRate(Chromosome toMutate, double mutRate) {
		double[] alleles = toMutate.getAlleles();
		for(int i = 0; i < alleles.length; i++){
			if (random.nextDouble() < mutRate) {
				alleles[i] = alleles[i] + random.nextGaussian();
				if (alleles[i] < -10)
					alleles[i] = -10;
				if (alleles[i] > 10)
					alleles[i] = 10;
			}
		}
	}

}
