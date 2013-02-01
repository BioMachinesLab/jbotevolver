package evolutionaryrobotics.parallel;

import result.Result;
import simulation.Simulator;
import simulation.util.Arguments;
import simulation.util.SimRandom;
import tasks.Task;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import experiments.CoevolutionExperiment;
import experiments.Experiment;
import factories.EvaluationFunctionFactory;
import factories.ExperimentFactory;
import gui.BatchGui;
import gui.Gui;

public class CoevolutionSlaveTask extends Task {

	private Arguments experimentArguments;
	private Arguments environmentArguments;
	private Arguments robotArguments;
	private Arguments controllerArguments;
	private Arguments evaluationArguments;

	private Long randomSeed;

	private Chromosome[] listOfAdversaries;
	private Integer samplesPerIndividual;
	private Integer stepsPerRun;
	private Gui gui = new BatchGui();
	private double fitness;
	private int chromosomeId;
	private Chromosome chromosome;

	public CoevolutionSlaveTask(int id, Arguments experimentArguments,
			Arguments environmentArguments, Arguments robotArguments,
			Arguments controllerArguments, Arguments evaluationArguments,
			Long randomSeed, int chromosomeId, Chromosome chromosome,
			Chromosome[] listOfAdversaries,
			Integer samplesPerIndividual, Integer stepsPerRun) {
		super(id);
		this.experimentArguments = experimentArguments;
		this.environmentArguments = environmentArguments;
		this.robotArguments = robotArguments;
		this.controllerArguments = controllerArguments;
		this.evaluationArguments = evaluationArguments;
		this.randomSeed = randomSeed;
		this.chromosomeId = chromosomeId;
		this.chromosome = chromosome;
		this.listOfAdversaries = listOfAdversaries;
		this.samplesPerIndividual = samplesPerIndividual;
		this.stepsPerRun = stepsPerRun;
		this.gui = new BatchGui();
		;
	}

	public void run() {
		Simulator simulator = new Simulator(new SimRandom());
		// out.print( "Evaluating chromosome: "+chromosome.getID()
		// +" (samples: " + samplesPerIndividual.intValue() + " of each " +
		// stepsPerRun.intValue() + " steps per run) ...");
		// out.flush();
		long startTime = System.currentTimeMillis();

		fitness = 0;
		long tempSeed = randomSeed;
		CoevolutionExperiment experiment;
		try {
			for(Chromosome adversary: listOfAdversaries){
				for (int i = 0; i < samplesPerIndividual.intValue(); i++) {
					// Make sure that random seed is controlled and the same for all
					// individuals at the beginning of each trial:
					experimentArguments.setArgument("fitnesssample", i);
					environmentArguments.setArgument("fitnesssample", i);
					environmentArguments.setArgument("totalsamples",
							samplesPerIndividual.intValue());
					// System.out.print("  S:"+tempSeed);
					simulator.getRandom().setSeed(tempSeed);
					tempSeed = simulator.getRandom().nextLong();
					experiment = (new ExperimentFactory(simulator)).getCoevolutionExperiment(
							experimentArguments, environmentArguments,
							robotArguments, controllerArguments);
					experiment.setChromosome(chromosome, adversary);
					simulator.setEnvironment(experiment.getEnvironment());
					EvaluationFunction evaluationFunction = (new EvaluationFunctionFactory(
							simulator)).getEvaluationFunction(evaluationArguments,
									experiment);
					gui.run(simulator, null, experiment, evaluationFunction,
							stepsPerRun.intValue());
					fitness += evaluationFunction.getFitness();
					// System.out.println("FITNESS SAMPLE "+i+": "+evaluationFunction.getFitness());
				}
			}

			// System.out.println("");

			double time = System.currentTimeMillis() - startTime;
			time /= 1000;
			fitness /= (samplesPerIndividual.doubleValue()*listOfAdversaries.length);

			// out.printf("fitness obtained: %f -- time: %5.2f\n", fitness,
			// time);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public Result getResult() {
		return new SlaveResult(chromosomeId, fitness);
	}

}
