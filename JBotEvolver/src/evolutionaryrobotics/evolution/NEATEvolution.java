package evolutionaryrobotics.evolution;

import java.text.DecimalFormat;
import java.util.Collections;

import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.neat.NEATGeneticAlgorithmWrapper;
import evolutionaryrobotics.evolution.neat.PreEvaluatedFitnessFunction;
import evolutionaryrobotics.evolution.neat.core.InnovationDatabase;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;
import evolutionaryrobotics.evolution.neat.core.mutators.NEATMutator;
import evolutionaryrobotics.evolution.neat.core.pselectors.TournamentSelector;
import evolutionaryrobotics.evolution.neat.core.xover.NEATCrossover;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.populations.NEATPopulation;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;

public class NEATEvolution extends Evolution {
	
	private NEATGADescriptor descriptor;
	private NEATPopulation population;
	protected DiskStorage diskStorage;
	protected String output = "";
	protected DecimalFormat df = new DecimalFormat("#.##");
	
	public NEATEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
		population = (NEATPopulation)jBotEvolver.getPopulation();
		descriptor = new NEATGADescriptor();
		configureDescriptor(args);
		
		if (jBotEvolver.getArguments().get("--output") != null) {
			output = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
			diskStorage = new DiskStorage(jBotEvolver.getArguments().get("--output").getCompleteArgumentString());
			try {
				diskStorage.start();
				diskStorage.saveCommandlineArguments(jBotEvolver.getArguments());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	@Override
	public void executeEvolution() {
		
		NEATGeneticAlgorithmWrapper algorithm = new NEATGeneticAlgorithmWrapper(descriptor, this);
		
		algorithm.pluginFitnessFunction(new PreEvaluatedFitnessFunction(Collections.<Chromosome, Float> emptyMap()));
		algorithm.pluginCrossOver(new NEATCrossover());
		algorithm.pluginMutator(new NEATMutator());
		algorithm.pluginParentSelector(new TournamentSelector());
		algorithm.createPopulation();
		
		int i = population.getNumberOfCurrentGeneration();
		
		if(population.getNumberOfCurrentGeneration() == 0) {
			population.setGenerationRandomSeed(jBotEvolver.getRandomSeed());
			population.createRandomPopulation();
		}
		
		if(!population.evolutionDone())
			taskExecutor.setTotalNumberOfTasks((population.getNumberOfGenerations()-population.getNumberOfCurrentGeneration())*population.getPopulationSize());
		
		if(i == 0)
			population.setNEATPopulation4J((NEATPopulation4J)algorithm.population());
		else
			algorithm.loadPopulation(population.getNEATPopulation4J());
		
		double highestFitness = 0;
		
		while (!population.evolutionDone() && executeEvolution) {
			
			double d = Double.valueOf(df.format(highestFitness));
			taskExecutor.setDescription(output+" "+population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations() + " " + d);
			
			algorithm.runEpoch();

			i++;
			
			if(executeEvolution) {
				  System.out.println("\nGeneration "+getPopulation().getNumberOfCurrentGeneration()+
							"\tHighest: "+population.getHighestFitness()+
							"\tAverage: "+population.getAverageFitness()+
							"\tLowest: "+population.getLowestFitness());
				  
				  try {
						diskStorage.savePopulation(population);
					} catch(Exception e) {e.printStackTrace();}
					
					highestFitness = population.getHighestFitness();
					population.createNextGeneration();
			}
		}
		
        InnovationDatabase db = algorithm.innovationDatabase();
		System.err.println("Innovation Database Stats - Hits:" + db.hits + " - misses:" + db.misses);
		
	}
	
	private void configureDescriptor(Arguments args) {

		int popSize = population.getPopulationSize();
		
		int[] neurons = getInputOutputNeurons();
		
		int inputNodes = neurons[0];
		int outputNodes = neurons[1];
		
		double pXover = 0.2;
		double pAddLink = 0.05;
		double pAddNode = 0.03;
		double pToggleLink = 0.0;
		double pMutation = 0.25;
		double pMutateBias = 0.3;
		double pWeightReplaced = 0.0;
		double excessCoeff = 1;
		double disjointCoeff = 1;
		double weightCoeff = 0.4;
		double threshold = 0.5;
		double thresholdChange = 0.05;
		boolean naturalOrder = false;
		int maxSpecieAge = 15;
		int specieAgeThreshold = 80;
		int specieYouthThreshold = 10;
		double agePenalty = 0.7;
		double youthBoost = 1.2;
		int specieCount = 5;
		double survialThreshold = 0.2;
		boolean featureSelection = false;
		int extraAlleles = 0;
		boolean eleEvents = false;
		double eleSurvivalCount = 0.1;
		int eleEventTime = 1000;
		boolean recurrencyAllowed = true;
		boolean keepBestEver = false;
		double terminationValue = 0.1;
		double maxPerturb = 0.5;
		double maxBiasPerturb = 0.1;
        boolean copyBest = true;
		
		descriptor.setPAddLink(pAddLink);
		descriptor.setPAddNode(pAddNode);
		descriptor.setPToggleLink(pToggleLink);
		descriptor.setPMutateBias(pMutateBias);
		descriptor.setPXover(pXover);
		descriptor.setPMutation(pMutation);
		descriptor.setInputNodes(inputNodes);
		descriptor.setOutputNodes(outputNodes);
		descriptor.setNaturalOrder(naturalOrder);
		descriptor.setPopulationSize(popSize);
		descriptor.setDisjointCoeff(disjointCoeff);
		descriptor.setExcessCoeff(excessCoeff);
		descriptor.setWeightCoeff(weightCoeff);
		descriptor.setThreshold(threshold);
		descriptor.setCompatabilityChange(thresholdChange);
		descriptor.setMaxSpecieAge(maxSpecieAge);
		descriptor.setSpecieAgeThreshold(specieAgeThreshold);
		descriptor.setSpecieYouthThreshold(specieYouthThreshold);
		descriptor.setAgePenalty(agePenalty);
		descriptor.setYouthBoost(youthBoost);
		descriptor.setSpecieCount(specieCount);
		descriptor.setPWeightReplaced(pWeightReplaced);
		descriptor.setSurvivalThreshold(survialThreshold);
		descriptor.setFeatureSelection(featureSelection);
		descriptor.setExtraFeatureCount(extraAlleles);
		descriptor.setEleEvents(eleEvents);
		descriptor.setEleSurvivalCount(eleSurvivalCount);
		descriptor.setEleEventTime(eleEventTime);
		descriptor.setRecurrencyAllowed(recurrencyAllowed);
		descriptor.setKeepBestEver(keepBestEver);
		descriptor.setTerminationValue(terminationValue);
		descriptor.setMaxPerturb(maxPerturb);
		descriptor.setMaxBiasPerturb(maxBiasPerturb);
		descriptor.setCopyBest(copyBest);
	}
	
	private int[] getInputOutputNeurons() {
		Simulator sim = jBotEvolver.createSimulator();
		Robot r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		Controller c = Controller.getController(sim,r, jBotEvolver.getArguments().get("--controllers"));
		
		int in = 0;
		int out = 0;
		
		if(c instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)c;
			in = controller.getNumberOfInputs();
			out = controller.getNumberOfOutputs();
		}
		
		return new int[]{in,out};
	}
	
	public JBotEvolver getJBotEvolver() {
		return jBotEvolver;
	}
	
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	@Override
	public Population getPopulation() {
		return population;
	}
	
	public boolean continueExecuting() {
		return executeEvolution;
	}
	
}
