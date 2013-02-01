package evolutionaryrobotics;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import simulation.Simulator;
import simulation.util.Arguments;
import simulation.util.SimRandom;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.parallel.Client;
import evolutionaryrobotics.parallel.Master;
import evolutionaryrobotics.parallel.MultiMaster;
import evolutionaryrobotics.parallel.ParallellerClient;
import evolutionaryrobotics.parallel.ParallellerCoevolutionClient;
import evolutionaryrobotics.parallel.Slave;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import evolutionaryrobotics.util.Util;
import experiments.CoevolutionExperiment;
import experiments.Experiment;
import factories.EvaluationFunctionFactory;
import factories.ExperimentFactory;
import factories.GuiFactory;
import factories.MasterFactory;
import factories.PopulationFactory;
import gui.Gui;
import gui.renderer.Renderer;

public class Main {
	protected Arguments experimentArguments = null;
	protected Arguments environmentArguments = null;
	protected Arguments robotsArguments = null;
	protected Arguments controllersArguments = null;
	protected Arguments populationArguments = null;
	protected Arguments evaluationArguments = null;
	protected Arguments guiArguments = null;
	protected Arguments masterArguments = null;

	protected DiskStorage diskStorage = new DiskStorage(null);
	protected String[] commandlineArguments = null;

	protected Renderer renderer = null;
	protected Gui gui = null;
	protected EvaluationFunction evaluationFunction = null;
	protected Population population;
	protected Experiment experiment;
	protected SimRandom simRandom = new SimRandom();
	protected Simulator simulator = new Simulator(simRandom);

	protected boolean slaveConnectsToMaster = false;

	protected int slavePort = 8000;
	protected String masterAddress = null;
	protected int masterPort = MultiMaster.DEFAULTCLIENTMASTERPORT;

	protected boolean actAsMaster = false;
	protected boolean actAsClient = false;
	protected boolean actAsSlave = false;
	protected boolean actAsPPClient = false;
	protected boolean actAsCoevolutionClient = false;

	protected long randomSeed = 0;
	
	protected String parentFolder = "";

	public Main() {}
	
	public Main(String[] args) throws IOException {
		loadArguments(args);
	}
	
	protected void loadArguments(String[] args) throws IOException{
		
		HashMap<String,Arguments> arguments = Arguments.parseArgs(args);
		
		guiArguments = arguments.get("--gui");
		experimentArguments = arguments.get("--experiment");
		environmentArguments = arguments.get("--environment");
		robotsArguments = arguments.get("--robots");
		controllersArguments = arguments.get("--controllers");
		populationArguments = arguments.get("--population");
		evaluationArguments = arguments.get("--evaluation");
		
		if(arguments.get("--random-seed") != null)
			randomSeed = Long.parseLong(arguments.get("--random-seed").getCompleteArgumentString());			
		
		simRandom.setSeed(randomSeed);
		
		diskStorage = new DiskStorage(arguments.get("--output").getCompleteArgumentString());
		
		String absolutePath = (new File("./"+arguments.get("--output").getCompleteArgumentString())).getCanonicalPath();
		
		if(parentFolder.isEmpty())
			populationArguments.setArgument("parentfolder", absolutePath);
		else
			populationArguments.setArgument("parentfolder", parentFolder);
	
		if(arguments.get("slave") != null) {
			actAsSlave = true;
			try {
				slavePort = Integer.parseInt(arguments.get("--slave").getCompleteArgumentString());
			} catch (NumberFormatException ne) {
				slaveConnectsToMaster = true;
				masterAddress = arguments.get("--slave").getCompleteArgumentString();
			}
		}
		
		if(arguments.get("--master") != null) {
			actAsMaster = true;
			masterArguments = arguments.get("--master");
		}
		
		if(arguments.get("--client") != null) {
			actAsClient = true;
			masterArguments = arguments.get("--client");
		}
		
		if(arguments.get("--paralleler-client") != null) {
			actAsPPClient = true;
			masterArguments = arguments.get("--paralleler-client");
		}
		
		if(arguments.get("--coevolution") != null) {
			actAsCoevolutionClient = true;
			masterArguments = arguments.get("--coevolution");
		}
		
		//split on whitespace
		commandlineArguments = arguments.get("commandline").getCompleteArgumentString().split("\\s+");
	}
	
