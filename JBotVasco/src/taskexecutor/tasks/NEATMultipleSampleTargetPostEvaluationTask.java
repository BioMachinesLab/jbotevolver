package taskexecutor.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import commoninterface.entities.target.Formation.FormationType;
import environment.target.FormationMultiTargetEnvironment;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.results.NEATPostEvaluationResult;

public class NEATMultipleSampleTargetPostEvaluationTask extends JBotEvolverTask {
	private static final long serialVersionUID = -2599801600071882203L;
	private int startSample;
	private int endSample;
	private double fitness = 0;
	private Chromosome chromosome;
	private double threshold = 0;
	private int run;
	private int generation;

	private int totalRuns = 0;
	private ArrayList<TaskRun> runs = new ArrayList<TaskRun>();

	public NEATMultipleSampleTargetPostEvaluationTask(int run, int generation, JBotEvolver jBotEvolver,
			Chromosome chromosome, int startSample, int endSample, double threshold) {
		super(jBotEvolver);
		this.chromosome = chromosome;
		this.startSample = startSample;
		this.endSample = endSample;
		this.threshold = threshold;
		this.run = run;
		this.generation = generation;

		HashMap<String, Arguments> args = jBotEvolver.getArgumentsCopy();
		// Check if it is supposed to vary the robots number, if there is the
		// range of quantities to use or if there is a defined robot quantity
		int[] robotsQuantity = null;
		if (args.get("--robots").getArgumentIsDefined("randomizenumber")) {
			String[] rawArray = args.get("--robots").getArgumentAsString("randomizenumber").split(",");
			if (rawArray.length >= 1) {
				robotsQuantity = new int[rawArray.length];

				for (int i = 0; i < rawArray.length; i++) {
					robotsQuantity[i] = Integer.parseInt(rawArray[i]);
				}
			} else {
				robotsQuantity = new int[1];
				robotsQuantity[0] = args.get("--robots").getArgumentAsInt("numberofrobots");
			}
		} else {
			robotsQuantity = new int[1];
			robotsQuantity[0] = args.get("--robots").getArgumentAsInt("numberofrobots");
		}

		// Check if the environment is the correct and if there is a specific
		// shape selected (we only run this if the selected shape is mix)
		ArrayList<FormationType> formations = new ArrayList<FormationType>();
		Arguments envArgs = args.get("--environment");
		if (envArgs.getArgumentAsString("classname").equals(FormationMultiTargetEnvironment.class.getName())
		/*
		 * &&
		 * envArgs.getArgumentAsString("formationShape").equals(FormationType.
		 * mix.toString())
		 */) {

			// Run for each formation, excluding the mix one
			for (FormationType formation : FormationType.values()) {
				if (formation != FormationType.mix) {
					formations.add(formation);
				}
			}
		} else {
			throw new IllegalArgumentException(
					"Invalid environment to use this task type (Actual: " + envArgs.getArgumentAsString("classname")
							+ " Expected: " + FormationMultiTargetEnvironment.class.getName() + ")");
		}

		for (int quantity : robotsQuantity) {
			for (FormationType formation : formations) {
				runs.add(new TaskRun(quantity, formation.toString()));
			}
		}
	}

	@Override
	public void run() {
		for (int i = startSample; i < endSample; i++) {
			HashMap<String, Arguments> args = jBotEvolver.getArguments();
			Arguments envArgs = jBotEvolver.getArguments().get("--environment");
			envArgs.setArgument("moveTarget", 0);
			envArgs.setArgument("rotateFormation", 1);
			for (TaskRun run : runs) {
				double localFitness = 0;
				boolean halfhalfFaults = args.get("--evolution").getArgumentAsIntOrSetDefault("halfhalfFaults", 1) == 1;
				if (envArgs.getArgumentAsIntOrSetDefault("injectFaults", 0) == 1) {
					if (halfhalfFaults) {
						localFitness += runTargetGenerationalTask(FormationType.valueOf(run.getFormation()),
								run.getRobotsQnt(), i);
					}

					envArgs.setArgument("injectFaults", 1);
					envArgs.setArgument("onePerOneRobotTarget", 0);
					envArgs.setArgument("targetsQuantity", run.getRobotsQnt() - 1);

					localFitness += runTargetGenerationalTask(FormationType.valueOf(run.getFormation()),
							run.getRobotsQnt(), i);
				} else {
					localFitness = runTargetGenerationalTask(FormationType.valueOf(run.getFormation()),
							run.getRobotsQnt(), i);
				}

				if (threshold > 0) {
					fitness += localFitness >= threshold ? 1 : 0;
				} else {
					fitness += localFitness;
				}
			}
		}
		fitness /= totalRuns;

	}

