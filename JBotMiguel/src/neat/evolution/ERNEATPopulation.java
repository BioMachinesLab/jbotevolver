package neat.evolution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import neat.ERNEATNetwork;
import neat.JBotEvolverRandomFactory;
import neat.WrapperNetwork;
import neat.continuous.ERNEATContinuousNetwork;
import neat.continuous.NEATContinuousCODEC;
import neat.continuous.NEATContinuousNetwork;
import neat.continuous.SelectContinuousProportion;
import neat.evaluation.NEATEvaluation;
import neat.evaluation.SOEvaluationManager;
import neat.evaluation.TaskStatistics;
import neat.layerered.LayeredANN;
import neat.layerered.LayeredNEATCODEC;
import neat.layerered.LayeredNeuralNetwork;
import neat.layerered.continuous.LayeredContinuousNEATCODEC;
import neat.layerered.continuous.LayeredContinuousNeuralNetwork;
import neat.mutations.MutatePerturbNeuronDecay;
import neat.mutations.NEATCrossoverJBot;
import neat.mutations.NEATMutateAddContinuousNode;
import neat.mutations.NEATMutateAddLinkJBot;
import neat.mutations.NEATMutateAddNodeJBot;
import neat.mutations.NEATMutateAddParameterNode;
import neat.mutations.NEATMutateChangeParameterNode;
import neat.mutations.NEATMutateNeuronDecays;
import neat.mutations.NEATMutateRemoveParameterNode;
import neat.parameters.ParameterNEATCODEC;
import neat.parameters.ParameterNeuralNetwork;

import org.encog.ml.MLMethod;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.opp.CompoundOperator;
import org.encog.ml.ea.opp.selection.TruncationSelection;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.opp.NEATMutateWeights;
import org.encog.neural.neat.training.opp.links.MutatePerturbLinkWeight;
import org.encog.neural.neat.training.opp.links.SelectProportion;
import org.encog.neural.neat.training.species.OriginalNEATSpeciation;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import controllers.Controller;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import evolutionaryrobotics.populations.Population;

public class ERNEATPopulation extends Population implements Serializable{

	private static final long serialVersionUID = 5093899312027130856L;
	protected int populationSize;
	protected int numberOfGenerations;
	protected int numberOfSamplesPerChromosome;
	protected int numberInputs, numberOutputs;
	protected NEATPopulation population;

	protected int generationsElapsed = 0;

	protected TrainEAJBot trainer;
	protected NEATEvaluation evaluator;
	protected Map<MLMethod, TaskStatistics> stats;

	protected double bestFitness, worstFitness, avgFitness;
	
	protected double addNodeRate = 0.02;
	protected double addLinkRate = 0.1;
	protected double mutateLinkWeightRate = 0.8;
	protected double mutateDecayRate = 0.4;
	protected double crossoverRate = 0.2;
	
	protected double parameterAddRate = 0.0;
	protected double parameterRemoveRate = 0.0;
	protected double parameterChangeRate = 0.1;
	
	protected WrapperNetwork network;
	
	protected int bootstrap = 0;

	public ERNEATPopulation(Arguments arguments) {
		super(arguments);
		populationSize = arguments.getArgumentAsIntOrSetDefault("size",100);
		numberOfGenerations = arguments.getArgumentAsIntOrSetDefault("generations",100);
		numberOfSamplesPerChromosome = arguments.getArgumentAsIntOrSetDefault("samples",10);
		
		addNodeRate = arguments.getArgumentAsDoubleOrSetDefault("addnoderate", addNodeRate);
		addLinkRate = arguments.getArgumentAsDoubleOrSetDefault("addlinkrate", addLinkRate);
		mutateLinkWeightRate = arguments.getArgumentAsDoubleOrSetDefault("mutatelinkweightrate", mutateLinkWeightRate);
		mutateDecayRate = arguments.getArgumentAsDoubleOrSetDefault("mutatedecayrate", mutateDecayRate);
		crossoverRate = arguments.getArgumentAsDoubleOrSetDefault("crossoverrate", crossoverRate);
		parameterAddRate = arguments.getArgumentAsDoubleOrSetDefault("parameteraddrate", parameterAddRate);
		parameterRemoveRate = arguments.getArgumentAsDoubleOrSetDefault("parameterremoverate", parameterRemoveRate);
		parameterChangeRate = arguments.getArgumentAsDoubleOrSetDefault("parameterchangerate", parameterChangeRate);
		
		bootstrap = arguments.getArgumentAsIntOrSetDefault("bootstrap", 0);
	}