	public void loadFile(String filename, String extraArguments) throws IOException{

		savePath(filename);
		
		String fileContents = Arguments.readContentFromFile(filename);
		fileContents+="\n"+extraArguments;
		
		String[] args = Arguments.readOptionsFromString(fileContents);
		
		loadArguments(args);
	}
	
	public void savePath(String file) {
		parentFolder = (new File(file)).getParent();
	}

	public void createExperimentAndEvaluationFunction() throws Exception {
		experiment = (new ExperimentFactory(simulator)).getExperiment(
				experimentArguments, environmentArguments, robotsArguments,
				controllersArguments);
		simulator.setEnvironment(experiment.getEnvironment());

		if (experiment == null) {
			throw new RuntimeException(
					"No experiment created -- experiment arguments: "
							+ experimentArguments);
		}

		evaluationFunction = null;
		if (evaluationArguments != null) {
			evaluationFunction = (new EvaluationFunctionFactory(simulator))
					.getEvaluationFunction(evaluationArguments, experiment);
		}
	}

	public void execute() throws Exception {
		if (actAsSlave && !actAsMaster) {
			if (slaveConnectsToMaster)
				while (true) {
					new Slave(masterAddress, 0, System.out, false);
					Thread.sleep((int) (Math.random() * 120000));
					// System.gc();
					// Thread.sleep((int)(Math.random()*30000));
					System.out.println("Trying to re-connect...");
				}
			else
				new Slave(slavePort, System.out, false);
		} else {
			if (populationArguments == null) {
				createExperimentAndEvaluationFunction();
				// simulator.setEnvironment(experiment.getEnvironment());
				gui = (new GuiFactory(simulator)).getGui(guiArguments,
						experiment);
				gui.run(simulator, renderer, experiment, evaluationFunction,
						experiment.getNumberOfStepsPerRun());
			} else if (populationArguments.getArgumentIsDefined("runbest")) {
				double fitness = 0;
				double maxFitness = Double.MIN_VALUE;
				double minFitness = Double.MAX_VALUE;

				simRandom.setSeed(randomSeed);
				gui = (new GuiFactory(simulator)).getGui(guiArguments,
						experiment);
				int numberOfRuns = (populationArguments
						.getArgumentIsDefined("samples")) ? populationArguments
						.getArgumentAsInt("samples") : 100;

				// int[] bins=new int[50];
				for (int i = 0; i < numberOfRuns; i++) {
					experiment = (new ExperimentFactory(simulator))
							.getExperiment(experimentArguments,
									environmentArguments, robotsArguments,
									controllersArguments);
					// experiment.getEnvironment();
					Population population = new PopulationFactory(simulator)
							.getPopulation(populationArguments,
									experiment.getGenomeLength());
					Chromosome bestChromosome = population.getBestChromosome();
					createExperimentAndEvaluationFunction();
					experiment.setChromosome(bestChromosome);
					// simulator.setEnvironment(experiment.getEnvironment());

					gui.run(simulator, renderer, experiment,
							evaluationFunction,
							population.getNumberOfStepsPerSample());
					double currentFitness = evaluationFunction.getFitness();// *1000.0
																			// /
																			// population.getNumberOfStepsPerSample();
					fitness += currentFitness;
					if (maxFitness < currentFitness)
						maxFitness = currentFitness;
					if (minFitness > currentFitness)
						minFitness = currentFitness;
					// System.out.println("Iteration "+i
					// +" ("+currentFitness+")");
					System.out.println(currentFitness);
					// bins[(int)(currentFitness)]++;
				}
				// if (evaluationFunction != null) {
				// System.out.println("Maximum\t"+maxFitness+"\tAverage\t"+fitness/numberOfRuns+"\tMinimum\t"+minFitness);
				// }
				// for(int i=0;i<bins.length;i++){
				// System.out.println(bins[i]);
				// }
				System.exit(0);

			} else if (populationArguments.getArgumentIsDefined("showbest")) {
				simRandom.setSeed(randomSeed);
				createExperimentAndEvaluationFunction();
				gui = (new GuiFactory(simulator)).getGui(guiArguments,
						experiment);
				Population population = new PopulationFactory(simulator)
						.getPopulation(populationArguments,
								experiment.getGenomeLength());
				Chromosome bestChromosome = population.getBestChromosome();
				experiment.setChromosome(bestChromosome);
				// simulator.setEnvironment(experiment.getEnvironment());
				gui.run(simulator, renderer, experiment, evaluationFunction,
						population.getNumberOfStepsPerSample());
				if (evaluationFunction != null) {
					System.out.println(evaluationFunction.getFitness());
				}
			} else if (populationArguments.getArgumentIsDefined("showbestCoevolved")) {
				simRandom.setSeed(randomSeed);

				experiment = new ExperimentFactory(simulator)
				.getCoevolutionExperiment(experimentArguments,
						environmentArguments, robotsArguments,
						controllersArguments);

				Population populationA = new PopulationFactory(simulator).getCoevolvedPopulation(
						populationArguments, experiment.getGenomeLength(),"A");

				if (populationA.getPopulationSize()
						- populationA.getNumberOfChromosomesEvaluated() == 0) {
					populationA.createNextGeneration();
				}

				Population populationB = new PopulationFactory(simulator).getCoevolvedPopulation(
						populationArguments, experiment.getGenomeLength(),"B");

				if (populationB.getPopulationSize()
						- populationB.getNumberOfChromosomesEvaluated() == 0) {
					populationB.createNextGeneration();
				}

				
				gui = (new GuiFactory(simulator)).getGui(guiArguments,
						experiment);
				Chromosome bestChromosomeA = populationA.getBestChromosome();
				Chromosome bestChromosomeB = populationB.getBestChromosome();
				
				CoevolutionExperiment coevolutionExperiment = (new ExperimentFactory(simulator)).getCoevolutionExperiment(
						experimentArguments, environmentArguments,
						robotsArguments, controllersArguments);
				
				coevolutionExperiment.setChromosome(bestChromosomeA, bestChromosomeB);
				simulator.setEnvironment(coevolutionExperiment.getEnvironment());
				EvaluationFunction evaluationFunction = (new EvaluationFunctionFactory(
						simulator)).getEvaluationFunction(evaluationArguments,
								coevolutionExperiment);
				gui.run(simulator, null, coevolutionExperiment, evaluationFunction,
						populationA.getNumberOfStepsPerSample());
				
				if (evaluationFunction != null) {
					System.out.println(evaluationFunction.getFitness());
				}
			} else {
				if (actAsCoevolutionClient) {
					diskStorage.start();
					diskStorage.saveCommandlineArguments(commandlineArguments);
					experiment = new ExperimentFactory(simulator)
							.getCoevolutionExperiment(experimentArguments,
									environmentArguments, robotsArguments,
									controllersArguments);
					ParallellerCoevolutionClient client = (new MasterFactory(
							simulator)).getCoevolutionPPClient(masterArguments,
							experimentArguments, environmentArguments,
							robotsArguments, controllersArguments,
							populationArguments, evaluationArguments,
							experiment, diskStorage);
					client.execute();
				} else if (actAsClient || actAsPPClient) {
					diskStorage.start();
					diskStorage.saveCommandlineArguments(commandlineArguments);
					experiment = (new ExperimentFactory(simulator))
							.getExperiment(experimentArguments,
									environmentArguments, robotsArguments,
									controllersArguments);
					simRandom.setSeed(randomSeed);
					Population population = new PopulationFactory(simulator)
							.getPopulation(populationArguments,
									experiment.getGenomeLength());
					System.out.println("Genome length: "
							+ experiment.getGenomeLength());
					if (actAsClient) {
						Client client = (new MasterFactory(simulator))
								.getClient(masterArguments,
										experimentArguments,
										environmentArguments, robotsArguments,
										controllersArguments, population,
										evaluationArguments, diskStorage);
						client.execute();
					} else {
						ParallellerClient client = (new MasterFactory(simulator))
								.getPPClient(masterArguments,
										experimentArguments,
										environmentArguments, robotsArguments,
										controllersArguments, population,
										evaluationArguments, diskStorage);
						client.execute();
					}
					diskStorage.close();
					// System.exit(0);
				} else if (actAsMaster) {
					diskStorage.start();
					diskStorage.saveCommandlineArguments(commandlineArguments);
					experiment = (new ExperimentFactory(simulator))
							.getExperiment(experimentArguments,
									environmentArguments, robotsArguments,
									controllersArguments);
					experiment.getEnvironment();
					Population population = new PopulationFactory(simulator)
							.getPopulation(populationArguments,
									experiment.getGenomeLength());
					System.out.println("Genome length: "
							+ experiment.getGenomeLength());
					Master master = (new MasterFactory(simulator)).getMaster(
							masterArguments, experimentArguments,
							environmentArguments, robotsArguments,
							controllersArguments, population,
							evaluationArguments, diskStorage);
					master.execute();
					diskStorage.close();
					System.exit(0);
				} else {
					createExperimentAndEvaluationFunction();
					simRandom.setSeed(randomSeed);
					Population population = new PopulationFactory(simulator)
							.getPopulation(populationArguments,
									experiment.getGenomeLength());
					runEvolution(population);
				}

			}
		}
		if (gui != null)
			gui.dispose();
	}