	private double runTargetGenerationalTask(FormationType formation, int robotsQuantity, long seed) {
		double localFitness = 0;
		HashMap<String, Arguments> args = jBotEvolver.getArguments();
		Arguments envArgs = args.get("--environment");
		Arguments robotArgs = args.get("--robots");
		envArgs.setArgument("moveTarget", 1);
		envArgs.setArgument("rotateFormation", 0);
		envArgs.setArgument("formationShape", formation.toString());
		robotArgs.removeArgument("randomizenumber");
		robotArgs.setArgument("variablenumber", 0);
		robotArgs.setArgument("numberofrobots", robotsQuantity);

		Simulator simulator = jBotEvolver.createSimulator(seed);
		simulator.setFileProvider(getFileProvider());

		ArrayList<Robot> robots = jBotEvolver.createRobots(simulator, chromosome);
		simulator.addRobots(robots);

		EvaluationFunction eval = EvaluationFunction.getEvaluationFunction(args.get("--evaluation"));
		simulator.addCallback(eval);
		simulator.simulate();

		localFitness += eval.getFitness();
		totalRuns++;

		envArgs.setArgument("moveTarget", 0);
		envArgs.setArgument("rotateFormation", 1);
		envArgs.setArgument("formationShape", formation.toString());
		robotArgs.removeArgument("randomizenumber");
		robotArgs.setArgument("variablenumber", 0);
		robotArgs.setArgument("numberofrobots", robotsQuantity);

		simulator = jBotEvolver.createSimulator(seed);
		simulator.setFileProvider(getFileProvider());

		robots = jBotEvolver.createRobots(simulator, chromosome);
		simulator.addRobots(robots);

		eval = EvaluationFunction.getEvaluationFunction(args.get("--evaluation"));
		simulator.addCallback(eval);
		simulator.simulate();

		localFitness += eval.getFitness();
		totalRuns++;
		
		envArgs.setArgument("moveTarget", 1);
		envArgs.setArgument("rotateFormation", 1);
		envArgs.setArgument("formationShape", formation.toString());
		robotArgs.removeArgument("randomizenumber");
		robotArgs.setArgument("variablenumber", 0);
		robotArgs.setArgument("numberofrobots", robotsQuantity);

		simulator = jBotEvolver.createSimulator(seed);
		simulator.setFileProvider(getFileProvider());

		robots = jBotEvolver.createRobots(simulator, chromosome);
		simulator.addRobots(robots);

		eval = EvaluationFunction.getEvaluationFunction(args.get("--evaluation"));
		simulator.addCallback(eval);
		simulator.simulate();

		localFitness += eval.getFitness();
		totalRuns++;

		return localFitness;
	}

	protected class TaskRun implements Serializable {
		private static final long serialVersionUID = -6030246656305024477L;
		int robotsQnt;
		String formation;

		public TaskRun(int robotsQnt, String formation) {
			this.robotsQnt = robotsQnt;
			this.formation = formation;
		}

		public int getRobotsQnt() {
			return robotsQnt;
		}

		public String getFormation() {
			return formation;
		}

	}

	@Override
	public Result getResult() {
		NEATPostEvaluationResult fr = new NEATPostEvaluationResult(run, generation, 0, fitness, startSample);
		return fr;
	}

	public int getTotalRuns() {
		return totalRuns;
	}

	public int getToDoTasks() {
		return runs.size() - totalRuns;
	}
}
