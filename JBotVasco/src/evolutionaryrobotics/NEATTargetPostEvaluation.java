package evolutionaryrobotics;

import java.io.File;
import java.io.FileWriter;

import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.results.MetricsResult;
import taskexecutor.results.NEATPostEvaluationResult;
import taskexecutor.tasks.MetricsGenerationalTask;
import taskexecutor.tasks.NEATMultipleSampleTargetPostEvaluationTask;

public class NEATTargetPostEvaluation extends NEATPostEvaluation {
	private int taskCount = 0;
	protected boolean showOutput = true;

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
			int generationNumber = 0;

			File folder = null;
			if (singleEvaluation) {
				folder = new File(dir);
			} else {
				folder = new File(dir, Integer.toString(startTrial));
			}

			generationNumber = getGenerationNumberFromFile(new File(folder, "_generationnumber"));
			result = new double[maxTrial][generationNumber][fitnesssamples];

			String[] newArgs = args != null ? new String[args.length + 1] : new String[1];
			newArgs[0] = new File(folder, "_showbest_current.conf").getAbsolutePath();

			for (int i = 1; i < newArgs.length; i++) {
				newArgs[i] = args[i - 1];
			}

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
			if (saveOutput) {
				fw = new FileWriter(new File(dir, "post_details.txt"));
			}

			for (int i = startTrial; i <= maxTrial; i++) {
				File directory;
				if (singleEvaluation) {
					directory = new File(dir, "show_best");
				} else {
					directory = new File(dir, i + File.separatorChar + "show_best");
				}

				File[] files = directory.listFiles();
				sortByNumber(files);
				int numberOfGenerations = files.length;

				if (!setNumberOfTasks) {
					totalTasks = (maxTrial - startTrial + 1) * fitnesssamples * samples * numberOfGenerations
							/ sampleIncrement;
					taskExecutor.setTotalNumberOfTasks(totalTasks);
					setNumberOfTasks = true;
				}

				for (File file : files) {
					int generation = Integer.valueOf(file.getName().substring(8, file.getName().indexOf(".")));

					newArgs[0] = directory.getAbsolutePath() + file.getName();
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

	public void runMetricsEval() {
		try {
			File folder = null;
			if (singleEvaluation) {
				folder = new File(dir);
			} else {
				folder = new File(dir, Integer.toString(startTrial));
			}

			// Load task executor from configuration file
			String[] newArgs = args != null ? new String[args.length + 1] : new String[1];
			newArgs[0] = new File(folder, "_showbest_current.conf").getAbsolutePath();
			for (int i = 1; i < newArgs.length; i++) {
				newArgs[i] = args[i - 1];
			}

			// Initialize task executor
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

			// Get task count
			taskCount = countTasksToPerform(new File(dir));
			if (taskCount > 0) {
				taskExecutor.setTotalNumberOfTasks(taskCount);

				for (int i = startTrial; i <= maxTrial; i++) {
					// Define the run root directory
					File runDirectory;
					if (singleEvaluation) {
						runDirectory = folder;
					} else {
						runDirectory = new File(dir, Integer.toString(i));
					}

					// Perform simulation and metrics collection on the runs in
					// Which it was not performed
					File metricsLogFile = new File(runDirectory, "_metrics.log");
					if (!metricsLogFile.exists()) {
						System.out.printf("[%s] Starting metrics collection on run #%d%n", getClass().getSimpleName(), i);

						// Get the show_best directory and sort the
						// Configuration files
						File showBestDirectory = new File(runDirectory, "show_best");
						File[] files = showBestDirectory.listFiles();
						sortByNumber(files);
						int numberOfGenerations = files.length;

						// Get the metrics for each of the generations
						for (File file : files) {
							int generation = Integer.valueOf(file.getName().substring(8, file.getName().indexOf(".")));

							newArgs[0] = file.getAbsolutePath();
							jBotEvolver = new JBotEvolver(newArgs);

							JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),
									jBotEvolver.getRandomSeed());
							Chromosome chromosome = newJBot.getPopulation().getBestChromosome();

							MetricsGenerationalTask t = new MetricsGenerationalTask(newJBot, i, 1, generation,
									chromosome);
							taskExecutor.addTask(t);

							if (showOutput) {
								System.out.print(".");
							}

							String name = dir.replace('/', '\\') + i + File.separatorChar + (generation + 1);
							taskExecutor.setDescription(
									name + " out of " + numberOfGenerations + " (total tasks: " + taskCount + ")");
						}

						if (showOutput) {
							System.out.println();
						}

						// Collect all metrics
						FormationTaskMetricsData[] data = new FormationTaskMetricsData[numberOfGenerations];
						for (int gen = 0; gen < numberOfGenerations; gen++) {
							MetricsResult combinedResult = (MetricsResult) taskExecutor.getResult();
							data[gen] = (FormationTaskMetricsData) combinedResult.getMetricsData();
						}

						System.out.printf("[%s] Run #%d - Collected results for %d generations%n",
								getClass().getSimpleName(), i, data.length);

						// Generate log lines
						if (saveOutput) {
							StringBuffer metricsData = new StringBuffer();
							metricsData.append(FormationTaskMetricsCodex.getEncodedMetricsFileHeader());
							metricsData.append('\n');

							for (int j = 0; j < data.length; j++) {
								metricsData.append(FormationTaskMetricsCodex.encodeMetricsData(data[j]));
								metricsData.append('\n');
							}

							FileWriter metricsLogFileWriter = new FileWriter(new File(runDirectory, "_metrics.log"));
							metricsLogFileWriter.append(metricsData);
							metricsLogFileWriter.close();
						}
					}
				}

				taskExecutor.stopTasks();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int countTasksToPerform(File folder) {
		int tasks = 0;
		for (int i = startTrial; i <= maxTrial; i++) {
			// Define the run root directory
			File runDirectory;
			if (singleEvaluation) {
				runDirectory = folder;
			} else {
				runDirectory = new File(folder, Integer.toString(i));
			}

			// Test if metrics measurement was already performed on this run
			File metricsLogFile = new File(runDirectory, "_metrics.log");
			if (!metricsLogFile.exists()) {
				tasks += getGenerationNumberFromFile(new File(runDirectory, "_generationnumber"));
			}
		}

		return tasks;
	}
}