	protected void runEvolution(Population population) throws Exception {
		gui = (new GuiFactory(simulator)).getGui(guiArguments, experiment);
		diskStorage.start();
		diskStorage.saveCommandlineArguments(commandlineArguments);

		int startGeneration = population.getNumberOfCurrentGeneration();
		long startTime = System.currentTimeMillis();
		// int tempSeed = simRandom.nextInt();
		while (!population.evolutionDone()) {
			Chromosome chromosome;

			System.out.println(population.getNumberOfCurrentGeneration() + "/"
					+ population.getNumberOfGenerations() + " - samples: "
					+ population.getNumberOfSamplesPerChromosome()
					+ ", steps: " + population.getNumberOfStepsPerSample());
			while ((chromosome = population.getNextChromosomeToEvaluate()) != null) {
				double fitness = 0;
				long tempSeed = population.getGenerationRandomSeed();

				for (int i = 0; i < population
						.getNumberOfSamplesPerChromosome(); i++) {
					// Make sure that random seed is controlled and the same for
					// all individuals at the beginning of each trial:
					// System.out.print("  S:"+tempSeed);
					simRandom.setSeed(tempSeed);
					tempSeed = simulator.getRandom().nextLong();

					experimentArguments.setArgument("fitnesssample", i);
					environmentArguments.setArgument("fitnesssample", i);
					environmentArguments.setArgument("totalsamples",
							population.getNumberOfSamplesPerChromosome());
					createExperimentAndEvaluationFunction();
					// Simulator.getInstance().setEnvironment(experiment.getEnvironment());
					experiment.setChromosome(chromosome);
					simulator.setEnvironment(experiment.getEnvironment());
					gui.run(simulator, renderer, experiment,
							evaluationFunction,
							population.getNumberOfStepsPerSample());
					fitness += evaluationFunction.getFitness();

				}
				// System.out.println("");

				fitness /= population.getNumberOfSamplesPerChromosome();
				population.setEvaluationResult(chromosome, fitness);

				// System.err.println("Memory heap total: "+Runtime.getRuntime().totalMemory()/1024+
				// "k  max: "+Runtime.getRuntime().maxMemory()/1024+
				// "k  free: "+Runtime.getRuntime().freeMemory()/1024+"k");

				System.out.print(".");

			}

			diskStorage.savePopulation(population, simRandom);
			double temp = (System.currentTimeMillis() - startTime) / 1000.0;
			temp /= (population.getNumberOfCurrentGeneration()
					- startGeneration + 1);
			temp *= (population.getNumberOfGenerations() - population
					.getNumberOfCurrentGeneration());

			System.out
					.printf("\nGeneration %3d/%3d, \tbest: %12.6f, \taverage: %12.6f, \tworst: %12.6f -- ETA: %s\n",
							population.getNumberOfCurrentGeneration(),
							population.getNumberOfGenerations(),
							population.getHighestFitness(),
							population.getAverageFitness(),
							population.getLowestFitness(),
							Util.formatIntoHHMMSS((long) temp));

			System.out.println("Seed: " + population.getGenerationRandomSeed());

			simRandom.setSeed(population.getGenerationRandomSeed());
			// tempSeed = simRandom.nextInt();
			population.createNextGeneration();
			// System.exit(0);
		}
	}

	public static void main(String[] args) throws IOException, Exception {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		try {
			new Main(args).execute();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			throw e;
		}
		System.exit(0);
	}

}