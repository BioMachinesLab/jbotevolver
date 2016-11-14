package taskexecutor.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import commoninterface.entities.target.Formation.FormationType;
import environment.target.FormationMultiTargetEnvironment;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import result.Result;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.results.SimpleFitnessResult;

public class TargetGenerationalTask extends JBotEvolverTask {
	private static final long serialVersionUID = -5013562053140293370L;
	private int samples;
	private double fitness = 0;
	private Chromosome chromosome;
	private Random random;
	private int totalRuns = 0;

	public TargetGenerationalTask(JBotEvolver jBotEvolver, int samples, Chromosome chromosome, long seed) {
		super(jBotEvolver);
		this.samples = samples;
		this.chromosome = chromosome;
		this.random = new Random(seed);
	}

	public TargetGenerationalTask(JBotEvolver jBotEvolver, int samples, Chromosome chromosome) {
		this(jBotEvolver, samples, chromosome, jBotEvolver.getRandomSeed());
	}

	@Override
	public void run() {
		HashMap<String, Arguments> args = jBotEvolver.getArguments();
		int[] robotsQuantity;

		// Check if it is supposed to vary the robots number, if
		// there is the range of quantities to use or if there is a
		// defined robot quantity
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
		Arguments envArgs = args.get("--environment");
		if (envArgs.getArgumentAsString("classname").equals(FormationMultiTargetEnvironment.class.getName())) {
			launchTasks(args, robotsQuantity);
		} else {
			System.err.printf("[%s] Invalid environment to use this task type.%n", this.getClass().getName());
			System.exit(0);
		}
	}

	private void launchTasks(HashMap<String, Arguments> args, int[] robotsQuantity) {
		boolean halfhalfFaults = args.get("--evolution").getArgumentAsIntOrSetDefault("halfhalfFaults", 1) == 1;
		Arguments envArgs = args.get("--environment");
		if (envArgs.getArgumentAsString("formationShape").equals(FormationType.mix.toString())) {
			// Run for each formation, excluding the mix one
			for (FormationType formation : FormationType.values()) {
				if (formation != FormationType.mix) {
					if (envArgs.getArgumentAsIntOrSetDefault("injectFaults", 0) == 1) {
						if (halfhalfFaults) {
							for (Integer qnt : robotsQuantity) {
								envArgs.setArgument("injectFaults", 0);
								runTargetGenerationalTask(formation, qnt);
							}
						}

						for (Integer qnt : robotsQuantity) {
							envArgs.setArgument("injectFaults", 1);
							envArgs.setArgument("onePerOneRobotTarget", 0);
							envArgs.setArgument("targetsQuantity", qnt - 1);

							runTargetGenerationalTask(formation, qnt);
						}
					} else {
						for (Integer qnt : robotsQuantity) {
							runTargetGenerationalTask(formation, qnt);
						}
					}
				}
			}
		} else {
			FormationType formation = FormationType.valueOf(envArgs.getArgumentAsString("formationShape"));
			if (envArgs.getArgumentAsIntOrSetDefault("injectFaults", 0) == 1) {
				if (halfhalfFaults) {
					for (Integer qnt : robotsQuantity) {
						envArgs.setArgument("injectFaults", 0);
						runTargetGenerationalTask(formation, qnt);
					}
				}

				for (Integer qnt : robotsQuantity) {
					envArgs.setArgument("injectFaults", 1);
					envArgs.setArgument("onePerOneRobotTarget", 0);
					envArgs.setArgument("targetsQuantity", qnt - 1);

					runTargetGenerationalTask(formation, qnt);
				}
			} else {
				for (Integer qnt : robotsQuantity) {
					runTargetGenerationalTask(formation, qnt);
				}
			}
		}
	}