	public void createRandomPopulation(JBotEvolver jBotEvolver) {
		
		Simulator sim = jBotEvolver.createSimulator();
		Robot r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		NeuralNetworkController c =(NeuralNetworkController)Controller.getController(sim, r, jBotEvolver.getArguments().get("--controllers"));
		network = (WrapperNetwork)c.getNeuralNetwork();
		numberInputs = network.getNumberOfInputNeurons();
		numberOutputs = network.getNumberOfOutputNeurons();
		
		randomNumberGenerator = new Random(Integer.parseInt(jBotEvolver.getArguments().get("--random-seed").getCompleteArgumentString()));
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
		
		population = new NEATEncogPopulation(numberInputs, numberOutputs, populationSize, network, bootstrap);
		
		population.setInitialConnectionDensity(1.0);// not required, but speeds training
		population.setRandomNumberFactory(new JBotEvolverRandomFactory(generationRandomSeed));
		population.reset();

		//encog's structure requires the fitness function and the EA within the population
		
		if(jBotEvolver.getArguments().get("--evalmanager") != null)
			this.evaluator = (NEATEvaluation)EvaluationFunction.getEvaluationFunction(jBotEvolver.getArguments().get("--evalmanager"));
		else
			this.evaluator  = new SOEvaluationManager(new Arguments(""));

		trainer = constructPopulationTrainer();
	}
	
