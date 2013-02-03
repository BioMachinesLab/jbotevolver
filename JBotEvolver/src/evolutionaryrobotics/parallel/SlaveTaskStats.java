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
//public class SlaveTaskStats extends Task {
//
//	private Arguments experimentArguments;
//	private Arguments environmentArguments;
//	private Arguments robotArguments;
//	private Arguments controllerArguments;
//	private Arguments evaluationArguments;
//	private Chromosome chromosome;
//	private Integer totalSamples;
//	private Integer stepsPerRun;
//	private Gui gui;
//	private double fitness;
//	private Double fitnessThreshold;
//	private Simulator simulator;
//	
//	private Integer success = 0;
//	private Integer fitnesssample = 0;
//
//	public SlaveTaskStats(int id, Arguments experimentArguments,
//			Arguments environmentArguments, Arguments robotArguments,
//			Arguments controllerArguments, Arguments evaluationArguments, Chromosome chromosome,
//			Integer totalSamples, Integer stepsPerRun, Double fitnessThreshold, Integer fitnesssample) {
//		super(id);
//		this.experimentArguments = experimentArguments;
//		this.environmentArguments = environmentArguments;
//		this.robotArguments = robotArguments;
//		this.controllerArguments = controllerArguments;
//		this.evaluationArguments = evaluationArguments;
//		this.chromosome = chromosome;
//		this.totalSamples = totalSamples;
//		this.stepsPerRun = stepsPerRun;
//		this.fitnessThreshold = fitnessThreshold;
//		this.fitnesssample = fitnesssample;
//	}
//
//	@Override
//	public void run() {
//		gui = new BatchGui();
//		simulator = new Simulator(new SimRandom());
//		simulator.setFileProvider(getFileProvider());
//		this.gui = new BatchGui();
//		
//		double avg = 0;
//
//		fitness = 0;
//		Experiment experiment;
//		// try {
//		for (int i = 0; i < totalSamples.intValue(); i++) {
//			// Make sure that random seed is controlled and the same for all
//			// individuals at the beginning of each trial:
//			experimentArguments.setArgument("fitnesssample", fitnesssample);
//			environmentArguments.setArgument("fitnesssample", fitnesssample);
//			environmentArguments.setArgument("totalsamples",
//					totalSamples.intValue());
//
//			simulator.getRandom().setSeed(i);
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
//			fitness = evaluationFunction.getFitness();
//			if(fitnessThreshold > 0) {
//				if(fitness >= fitnessThreshold)
//					success++;
//			}else
//				avg+=fitness;
//		}
//		
//		if(fitnessThreshold == 0)
//			success = (int)(avg/totalSamples.doubleValue());
//			
//	}
//
//	@Override
//	public Result getResult() {
//		return new SlaveResult(this.id, success);
//	}
//
//}
