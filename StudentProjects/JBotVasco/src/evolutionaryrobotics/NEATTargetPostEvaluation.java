package evolutionaryrobotics;

import java.io.File;
import java.io.FileWriter;

import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.results.NEATPostEvaluationResult;
import taskexecutor.tasks.NEATMultipleSampleTargetPostEvaluationTask;

public class NEATTargetPostEvaluation extends NEATPostEvaluation {
	public NEATTargetPostEvaluation(String[] args, String[] extraArgs) {
		super(args, extraArgs);
	}

	public NEATTargetPostEvaluation(String[] args) {
		super(args);
	}

	@Override
	public double[][][] runPostEval() {
		double[][][] result = null;

		try {
			String file = "";
			int generationNumber = 0;

			if (singleEvaluation) {
				file = dir + "/_showbest_current.conf";
				generationNumber = getGenerationNumberFromFile(dir + "/_generationnumber");
			} else {
				file = dir + startTrial + "/_showbest_current.conf";
				generationNumber = getGenerationNumberFromFile(dir + startTrial + "/_generationnumber");
			}

			result = new double[maxTrial][generationNumber][fitnesssamples];

			String[] newArgs = args != null ? new String[args.length + 1] : new String[1];

			newArgs[0] = file;

			for (int i = 1; i < newArgs.length; i++)
				newArgs[i] = args[i - 1];

			JBotEvolver jBotEvolver = new JBotEvolver(newArgs);

			if (jBotEvolver.getArguments().get("--executor") != null) {
				if (localEvaluation) {
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver,
							new Arguments("classname=ParallelTaskExecutor", true));
				} else {
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver,
							jBotEvolver.getArguments().get("--executor"));
				}
				taskExecutor.start();
			}

			boolean setNumberOfTasks = false;
			int totalTasks = 0;

			StringBuffer data = new StringBuffer();
			FileWriter fw = null;

			if (saveOutput)
				fw = new FileWriter(new File(dir + "/post_details.txt"));

			for (int i = startTrial; i <= maxTrial; i++) {
				if (singleEvaluation)
					file = dir + "/show_best/";
				else
					file = dir + i + "/show_best/";

				File directory = new File(file);

				int numberOfGenerations = directory.listFiles().length;

				if (!setNumberOfTasks) {
					totalTasks = (maxTrial - startTrial + 1) * fitnesssamples * samples * numberOfGenerations
							/ sampleIncrement;
					taskExecutor.setTotalNumberOfTasks(totalTasks);
					setNumberOfTasks = true;
				}

				File[] files = directory.listFiles();
				sortByNumber(files);

				for (File f : files) {
					int generation = Integer.valueOf(f.getName().substring(8, f.getName().indexOf(".")));

					newArgs[0] = file + f.getName();
					jBotEvolver = new JBotEvolver(newArgs);

					for (int sample = 0; sample < samples; sample += sampleIncrement) {
						JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),
								jBotEvolver.getRandomSeed());
						Population pop = newJBot.getPopulation();
						Chromosome chr = pop.getBestChromosome();

						NEATMultipleSampleTargetPostEvaluationTask t = new NEATMultipleSampleTargetPostEvaluationTask(i,
								generation, newJBot, chr, sample, sample + sampleIncrement, targetfitness);
						taskExecutor.addTask(t);

						if (showOutput) {
							System.out.println(".");
						}
					}

					if (showOutput)
						System.out.println();

					taskExecutor.setDescription(dir + i + "/" + (generation + 1) + " out of " + numberOfGenerations
							+ " (total tasks: " + totalTasks + ")");

					if (showOutput)
						System.out.println();
				}

				for (int gen = 0; gen < numberOfGenerations; gen++) {
					for (int sample = 0; sample < samples; sample += sampleIncrement) {

						NEATPostEvaluationResult sfr = (NEATPostEvaluationResult) taskExecutor.getResult();
						result[sfr.getRun() - 1][sfr.getGeneration()][sfr.getFitnesssample()] += sfr.getFitness()
								* sampleIncrement / samples;
						String line = sfr.getRun() + " " + sfr.getGeneration() + " " + sfr.getFitnesssample() + " "
								+ sfr.getSample() + " " + sfr.getFitness() + "\n";
						data.append(line);

						if (showOutput) {
							System.out.print(line);
						}
					}
				}
			}

			if (showOutput)
				System.out.println();

			if (saveOutput) {
				fw.append(data);
				fw.close();
			}

			taskExecutor.stopTasks();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
}
