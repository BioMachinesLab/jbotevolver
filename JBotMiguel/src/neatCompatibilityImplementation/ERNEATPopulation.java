package neatCompatibilityImplementation;

import java.io.Serializable;
import java.util.Map;

import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.ml.ea.train.basic.TrainEA;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.opp.NEATMutateAddLink;
import org.encog.neural.neat.training.opp.NEATMutateAddNode;
import org.encog.neural.neat.training.opp.NEATMutateRemoveLink;
import org.encog.neural.neat.training.opp.NEATMutateWeights;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.MutateResetLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectFixed;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;


public class ERNEATPopulation extends Population implements Serializable{

	protected int populationSize;
	protected int numberOfGenerations;
	protected int numberOfSamplesPerChromosome;
	protected int numberInputs, numberOutputs;
	protected org.encog.neural.neat.NEATPopulation population;

	protected int generationsElapsed = 0;

	protected transient TrainEA trainer;

	protected transient MOEvaluation<NEATNetwork> evaluator;

	protected Map<NEATNetwork, TaskStatistics> stats;

	protected double bestFitness, worstFitness, avgFitness;

	public ERNEATPopulation(Arguments arguments) {
		super(arguments);
		populationSize = arguments.getArgumentAsIntOrSetDefault("size",50);
		numberOfGenerations = arguments.getArgumentAsIntOrSetDefault("generations",100);
		numberOfSamplesPerChromosome = arguments.getArgumentAsIntOrSetDefault("samples",10);
		/**
		 * compatibility with encog requires the field "--population" 
		 * to indicate the number of inputs and the number of outputs
		 */

		numberInputs = arguments.getArgumentAsIntOrSetDefault("inputs", 1);
		numberOutputs = arguments.getArgumentAsIntOrSetDefault("outputs", 1);
	}

	public void createRandomPopulation(JBotEvolver jBotEvolver) {
		population = new org.encog.neural.neat.NEATPopulation(
				numberInputs, numberOutputs, populationSize);
		population.setInitialConnectionDensity(1.0);// not required, but speeds training
		population.reset();

		//encog's structure requires the fitness function and the EA within the population
		this.evaluator = MOEvaluation.getEvaluationFunction(
				jBotEvolver.getArguments().get("--evaluation"));

		evaluator.setupObjectives(jBotEvolver.getArguments());

		trainer = constructPopulationTrainer();

		setGenerationRandomSeed(randomNumberGenerator.nextInt());
		//System.out.println(population.getSpecies().size());
	}

