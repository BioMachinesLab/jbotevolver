//package evolutionaryrobotics.parallel;
//
//import result.Result;
//import simulation.Simulator;
//import simulation.util.Arguments;
//import simulation.util.SimRandom;
//import tasks.Task;
//import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
//import evolutionaryrobotics.neuralnetworks.Chromosome;
//import experiments.Experiment;
//import factories.EvaluationFunctionFactory;
//import factories.ExperimentFactory;
//import gui.BatchGui;
//import gui.Gui;
//
//public class SlaveTask extends Task {
//
//	private Arguments experimentArguments;
//	private Arguments environmentArguments;
//	private Arguments robotArguments;
//	private Arguments controllerArguments;
//	private Arguments evaluationArguments;
//
//	private Long randomSeed;
//
//	private Chromosome chromosome;
//	private Integer samplesPerIndividual;
//	private Integer stepsPerRun;
//	private Gui gui;
//	private double fitness;
//	private boolean cancel = false;
//	private Simulator simulator;
//
//	public SlaveTask(int id, Arguments experimentArguments,
//			Arguments environmentArguments, Arguments robotArguments,
//			Arguments controllerArguments, Arguments evaluationArguments,
//			Long randomSeed, Chromosome chromosome,
//			Integer samplesPerIndividual, Integer stepsPerRun) {
//		super(id);
//		this.experimentArguments = experimentArguments;
//		this.environmentArguments = environmentArguments;
//		this.robotArguments = robotArguments;
//		this.controllerArguments = controllerArguments;
//		this.evaluationArguments = evaluationArguments;
//		this.randomSeed = randomSeed;
//		this.chromosome = chromosome;
//		this.samplesPerIndividual = samplesPerIndividual;
//		this.stepsPerRun = stepsPerRun;
//	}
//
//	@Override
//	public void run() {
//		gui = new BatchGui();
//		simulator = new Simulator(new SimRandom());
//		simulator.setFileProvider(getFileProvider());
//		this.gui = new BatchGui();
//
//		// out.print( "Evaluating chromosome: "+chromosome.getID()
//		// +" (samples: " + samplesPerIndividual.intValue() + " of each " +
//		// stepsPerRun.intValue() + " steps per run) ...");
//		// out.flush();
//		long startTime = System.currentTimeMillis();
//
//		fitness = 0;
//		long tempSeed = randomSeed;
//		Experiment experiment;
//		// try {
//		for (int i = 0; i < samplesPerIndividual.intValue(); i++) {
//			// Make sure that random seed is controlled and the same for all
//			// individuals at the beginning of each trial:
//			experimentArguments.setArgument("fitnesssample", i);
//			environmentArguments.setArgument("fitnesssample", i);
//			environmentArguments.setArgument("totalsamples",
//					samplesPerIndividual.intValue());
//			// System.out.print("  S:"+tempSeed);
//			simulator.getRandom().setSeed(tempSeed);
//			tempSeed = simulator.getRandom().nextLong();
//			experiment = (new ExperimentFactory(simulator)).getExperiment(
//					experimentArguments, environmentArguments, robotArguments,
//					controllerArguments);
//			experiment.setChromosome(chromosome);
//			simulator.setEnvironment(experiment.getEnvironment());
//			EvaluationFunction evaluationFunction = (new EvaluationFunctionFactory(
//					simulator)).getEvaluationFunction(evaluationArguments,
//					experiment);
//			gui.run(simulator, null, experiment, evaluationFunction,
//					stepsPerRun.intValue());
//			fitness += evaluationFunction.getFitness();
//			// System.out.println("FITNESS SAMPLE "+i+": "+evaluationFunction.getFitness());
//		}
//
//		// System.out.println("");
//
//		double time = System.currentTimeMillis() - startTime;
//		time /= 1000;
//		fitness /= samplesPerIndividual.doubleValue();
//
//		// out.printf("fitness obtained: %f -- time: %5.2f\n", fitness,
//		// time);
//		// } catch (Exception e) {
//		// e.printStackTrace();
//		// }
//
//	}
//
//	@Override
//	public Result getResult() {
//		return new SlaveResult(chromosome.getID(), fitness);
//	}
//
//}
