package evolutionaryrobotics.populations;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import simulation.util.Arguments;
import evolutionaryrobotics.evolution.JoinedGenerationalEvolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.neuralnetworks.MultipleChromosome;

public class MixedPopulation extends MuLambdaPopulation{

	protected int numberOfGenomes;
	protected int[] genomeLengths;

	public MixedPopulation(Arguments arguments) {
		super(arguments);
		
		//Getting the genome lengths
		String values = arguments.getArgumentAsString("genomelengthstr");
		String[] split = values.trim().split(":");
		
		numberOfGenomes = arguments.getArgumentAsInt("numberofchromossomes");
		genomeLengths = new int[numberOfGenomes];
		for(int i = 0; i < numberOfGenomes; i++){
			genomeLengths[i] = Integer.parseInt(split[i+1].trim());
		}
	}

	@Override
	public void createNextGeneration() {
		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		if (numberOfChromosomesEvaluated < populationSize) {
			throw new java.lang.RuntimeException(
					"Trying to create a new generation before all chromosomes have been evaluated");
		}

		fitnessThresholdReached = checkFitnessThreshold(bestFitness);

		LinkedList<Chromosome> sortedChromosomes = new LinkedList<Chromosome>();
		for (int i = 0; i < populationSize; i++)
			sortedChromosomes.add(chromosomes[i]);

		Collections.sort(sortedChromosomes,
				new Chromosome.CompareChromosomeFitness());

		// Find the parents for reproduction:
		LinkedList<Chromosome> parents = new LinkedList<Chromosome>();

		// Copy the parents to a new vector:
		if (lambda >= populationSize) {
			throw new java.lang.RuntimeException("Error: There are "
					+ populationSize
					+ " chromosomes in the population and lambda is " + lambda);
		}

		Iterator<Chromosome> sortedIterator = sortedChromosomes.iterator();
		for (int i = 0; i < lambda; i++)
			parents.add(sortedIterator.next());

		// Copy the elites (if any) to a new vector
		if (numberOfElites >= populationSize) {
			throw new java.lang.RuntimeException("There are " + populationSize
					+ " chromosomes in the population and the elite is "
					+ numberOfElites);
		}

		sortedIterator = sortedChromosomes.iterator();

		for (int i = 0; i < numberOfElites; i++) {

			MultipleChromosome eliteChromosome = new MultipleChromosome(sortedIterator.next()
					.getAlleles(), i, genomeLengths, numberOfGenomes);
			chromosomes[i] =  eliteChromosome;
		}

		// Do the reproduction:
		Iterator<Chromosome> parentIterator = parents.iterator();

		for (int i = 0; i < populationSize - numberOfElites; i++) {
			if (!parentIterator.hasNext()) {
				parentIterator = parents.iterator();
			}
			double[] alleles = new double[genomelength];

			Chromosome parent = parentIterator.next();

			for (int j = 0; j < genomelength; j++) {
				double allele = parent.getAlleles()[j];
				if (randomNumberGenerator.nextDouble() < mutationRate) {
					allele = allele + randomNumberGenerator.nextGaussian();
					if (allele < -10)
						allele = -10;
					if (allele > 10)
						allele = 10;
				}

				alleles[j] = allele;
			}

			chromosomes[numberOfElites + i] = new MultipleChromosome(alleles, numberOfElites + i, genomeLengths, numberOfGenomes);
		}

		resetGeneration();
		currentGeneration++;

		// Create a new random seed for this generation
		// (just use the random generators next int):
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	@Override
	public void createRandomPopulation() {

		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		chromosomes = new MultipleChromosome[populationSize];
		
		if(fixedInitialPopulation) {
			for (int i = 0; i < populationSize; i++) {
				double[] alleles = new double[initialWeights.length*numberOfGenomes];
				for(int j = 0; j<numberOfGenomes; j++){
					alleles = MultipleChromosome.concatArray(alleles, initialWeights);
				}
				chromosomes[i] = new MultipleChromosome(alleles, i, genomeLengths, numberOfGenomes);				
			}
			bestChromosome = chromosomes[0];
		} else {
			for (int i = 0; i < populationSize; i++) {
				double[] alleles = new double[genomelength];
				for (int j = 0; j < genomelength; j++) {
					alleles[j] = randomNumberGenerator.nextGaussian() * 2;
				}
				chromosomes[i] = new MultipleChromosome(alleles, i, genomeLengths, numberOfGenomes);
			}
		}

		resetGeneration();
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	@Override
	public Chromosome getBestChromosome() {
		return bestChromosome;
	}
	
	@Override
	public Chromosome[] getTopChromosome(int number) {
		Chromosome[] top = new Chromosome[number];
		LinkedList<Chromosome> sortedChromosomes = new LinkedList<Chromosome>();
		for (int i = 0; i < populationSize; i++)
			sortedChromosomes.add(chromosomes[i]);

		Collections.sort(sortedChromosomes,
				new Chromosome.CompareChromosomeFitness());

		Iterator<Chromosome> sortedIterator = sortedChromosomes.iterator();

		for (int i = 0; i < top.length; i++) {
			top[i] = sortedIterator.next();
		}

		return top;
	}

	@Override
	public double getLowestFitness() {
		return worstFitness;
	}

	@Override
	public double getAverageFitness() {
		return accumulatedFitness / (double) populationSize;
	}

	@Override
	public double getHighestFitness() {
		return bestFitness;
	}

	@Override
	public Chromosome getNextChromosomeToEvaluate() {
//		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		if (nextChromosomeToEvaluate < populationSize) {
			return chromosomes[nextChromosomeToEvaluate++];
		} else {
			return null;
		}
	}

	@Override
	public int getNumberOfChromosomesEvaluated() {
		return numberOfChromosomesEvaluated;
	}

	// @Override
	public int getPopulationSize() {
		return populationSize;
	}

	// @Override
	public int getNumberOfCurrentGeneration() {
		return currentGeneration;
	}

	// @Override
	public void setEvaluationResult(Chromosome chromosome, double fitness) {
		if (chromosome.getFitnessSet()) {
			throw new java.lang.RuntimeException("Fitness of " + chromosome
					+ " already set -- trying to set it again");
		}

		chromosome.setFitness(fitness);
		numberOfChromosomesEvaluated++;
		accumulatedFitness += fitness;

		if (fitness > bestFitness) {
			bestChromosome = chromosome;
			bestFitness = fitness;
		}

		if (fitness < worstFitness) {
			worstFitness = fitness;
		}

}}
