package evolutionaryrobotics.mains;

import simulation.Simulator;
import evolutionaryrobotics.Main;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import experiments.CoevolutionExperiment;
import factories.EvaluationFunctionFactory;
import factories.ExperimentFactory;
import factories.PopulationFactory;
import gui.ResultViewerGui;
import gui.renderer.TwoDRendererDebug;

public class ResultViewerMain extends Main {

	private CoevolutionExperiment coevolutionExperiment;
	private Population populationA;

	public ResultViewerMain() {
		gui = new ResultViewerGui(simulator, new TwoDRendererDebug(simulator),
				this);
	}

	public Simulator setupSimulator() throws Exception {

		simRandom.setSeed(randomSeed);

		if(simulator != null)
			simulator.stop();
		
		simulator = new Simulator(simRandom);

		if (populationArguments.getArgumentIsDefined("showbestCoevolved")) {

			experiment = new ExperimentFactory(simulator)
					.getCoevolutionExperiment(experimentArguments,
							environmentArguments, robotsArguments,
							controllersArguments);

			populationA = new PopulationFactory(simulator)
					.getCoevolvedPopulation(populationArguments,
							experiment.getGenomeLength(), "A");

			Population populationB = new PopulationFactory(simulator)
					.getCoevolvedPopulation(populationArguments,
							experiment.getGenomeLength(), "B");

			int popANum = (populationArguments
					.getArgumentIsDefined("populationIndexA")) ? populationArguments
					.getArgumentAsInt("populationIndexA") : 0;
			int popBNum = (populationArguments
					.getArgumentIsDefined("populationIndexB")) ? populationArguments
					.getArgumentAsInt("populationIndexB") : 0;
			Chromosome bestChromosomeA = populationA
					.getTopChromosome(popANum + 1)[popANum];
			Chromosome bestChromosomeB = populationB
					.getTopChromosome(popBNum + 1)[popBNum];

			coevolutionExperiment = (new ExperimentFactory(simulator))
					.getCoevolutionExperiment(experimentArguments,
							environmentArguments, robotsArguments,
							controllersArguments);

			coevolutionExperiment.setChromosome(bestChromosomeA,
					bestChromosomeB);
			simulator.setEnvironment(coevolutionExperiment.getEnvironment());
			evaluationFunction = (new EvaluationFunctionFactory(simulator))
					.getEvaluationFunction(evaluationArguments,
							coevolutionExperiment);
			coevolutionExperiment.setNumberOfStepsPerRun(populationA
					.getNumberOfStepsPerSample());
		} else {
			createExperimentAndEvaluationFunction();
			population = new PopulationFactory(simulator).getPopulation(
					populationArguments, experiment.getGenomeLength());
			Chromosome bestChromosome = population.getBestChromosome();
			experiment.setNumberOfStepsPerRun(population
					.getNumberOfStepsPerSample());
			experiment.setChromosome(bestChromosome);
		}

		return simulator;
	}

	@Override
	public void execute() throws Exception {
		setupSimulator();

		if (populationArguments.getArgumentIsDefined("showbestCoevolved")) {
			gui.run(simulator, renderer, coevolutionExperiment,
					evaluationFunction, populationA.getNumberOfStepsPerSample());
		} else {
			gui.run(simulator, renderer, experiment, evaluationFunction,
					population.getNumberOfStepsPerSample());
		}
	}

	public Simulator getSimulator() {
		return simulator;
	}

	public EvaluationFunction getEvaluationFunction() {
		return evaluationFunction;
	}

	public static void main(String[] args) {
		try {
			new ResultViewerMain();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