	public TrainEAJBot constructPopulationTrainer() {
		final TrainEAJBot result = new TrainEAJBot(population, this.evaluator);
		result.setThreadCount(getPopulationSize());
		
		OriginalNEATSpeciation speciation = new OriginalNEATSpeciation();
		//coefficient 3
		speciation.setConstMatched(3.0);
		//C_t
		speciation.setCompatibilityThreshold(3.0);
		result.setSpeciation(speciation);
		
		result.setSelection(new TruncationSelection(result, 0.2));
		
		final CompoundOperator weightMutation = new CompoundOperator();
		
		boolean continuousNetwork = network instanceof LayeredContinuousNeuralNetwork || network instanceof ERNEATContinuousNetwork;
		
		if(continuousNetwork) {
			System.out.println("####### Continuous Network");
			weightMutation.getComponents().add(0.5,new NEATMutateWeights(new SelectProportion(0.1),new MutatePerturbLinkWeight(1)));
			weightMutation.getComponents().add(0.5,new NEATMutateNeuronDecays(new SelectContinuousProportion(0.1),new MutatePerturbNeuronDecay(1)));
			
			weightMutation.getComponents().finalizeStructure();
			result.setChampMutation(weightMutation);
			
			result.addOperation(mutateLinkWeightRate, weightMutation);
			
		} else {
			System.out.println("####### Discrete Network");
			weightMutation.getComponents().add(1.0,new NEATMutateWeights(new SelectProportion(0.1),new MutatePerturbLinkWeight(1)));
			
			weightMutation.getComponents().finalizeStructure();
			result.setChampMutation(weightMutation);
			
			result.addOperation(mutateLinkWeightRate, weightMutation);
		}
		
		if(network instanceof ParameterNeuralNetwork) {
			result.addOperation(parameterAddRate, new NEATMutateAddParameterNode());
			result.addOperation(parameterRemoveRate, new NEATMutateRemoveParameterNode());
			result.addOperation(parameterChangeRate, new NEATMutateChangeParameterNode(1.0));
		}
		
		
		result.addOperation(crossoverRate, new NEATCrossoverJBot());
		
		if(continuousNetwork) {
			result.addOperation(addNodeRate, new NEATMutateAddContinuousNode());
		} else {
			result.addOperation(addNodeRate, new NEATMutateAddNodeJBot());
		}
		
		result.addOperation(addLinkRate, new NEATMutateAddLinkJBot());
		
		result.getOperators().finalizeStructure();
		
		result.setCODEC(network.getCODEC());
		
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

	public Genome getBestGenome(){
		return this.population.getBestGenome();
	}

	public void evolvePopulation(JBotEvolver jBotEvolver, TaskExecutor taskExecutor) {
		
		generationsElapsed++;
		//System.out.println("GENERATION " + generationsElapsed);
		randomNumberGenerator.setSeed(getGenerationRandomSeed());
		
		setGenerationRandomSeed(randomNumberGenerator.nextInt());
		trainer.setRandomNumberFactory(new JBotEvolverRandomFactory(getGenerationRandomSeed()));
		
		evaluator.setupEvolution(jBotEvolver, taskExecutor, this.numberOfSamplesPerChromosome,this.generationRandomSeed);
		population.setRandomNumberFactory(new JBotEvolverRandomFactory(generationRandomSeed));
		trainer.iteration();
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
		
		for(MLMethod key : this.stats.keySet()){
			TaskStatistics stats = this.stats.get(key);
			double objectiveValue = stats.get(objectiveKey);
			this.bestFitness = Math.max(objectiveValue, bestFitness);
			this.worstFitness = Math.min(objectiveValue, worstFitness);
			avgFitness += objectiveValue;
		}
		
		this.avgFitness /= this.stats.size();
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

	public Map<MLMethod, TaskStatistics> getStatistics(){
		return this.stats;
	}
	
	@Override
	public void setupIndividual(Robot r) {
		Genome best = getBestGenome();
		MLMethod net = population.getCODEC().decode(best);
		setIndividualMLController(r, net);
	}
	
	public void setMLControllers(ArrayList<Robot> robots, MLMethod method) {
		for(Robot r : robots) {
			setIndividualMLController(r, method);
		}
	}
	
	public void setIndividualMLController(Robot r, MLMethod method) {
		NeuralNetworkController controller = (NeuralNetworkController) r.getController();
		WrapperNetwork wrapper = (WrapperNetwork)controller.getNeuralNetwork();
		wrapper.setNetwork(method);
		controller.setNeuralNetwork(wrapper);
		controller.reset();
	}
	
	public NEATPopulation getPopulation() {
		return population;
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
		if(trainer != null)
			return this.trainer.getGenomesEvaluated();
		return 0;
	}
	
	public void stopEvolution() {
		if(trainer != null)
			trainer.stopEvolution();
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
		
		if(population.getBestGenome() == null)
			return null;
		
		MLMethod net = population.getCODEC().decode(population.getBestGenome());

		if(population.getCODEC() instanceof ParameterNEATCODEC)
			return new Chromosome(ParameterNeuralNetwork.getWeights((LayeredANN)net), 1);
		if(population.getCODEC() instanceof NEATContinuousCODEC)
			return new Chromosome(ERNEATContinuousNetwork.getWeights((NEATContinuousNetwork)net), 1);
		if(population.getCODEC() instanceof LayeredContinuousNEATCODEC)
			return new Chromosome(LayeredContinuousNeuralNetwork.getWeights((LayeredANN)net), 1);
		if(population.getCODEC() instanceof LayeredNEATCODEC)
			return new Chromosome(LayeredNeuralNetwork.getWeights((LayeredANN)net), 1);
		
		return new Chromosome(ERNEATNetwork.getWeights((NEATNetwork)net), 1);
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
}