	/**
	 * from encog
	 */
	public TrainEA constructPopulationTrainer() {
		final TrainEA result = new TrainEA(population, this.evaluator);
		OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
		speciation.setMaxNumberOfSpecies(5);
		result.setSpeciation(speciation);

		result.setSelection(new TruncationSelection(result, 0.2));
		final CompoundOperator weightMutation = new CompoundOperator();
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectFixed(1),
						new MutatePerturbLinkWeight(0.02)));
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectFixed(2),
						new MutatePerturbLinkWeight(0.02)));
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectFixed(3),
						new MutatePerturbLinkWeight(0.02)));
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectProportion(0.02),
						new MutatePerturbLinkWeight(0.02)));
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectFixed(1),
						new MutatePerturbLinkWeight(1)));
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectFixed(2),
						new MutatePerturbLinkWeight(1)));
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectFixed(3),
						new MutatePerturbLinkWeight(1)));
		weightMutation.getComponents().add(
				0.1125,
				new NEATMutateWeights(new SelectProportion(0.02),
						new MutatePerturbLinkWeight(1)));
		weightMutation.getComponents().add(
				0.03,
				new NEATMutateWeights(new SelectFixed(1),
						new MutateResetLinkWeight()));
		weightMutation.getComponents().add(
				0.03,
				new NEATMutateWeights(new SelectFixed(2),
						new MutateResetLinkWeight()));
		weightMutation.getComponents().add(
				0.03,
				new NEATMutateWeights(new SelectFixed(3),
						new MutateResetLinkWeight()));
		weightMutation.getComponents().add(
				0.01,
				new NEATMutateWeights(new SelectProportion(0.02),
						new MutateResetLinkWeight()));
		weightMutation.getComponents().finalizeStructure();

		result.setChampMutation(weightMutation);
		//result.addOperation(0.5, new NEATCrossover());
		result.addOperation(0.5, weightMutation);
		result.addOperation(0.03, new NEATMutateAddNode());
		result.addOperation(0.05, new NEATMutateAddLink());
		result.addOperation(0.05, new NEATMutateRemoveLink());
		result.getOperators().finalizeStructure();

		result.setCODEC(new NEATCODEC());

		return result;
	}

	@Override
	public boolean evolutionDone() {
		return this.getNumberOfCurrentGeneration() >= this.getNumberOfGenerations();
	}

	@Override
	public int getNumberOfCurrentGeneration() {
		return generationsElapsed;
	}

	@Override
	public int getPopulationSize() {
		return this.populationSize;
	}

	public NEATGenome getBestGenome(){
		return (NEATGenome) this.population.getBestGenome();
	}

	public void evolvePopulation(JBotEvolver jBotEvolver, TaskExecutor taskExecutor) {
		generationsElapsed++;
		//System.out.println("GENERATION " + generationsElapsed);

		randomNumberGenerator.setSeed(getGenerationRandomSeed());
		evaluator.setupEvolution(jBotEvolver, taskExecutor, this.numberOfSamplesPerChromosome);

		trainer.iteration();

		setGenerationRandomSeed(randomNumberGenerator.nextInt());
	}

	public void updateStatistics() {
		this.stats = evaluator.getObjectivesStatistics();

		updateFitnessStats();
	}

	private void updateFitnessStats() {
		//the first objective is the one related to task performance
		String objectiveKey = this.evaluator.getObjectiveKey(1);
		this.worstFitness = Double.MAX_VALUE;
		this.bestFitness = Double.MIN_VALUE;
		this.avgFitness = 0.0;

		for(NEATNetwork key : this.stats.keySet()){
			TaskStatistics stats = this.stats.get(key);
			double objectiveValue = stats.get(objectiveKey);
			this.bestFitness = Math.max(objectiveValue, bestFitness);
			this.worstFitness = Math.min(objectiveValue, worstFitness);
			avgFitness += objectiveValue;
		}

		this.avgFitness /= this.stats.size();
	}

	public int getNumberOfObjectives() {
		return this.evaluator.getNumberOfObjectives();
	}

	@Override
	public double getLowestFitness() {
		return this.worstFitness;
	}

	@Override
	public double getAverageFitness() {
		return this.avgFitness;
	}

	@Override
	public double getHighestFitness() {
		return this.bestFitness;
	}

	public int getNumberOfSpecies() {
		return this.population.getSpecies().size();
	}


	public void resetStatistics() {
		this.stats.clear();
		this.evaluator.resetStatistics();
	}

	public Map<NEATNetwork, TaskStatistics> getStatistics(){
		return this.stats;
	}

	/**************************************************************************
	 **************************************************************************
	 **************************************************************************
	 * not used.
	 **************************************************************************
	 **************************************************************************
	 **************************************************************************/

	@Override
	public int getNumberOfChromosomesEvaluated() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Chromosome getNextChromosomeToEvaluate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEvaluationResult(Chromosome chromosome, double fitness) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEvaluationResultForId(int pos, double fitness) {
		// TODO Auto-generated method stub

	}

	@Override
	public Chromosome getBestChromosome() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chromosome[] getTopChromosome(int number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Chromosome getChromosome(int chromosomeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createNextGeneration() {
		// TODO Auto-generated method stub

	}

	@Override
	public void createRandomPopulation() {
		// TODO Auto-generated method stub

	}



	/**
	 * end of not methods not used.
	 */


}