	private void runTargetGenerationalTask(FormationType formation, int robotsQuantity) {
		for (int i = 0; i < samples; i++) {
			HashMap<String, Arguments> args = jBotEvolver.getArguments();
			Arguments envArgs = args.get("--environment");
			Arguments robotArgs = args.get("--robots");
			envArgs.setArgument("moveTarget", 1);
			envArgs.setArgument("rotateFormation", 0);
			envArgs.setArgument("fitnesssample", i);
			envArgs.setArgument("formationShape", formation.toString());
			robotArgs.removeArgument("randomizenumber");
			robotArgs.setArgument("variablenumber", 0);
			robotArgs.setArgument("numberofrobots", robotsQuantity);

			// systemOutArgs(args, formation, robotsQuantity);

			Simulator simulator = jBotEvolver.createSimulator(random.nextLong());
			simulator.setFileProvider(getFileProvider());

			ArrayList<Robot> robots = jBotEvolver.createRobots(simulator, chromosome);
			simulator.addRobots(robots);

			EvaluationFunction eval = EvaluationFunction.getEvaluationFunction(args.get("--evaluation"));
			simulator.addCallback(eval);
			simulator.simulate();

			fitness += eval.getFitness();
			totalRuns++;

			envArgs.setArgument("moveTarget", 0);
			envArgs.setArgument("rotateFormation", 1);
			envArgs.setArgument("fitnesssample", i);
			envArgs.setArgument("formationShape", formation.toString());
			robotArgs.removeArgument("randomizenumber");
			robotArgs.setArgument("variablenumber", 0);
			robotArgs.setArgument("numberofrobots", robotsQuantity);

			// systemOutArgs(args, formation, robotsQuantity);

			simulator = jBotEvolver.createSimulator(random.nextLong());
			simulator.setFileProvider(getFileProvider());

			robots = jBotEvolver.createRobots(simulator, chromosome);
			simulator.addRobots(robots);

			eval = EvaluationFunction.getEvaluationFunction(args.get("--evaluation"));
			simulator.addCallback(eval);
			simulator.simulate();

			fitness += eval.getFitness();
			totalRuns++;

			envArgs.setArgument("moveTarget", 1);
			envArgs.setArgument("rotateFormation", 1);
			envArgs.setArgument("fitnesssample", i);
			envArgs.setArgument("formationShape", formation.toString());
			robotArgs.removeArgument("randomizenumber");
			robotArgs.setArgument("variablenumber", 0);
			robotArgs.setArgument("numberofrobots", robotsQuantity);

			// systemOutArgs(args, formation, robotsQuantity);

			simulator = jBotEvolver.createSimulator(random.nextLong());
			simulator.setFileProvider(getFileProvider());

			robots = jBotEvolver.createRobots(simulator, chromosome);
			simulator.addRobots(robots);

			eval = EvaluationFunction.getEvaluationFunction(args.get("--evaluation"));
			simulator.addCallback(eval);
			simulator.simulate();

			fitness += eval.getFitness();
			totalRuns++;
		}
	}

	@Override
	public Result getResult() {
		SimpleFitnessResult fr = new SimpleFitnessResult(getId(), chromosome.getID(), fitness / totalRuns);
		return fr;
	}

	@SuppressWarnings("unused")
	private void systemOutArgs(HashMap<String, Arguments> args, FormationType formation, int robotsQuantity) {
		System.out.println("##################");
		System.out.println("Inject: " + args.get("--environment").getArgumentAsInt("injectFaults"));
		System.out.println("1 for 1: " + args.get("--environment").getArgumentAsInt("onePerOneRobotTarget"));
		System.out.println("Formation: " + formation);
		System.out.println("Robots Qnt: " + robotsQuantity);
		System.out.println("Targets Qnt: " + args.get("--environment").getArgumentAsInt("targetsQuantity"));
		System.out.println("Move: " + args.get("--environment").getArgumentAsInt("moveTarget"));
		System.out.println("Rotate: " + args.get("--environment").getArgumentAsInt("rotateFormation"));
	}
}