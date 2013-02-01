package evolutionaryrobotics.parallel;

import java.io.IOException;

import simulation.Simulator;
import simulation.util.Arguments;

import comm.ClientPriority;
import client.Client;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import experiments.CoevolutionExperiment;
import experiments.Experiment;
import factories.ExperimentFactory;
import factories.PopulationFactory;

public class ParallellerCoevolutionClient extends ParallellerClient {

	private int numberOfOpponents;
	private Population populationB;

	private Chromosome[] topA;
	private Chromosome[] topB;

	public ParallellerCoevolutionClient(Simulator simulator,
			ClientPriority priority, String masterAddress, int masterPort,
			String codeServerAddress, int codeServerPort,
			Arguments experimentArguments, Arguments environmentArguments,
			Arguments robotArguments, Arguments controllerArguments,
			Arguments populationArguments, Arguments evaluationArguments,
			Experiment experiment, DiskStorage diskStorage,
			int numberOfOpponents) {
		super(simulator, priority, masterAddress, masterPort,
				codeServerAddress, codeServerPort, experimentArguments,
				environmentArguments, robotArguments, controllerArguments,
				null, evaluationArguments, diskStorage);

		this.numberOfOpponents = numberOfOpponents;
		topA = new Chromosome[numberOfOpponents];
		topB = new Chromosome[numberOfOpponents];
		try {
			population = new PopulationFactory(simulator)
					.getCoevolvedPopulation(populationArguments,
							experiment.getGenomeLength(), "A");

			populationB = new PopulationFactory(simulator)
					.getCoevolvedPopulation(populationArguments,
							experiment.getGenomeLength(), "B");

			if (populationB.getPopulationSize()
					- populationB.getNumberOfChromosomesEvaluated() == 0) {
				populationB.createNextGeneration();
			}

			if (population.getPopulationSize()
					- population.getNumberOfChromosomesEvaluated() == 0) {
				population.createNextGeneration();
			}

			// this.populationA = new
			// PopulationFactory(simulator).getPopulation(
			// populationArguments, experiment.getGenomeLength());
			//
			// if (populationA.getPopulationSize()
			// - populationA.getNumberOfChromosomesEvaluated() == 0) {
			// populationA.createNextGeneration();
			// }
			//
			// this.populationB = new
			// PopulationFactory(simulator).getPopulation(
			// populationArguments, experiment.getGenomeLength());
			//
			// if (populationB.getPopulationSize()
			// - populationB.getNumberOfChromosomesEvaluated() == 0) {
			// populationB.createNextGeneration();
			// }

			topB = populationB.getTopChromosome(numberOfOpponents);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		;

		startTime = System.currentTimeMillis();
	}

	public void execute() {
		// while(!population.evolutionDone()){
		try {
			// totalNumberOfEvaluationsNecessary =
			// (population.getNumberOfGenerations() -
			// population.getNumberOfCurrentGeneration()) *
			// population.getPopulationSize();

			while (!populationB.evolutionDone()) {
				evalutePopulation(population, topB);
				System.out.println(this.toString()
						+ ": Received fitness for generation A"
						+ population.getNumberOfCurrentGeneration());
				// simulator.getRandom().setSeed(population.getGenerationRandomSeed());
				topA = population.getTopChromosome(numberOfOpponents);

				evalutePopulation(populationB, topA);
				System.out.println(this.toString()
						+ ": Received fitness for generation B"
						+ populationB.getNumberOfCurrentGeneration());
				topB = populationB.getTopChromosome(numberOfOpponents);

				diskStorage.savePopulation(population, populationB,
						simulator.getRandom());
				// simulator.getRandom().setSeed(population.getGenerationRandomSeed());
				population.createNextGeneration();
				populationB.createNextGeneration();

				// randomSeed = simulator.getRandom().nextLong();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(this.toString() + ": Done");
	}

	private void evalutePopulation(Population population, Chromosome[] top) {
		for (int i = 0; i < population.getPopulationSize(); i++) {
			Chromosome chromosome = population.getNextChromosomeToEvaluate();
			int samplesPerIndividual = population
					.getNumberOfSamplesPerChromosome();
			int stepsPerRun = population.getNumberOfStepsPerSample();
			CoevolutionSlaveTask task = new CoevolutionSlaveTask(i,
					experimentArguments, environmentArguments, robotArguments,
					controllerArguments, evaluationArguments,
					(long) population.getGenerationRandomSeed(),
					chromosome.getID(), chromosome, top, samplesPerIndividual,
					stepsPerRun);
			client.commit(task);
			// task.run();
			// System.out.println(i + " -> "
			// + ((SlaveResult) task.getResult()).getFitness());
			// population.setEvaluationResult(chromosome,
			// ((SlaveResult) task.getResult()).getFitness());
		}
		for (int i = 0; i < population.getPopulationSize(); i++) {
			SlaveResult result = (SlaveResult) client.getNextResult();
			// System.out.println(result.getChromosomeId() +" - "+
			// result.getFitness());
			population.setEvaluationResultForId(result.getChromosomeId(),
					result.getFitness());
		}

	}

}
