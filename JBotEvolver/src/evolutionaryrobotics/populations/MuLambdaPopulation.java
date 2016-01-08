package evolutionaryrobotics.populations;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

/**
 * Implements a [mu, lambda] evolutionary algorithm with rank-based selection
 * and elitism.
 * 
 * @author alc
 */

public class MuLambdaPopulation extends Population implements Serializable {
	private static final long serialVersionUID = 1L;

	protected int genomelength;
	@ArgumentsAnnotation(name="size", defaultValue="100")
	protected int populationSize;
	protected int currentGeneration;
	protected Chromosome bestChromosome;
	protected Chromosome chromosomes[];

	protected double bestFitness;
	protected double accumulatedFitness;
	protected double worstFitness;
	protected int numberOfChromosomesEvaluated;
	protected int nextChromosomeToEvaluate;
	protected int numberOfElites = 5;
	protected int lambda = 5;
	protected boolean fitnessThresholdReached = false;
	
	protected double[] initialWeights;
	protected boolean fixedInitialPopulation = false;

	public MuLambdaPopulation(Arguments arguments) {
		super(arguments);
		populationSize = arguments.getArgumentAsIntOrSetDefault("size",100);
		numberOfGenerations = arguments.getArgumentAsIntOrSetDefault("generations",100);
		numberOfSamplesPerChromosome = arguments.getArgumentAsIntOrSetDefault("samples",5);
		mutationRate = arguments.getArgumentAsDoubleOrSetDefault("mutationrate", 0.1);
		
		genomelength = arguments.getArgumentAsInt("genomelength");
		
		fixedInitialPopulation = arguments.getArgumentAsIntOrSetDefault("fixedinitialpopulation", 0) == 1;
		
		if(fixedInitialPopulation) {
			if(arguments.getArgumentIsDefined("initialweights")) {
				String[] rawArray = arguments.getArgumentAsString("initialweights").split(",");
				initialWeights = new double[rawArray.length];
				for(int i = 0 ; i < initialWeights.length ; i++)
					initialWeights[i] = Double.parseDouble(rawArray[i]);
			} 
		}
	}

	@Override
	public void createNextGeneration() {
		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		if (numberOfChromosomesEvaluated < populationSize) {
			throw new java.lang.RuntimeException("Trying to create a new generation before all chromosomes have been evaluated");
		}

		fitnessThresholdReached = checkFitnessThreshold(bestFitness);

		LinkedList<Chromosome> sortedChromosomes = new LinkedList<Chromosome>();
		for (int i = 0; i < populationSize; i++) {
			sortedChromosomes.add(chromosomes[i]);
		}
		
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

			Chromosome eliteChromosome = new Chromosome(sortedIterator.next()
					.getAlleles(), i);
			chromosomes[i] = eliteChromosome;
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

			chromosomes[numberOfElites + i] = new Chromosome(alleles,
					numberOfElites + i);
		}

		resetGeneration();
		currentGeneration++;

		// Create a new random seed for this generation
		// (just use the random generators next int):
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	protected void resetGeneration() {
		bestFitness = -1e10;
		accumulatedFitness = 0;
		worstFitness = 1e10;
		numberOfChromosomesEvaluated = 0;
		nextChromosomeToEvaluate = 0;
	}

	@Override
	public void createRandomPopulation() {

		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		chromosomes = new Chromosome[populationSize];
		
		if(fixedInitialPopulation) {
			for (int i = 0; i < populationSize; i++) {
				chromosomes[i] = new Chromosome(initialWeights, i);
			}
			bestChromosome = chromosomes[0];
		} else {
			for (int i = 0; i < populationSize; i++) {
				double[] alleles = new double[genomelength];
				for (int j = 0; j < genomelength; j++) {
					alleles[j] = randomNumberGenerator.nextGaussian() * 2;
				}
				chromosomes[i] = new Chromosome(alleles, i);
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
		return accumulatedFitness / (double) chromosomes.length;
	}

	@Override
	public double getHighestFitness() {
		return bestFitness;
	}

	@Override
	public Chromosome getNextChromosomeToEvaluate() {
//		randomNumberGenerator.setSeed(getGenerationRandomSeed());

		if (nextChromosomeToEvaluate < chromosomes.length) {
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

	@Override
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
	}

	@Override
	public void setEvaluationResultForId(int pos, double fitness) {
		if (pos >= chromosomes.length) {
			throw new java.lang.RuntimeException("No such position: " + pos
					+ " on the population");
		}

		chromosomes[pos].setFitness(fitness);
		numberOfChromosomesEvaluated++;
		accumulatedFitness += fitness;

		if (fitness > bestFitness) {
			bestChromosome = chromosomes[pos];
			bestFitness = fitness;
		}

		if (fitness < worstFitness) {
			worstFitness = fitness;
		}

	}

	// @Override
	public boolean evolutionDone() {
		if (currentGeneration >= numberOfGenerations ||
				(currentGeneration == numberOfGenerations-1 && getNumberOfChromosomesEvaluated() == chromosomes.length) ||
				fitnessThresholdReached)
			return true;
		else
			return false;
	}

	@Override
	public Chromosome getChromosome(int chromosomeId) {
		return chromosomes[chromosomeId];
	}
	
	@Override
	public void setupIndividual(Robot r) {
		Chromosome c = getBestChromosome();
		if(r.getController() instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController fc = (FixedLenghtGenomeEvolvableController)r.getController();
			if(fc.getNNWeights() == null) {
				fc.setNNWeights(c.getAlleles());
			}
		}
	}
	
	public Chromosome[] getChromosomes() {
		return chromosomes;
	}
	
	public void setChromosomes(Chromosome[] c) {
		this.chromosomes = c;
	}
}