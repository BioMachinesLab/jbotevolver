package evolutionaryrobotics.parallel;

import java.io.IOException;

import simulation.Simulator;
import simulation.util.Arguments;

import comm.ClientPriority;
import client.Client;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;

public class ParallellerClient {

	protected Client client;
	protected int numberOfChromosomesLeft;
	protected long randomSeed;
	protected long startTime;
	protected int totalNumberOfEvaluationsNecessary;
	protected Population population;
	protected DiskStorage diskStorage;
	protected Arguments evaluationArguments;
	protected Arguments controllerArguments;
	protected Arguments experimentArguments;
	protected Arguments environmentArguments;
	protected Arguments robotArguments;
	protected Simulator simulator;

	public ParallellerClient(Simulator simulator, ClientPriority priority,
			String masterAddress, int masterPort, String codeServerAddress,
			int codeServerPort, Arguments experimentArguments,
			Arguments environmentArguments, Arguments robotArguments,
			Arguments controllerArguments, Population population,
			Arguments evaluationArguments, DiskStorage diskStorage) {
		this.client = new Client(priority, masterAddress, masterPort,
				codeServerAddress, codeServerPort);
		this.simulator = simulator;
		this.diskStorage = diskStorage;
		this.evaluationArguments = evaluationArguments;
		this.controllerArguments = controllerArguments;
		this.experimentArguments = experimentArguments;
		this.environmentArguments = environmentArguments;
		this.robotArguments = robotArguments;
		this.population = population;

		if (population != null && population.getPopulationSize()
				- population.getNumberOfChromosomesEvaluated() == 0) {
			population.createNextGeneration();
		}

		startTime = System.currentTimeMillis();
	}

	public void execute() {
		// while(!population.evolutionDone()){
		try {
			// totalNumberOfEvaluationsNecessary =
			// (population.getNumberOfGenerations() -
			// population.getNumberOfCurrentGeneration()) *
			// population.getPopulationSize();
			
			Chromosome chromosome = null;

			while (!population.evolutionDone()) {
				
				int numberOfTasksLaunched = 0;
				
				while ((chromosome = population.getNextChromosomeToEvaluate()) != null) {
					
					int samplesPerIndividual = population
							.getNumberOfSamplesPerChromosome();
					int stepsPerRun = population.getNumberOfStepsPerSample();
					SlaveTask task = new SlaveTask(numberOfTasksLaunched, experimentArguments,
							environmentArguments, robotArguments,
							controllerArguments, evaluationArguments,
							(long) population.getGenerationRandomSeed(),
							chromosome, samplesPerIndividual, stepsPerRun);
					client.commit(task);
					numberOfTasksLaunched++;
					
//					 task.run();
//					 System.out.println(i + " -> " + ((SlaveResult)
//					 task.getResult()).getFitness());
//					 population.setEvaluationResult(chromosome, ((SlaveResult)
//					 task.getResult()).getFitness());
				}
				for (int i = 0; i < numberOfTasksLaunched; i++) {
					SlaveResult result = (SlaveResult) client.getNextResult();
					// System.out.println(result.getChromosomeId() +" - "+
					// result.getFitness());
					population.setEvaluationResultForId(
							result.getChromosomeId(), result.getFitness());
				}
				
				//this is for weird situations in which some of the tasks crash
				if(population.getNumberOfChromosomesEvaluated() < population.getPopulationSize())
					throw new IOException();
				
				System.out.println(this.toString()
						+ ": Received fitness for generation "
						+ population.getNumberOfCurrentGeneration());
				diskStorage.savePopulation(population, simulator.getRandom());
				// simulator.getRandom().setSeed(population.getGenerationRandomSeed());
				population.createNextGeneration();
				// randomSeed = simulator.getRandom().nextLong();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(this.toString() + ": Done");
	}
	
	public int getNumberOfCurrentGeneration() {
		return population.getNumberOfCurrentGeneration();
	}
	
	public int getNumberOfGenerations() {
		return population.getNumberOfGenerations();
	}

}
