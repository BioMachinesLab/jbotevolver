package evolutionaryrobotics.populations;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import evolutionaryrobotics.evolution.neat.NEATSerializer;
import evolutionaryrobotics.evolution.neat.core.NEATNetDescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATNeuralNet;
import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class NEATPopulation extends Population {

	private static final long serialVersionUID = 3149732524276911322L;
	protected NEATPopulation4J pop;
	protected int size = 200;
	protected int generationNumber = 0;
	protected Chromosome bestChromosome;
	protected Chromosome chromosomes[];

	protected double bestFitness;
	protected double accumulatedFitness;
	protected double worstFitness;
	protected int numberOfChromosomesEvaluated;

	public NEATPopulation(Arguments arguments) {
		super(arguments);
		size = arguments.getArgumentAsIntOrSetDefault("size", size);
	}

	public void setNEATPopulation4J(NEATPopulation4J pop) {
		this.pop = pop;
	}

	public NEATPopulation4J getNEATPopulation4J() {
		return pop;
	}

	@Override
	public void createRandomPopulation() {
		randomNumberGenerator.setSeed(generationRandomSeed);
		generationNumber = 0;
		chromosomes = new Chromosome[getPopulationSize()];
		bestChromosome = null;
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	@Override
	public int getNumberOfCurrentGeneration() {
		return generationNumber;
	}

	@Override
	public int getPopulationSize() {
		return size;
	}

	@Override
	public int getNumberOfChromosomesEvaluated() {
		return numberOfChromosomesEvaluated;
	}

	@Override
	public Chromosome getNextChromosomeToEvaluate() {
		return null;
	}

	@Override
	public void setEvaluationResult(Chromosome chromosome, double fitness) {
		numberOfChromosomesEvaluated++;
		accumulatedFitness += fitness;
		worstFitness = Math.min(worstFitness, fitness);

		if (bestFitness < fitness) {
			bestChromosome = chromosome;
			bestFitness = Math.max(bestFitness, fitness);
		}
	}

	@Override
	public void setEvaluationResultForId(int pos, double fitness) {
		numberOfChromosomesEvaluated++;
		accumulatedFitness += fitness;
		bestFitness = Math.max(bestFitness, fitness);
		worstFitness = Math.min(worstFitness, fitness);
	}

	@Override
	public void createNextGeneration() {
		resetGeneration();
		generationNumber++;
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	protected void resetGeneration() {
		bestFitness = Double.MIN_VALUE;
		accumulatedFitness = 0;
		worstFitness = Double.MAX_VALUE;
		numberOfChromosomesEvaluated = 0;
		chromosomes = new Chromosome[getPopulationSize()];
	}

	@Override
	public double getLowestFitness() {
		return worstFitness;
	}

	@Override
	public double getAverageFitness() {
		return accumulatedFitness / getPopulationSize();
	}

	@Override
	public double getHighestFitness() {
		return bestFitness;
	}

	@Override
	public Chromosome getBestChromosome() {
		return bestChromosome;
	}

	@Override
	public Chromosome[] getTopChromosome(int number) {
		Chromosome[] top = new Chromosome[number];
		LinkedList<Chromosome> sortedChromosomes = new LinkedList<Chromosome>();
		for (int i = 0; i < getPopulationSize(); i++)
			sortedChromosomes.add(chromosomes[i]);

		Collections.sort(sortedChromosomes, new Chromosome.CompareChromosomeFitness());

		Iterator<Chromosome> sortedIterator = sortedChromosomes.iterator();

		for (int i = 0; i < top.length; i++) {
			top[i] = sortedIterator.next();
		}

		return top;
	}

	@Override
	public Chromosome getChromosome(int chromosomeId) {
		return chromosomes[chromosomeId];
	}

	@Override
	public void setupIndividual(Robot r) {
		Chromosome c = getBestChromosome();
		c.setupRobot(r);
	}

	@Override
	public boolean evolutionDone() {
		if (generationNumber >= numberOfGenerations
				|| (generationNumber == numberOfGenerations - 1
						&& getNumberOfChromosomesEvaluated() == getPopulationSize())
				|| checkFitnessThreshold(bestFitness))
			return true;
		else
			return false;
	}

	public Chromosome convertChromosome(evolutionaryrobotics.evolution.neat.ga.core.Chromosome c, int i) {
		NEATNetDescriptor descr = new NEATNetDescriptor(0, null);
		descr.updateStructure(c);
		NEATNeuralNet network = new NEATNeuralNet();
		network.createNetStructure(descr);
		network.updateNetStructure();
		return new Chromosome(NEATSerializer.serialize(network), i);
	}

	@Override
	public Chromosome[] getChromosomes() {
		return chromosomes;
	}
}