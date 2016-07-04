package evolutionaryrobotics.evolution;

import java.text.DecimalFormat;
import java.util.Collections;

import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
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
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import tests.Cronometer;

public class NEATEvolution extends Evolution {

	protected NEATGADescriptor descriptor;
	protected NEATPopulation population;
	protected DiskStorage diskStorage;
	protected String output = "";
	protected DecimalFormat df = new DecimalFormat("#.##");

	public NEATEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
		population = (NEATPopulation) jBotEvolver.getPopulation();

		if (jBotEvolver.getArguments().get("--population").getArgumentIsDefined("generations"))
			population.setNumberOfGenerations(
					jBotEvolver.getArguments().get("--population").getArgumentAsInt("generations"));
		population.setGenerationRandomSeed(jBotEvolver.getRandomSeed());

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
		
		NEATGeneticAlgorithmWrapper algorithm;

		int i = population.getNumberOfCurrentGeneration();

		if (i == 0) {
			algorithm = new NEATGeneticAlgorithmWrapper(descriptor, this);
		} else {
			algorithm = new NEATGeneticAlgorithmWrapper(descriptor, this);
			algorithm.loadPopulation(population.getNEATPopulation4J());
		}

		algorithm.pluginFitnessFunction(new PreEvaluatedFitnessFunction(Collections.<Chromosome, Float> emptyMap()));
		algorithm.pluginCrossOver(new NEATCrossover());
		algorithm.pluginMutator(new NEATMutator());
		algorithm.pluginParentSelector(new TournamentSelector());

		if (i == 0) {
			algorithm.createPopulation();
			population.setGenerationRandomSeed(jBotEvolver.getRandomSeed());
			population.createRandomPopulation();
			population.setNEATPopulation4J((NEATPopulation4J) algorithm.population());
			population.getNEATPopulation4J().setSpecies(algorithm.getSpecies());
		} 

		if (!population.evolutionDone())
			taskExecutor.setTotalNumberOfTasks(
					(population.getNumberOfGenerations() - population.getNumberOfCurrentGeneration())
							* population.getPopulationSize());

		double highestFitness = population.getHighestFitness();

		while (!population.evolutionDone() && executeEvolution) {
			double d = Double.valueOf(df.format(highestFitness));

			taskExecutor.setDescription(output + " " + population.getNumberOfCurrentGeneration() + "/"
					+ population.getNumberOfGenerations() + " " + d);

			algorithm.runEpoch();

			i++;

			if (executeEvolution) {
				System.out.println("\nGeneration " + getPopulation().getNumberOfCurrentGeneration() + "\tHighest: "
						+ population.getHighestFitness() + "\tAverage: " + population.getAverageFitness() + "\tLowest: "
						+ population.getLowestFitness());

				try {
					diskStorage.savePopulation(population);
				} catch (Exception e) {
					e.printStackTrace();
				}

				highestFitness = population.getHighestFitness();
			}

			if (population.evolutionDone())
				break;

			population.createNextGeneration();
		}

		InnovationDatabase db = algorithm.innovationDatabase();
		System.out.println("Innovation Database Stats - Hits: " + db.hits + " - misses: " + db.misses);
	}

	protected void configureDescriptor(Arguments args) {

		int popSize = population.getPopulationSize();

		int[] neurons = getInputOutputNeurons();

		int inputNodes = neurons[0];
		int outputNodes = neurons[1];

		double pXover = args.getArgumentAsDoubleOrSetDefault("pXover", 0.2);
		double pAddLink = args.getArgumentAsDoubleOrSetDefault("pAddLink", 0.05);
		double pAddNode = args.getArgumentAsDoubleOrSetDefault("pAddNode", 0.03);
		double pToggleLink = args.getArgumentAsDoubleOrSetDefault("pToggleLink", 0.0);
		double pMutation = args.getArgumentAsDoubleOrSetDefault("pMutation", 0.25);
		double pMutateBias = args.getArgumentAsDoubleOrSetDefault("pMutateBias", 0.3);
		double pWeightReplaced = args.getArgumentAsDoubleOrSetDefault("pWeightReplaced", 0.0);
		double excessCoeff = args.getArgumentAsIntOrSetDefault("excessCoeff", 1);
		double disjointCoeff = args.getArgumentAsIntOrSetDefault("disjointCoeff", 1);
		double weightCoeff = args.getArgumentAsDoubleOrSetDefault("weightCoeff", 0.4);
		double threshold = args.getArgumentAsDoubleOrSetDefault("threshold", 0.5);
		double thresholdChange = args.getArgumentAsDoubleOrSetDefault("thresholdChange", 0.05);
		boolean naturalOrder = args.getArgumentAsIntOrSetDefault("naturalOrder", 0) == 1;
		int maxSpecieAge = args.getArgumentAsIntOrSetDefault("maxSpecieAge", 15);
		int specieAgeThreshold = args.getArgumentAsIntOrSetDefault("specieAgeThreshold", 80);
		int specieYouthThreshold = args.getArgumentAsIntOrSetDefault("specieYouthThreshold", 10);
		double agePenalty = args.getArgumentAsDoubleOrSetDefault("agePenalty", 0.7);
		double youthBoost = args.getArgumentAsDoubleOrSetDefault("youthBoost", 1.2);
		int specieCount = args.getArgumentAsIntOrSetDefault("specieCount", 5);
		double survialThreshold = args.getArgumentAsDoubleOrSetDefault("survialThreshold", 0.2);
		boolean featureSelection = args.getArgumentAsIntOrSetDefault("featureSelection", 0) == 1;
		int extraAlleles = args.getArgumentAsIntOrSetDefault("extraAlleles", 0);
		boolean eleEvents = args.getArgumentAsIntOrSetDefault("eleEvents", 0) == 1;
		double eleSurvivalCount = args.getArgumentAsDoubleOrSetDefault("eleSurvivalCount", 0.1);
		int eleEventTime = args.getArgumentAsIntOrSetDefault("eleEventTime", 1000);
		boolean recurrencyAllowed = args.getArgumentAsIntOrSetDefault("recurrencyAllowed", 1) == 1;
		boolean keepBestEver = args.getArgumentAsIntOrSetDefault("keepBestEver", 0) == 1;
		double terminationValue = args.getArgumentAsDoubleOrSetDefault("terminationValue", 0.1);
		double maxPerturb = args.getArgumentAsDoubleOrSetDefault("maxPerturb", 0.5);
		double maxBiasPerturb = args.getArgumentAsDoubleOrSetDefault("maxBiasPerturb", 0.1);
		boolean copyBest = args.getArgumentAsIntOrSetDefault("copyBest", 1) == 1;

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

	protected int[] getInputOutputNeurons() {
		Simulator sim = jBotEvolver.createSimulator();
		Robot r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		Controller c = Controller.getController(sim, r, jBotEvolver.getArguments().get("--controllers"));

		int in = 0;
		int out = 0;

		if (c instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController) c;
			in = controller.getNumberOfInputs();
			out = controller.getNumberOfOutputs();
		}

		return new int[] { in, out };
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
