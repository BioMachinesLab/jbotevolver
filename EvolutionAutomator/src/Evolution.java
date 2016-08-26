package src;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.NEATPostEvaluation;
import evolutionaryrobotics.PostEvaluation;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;

public class Evolution extends Thread {

	protected Controller controller;
	protected HashMap<String, Arguments> args;
	protected String defaultArgs;
	protected Main main;
	protected String outputFolder;
	protected int nTries = 3;

	public Evolution(Main main, Controller controller, String arguments) {
		this.controller = controller;
		this.defaultArgs = arguments;
		this.main = main;
		controller.setEvolving(true);
	}

	@Override
	public void run() {

		controller.createArguments(defaultArgs);
		args = controller.getArguments();

		if (!controller.skipEvolution()) {
			try {
				outputFolder = main.getFolderName() + "/" + controller.getName();

				createConfigFile(outputFolder, controller.getName() + ".conf");
				runEvolutions();
				Arguments postArguments = controller.getArguments("--postevaluation");
				if (postArguments != null) {
					System.out.println("\nEvolution finished, running Post Eval on " + controller.getName());
					int run = runPostEvaluation();
					System.out.println("Post-evaluation on " + controller.getName() + ": " + run);
					String weights = getWeights(run);
					System.out.println(
							"Got weights (#" + weights.length() + ") for " + controller.getName() + ": " + run);
					controller.setWeights(weights);
					createConfigFile(outputFolder, "best.conf");
				} else {
					controller.setWeights("weights");
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (nTries > 0) {
					nTries--;
					this.run();
					return;
				}
			}
		}
		main.evolutionFinished(controller.getName());
	}

	private String getWeights(int run) {

		String execute = outputFolder + "/" + run + "/_showbest_current.conf";
		String weights = "";
		try {
			JBotEvolver jBotEvolver = new JBotEvolver(execute.split(" "));
			Chromosome chromosome = jBotEvolver.getPopulation().getBestChromosome();
			weights = chromosome.getAllelesString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (weights.isEmpty())
			throw new RuntimeException("Weights are empty!");

		return weights;
	}

	protected int runPostEvaluation() throws Exception {

		Thread.sleep(2000); // wait to make sure that all files have been
							// written to the disk

		File post = new File(outputFolder + "/post.txt");

		if (post.exists())
			return Integer.parseInt(new Scanner(post).nextLine().split(":")[1].trim());

		String runsVariable = main.getGlobalVariable("%runs");
		int nRuns = 1;
		if (runsVariable != null)
			nRuns = Integer.parseInt(runsVariable);

		Arguments postArguments = controller.getArguments("--postevaluation");
		String stringArguments = "dir=" + outputFolder;
		if (postArguments != null) {
			for (String arg : postArguments.getArguments())
				stringArguments += " " + arg + "=" + postArguments.getArgumentAsString(arg);
		}

		String evolutionType = controller.getArguments("--evolution").getArgumentAsString("classname");
		String[] split = evolutionType.split("\\.");

		if (split[split.length - 1].startsWith("NEAT")) {
			int fitnessSamples = postArguments.getArgumentAsIntOrSetDefault("fitnesssamples", 1);
			return runNeatPostEvaluation(nRuns, fitnessSamples, stringArguments);
		} else
			return runGenerationalPostEvaluation(nRuns, stringArguments);
	}

	protected int runGenerationalPostEvaluation(int nRuns, String stringArguments) throws Exception {
		PostEvaluation postEval = new PostEvaluation(stringArguments.split(" "));
		double[][] values = postEval.runPostEval();

		double[] results = new double[nRuns];
		double[] averages = new double[nRuns];
		double[] stdDeviations = new double[nRuns];

		int maxIndex = 0;

		for (int i = 0; i < results.length; i++) {

			double val = 0;

			for (int j = 0; j < values[i].length; j++)
				val += values[i][j];

			averages[i] = getAverage(values[i]);
			stdDeviations[i] = getStdDeviation(values[i], averages[i]);

			results[i] = val;

			if (results[i] > results[maxIndex])
				maxIndex = i;
		}

		int best = maxIndex + 1;

		String result = "#best: " + best + "\n";

		for (int i = 0; i < values.length; i++) {
			result += (i + 1) + " ";
			for (double j : values[i])
				result += j + " ";

			result += "(" + averages[i] + " +- " + stdDeviations[i] + ")\n";
		}

		double overallAverage = getAverage(averages);

		result += "Overall: " + overallAverage + " +- " + getStdDeviation(averages, overallAverage);

		System.out.println(result);

		createFile(outputFolder, "post.txt", result);

		return best;
	}

	protected int runNeatPostEvaluation(int nRuns, int fitnessSamples, String stringArguments) throws Exception {
		NEATPostEvaluation p = new NEATPostEvaluation(stringArguments.split(" "));
		double[][][] values = p.runPostEval();

		int[] bestGenerationIndex = new int[nRuns];
		double[] bestFitness = new double[nRuns];
		double[][] generationAverage = new double[nRuns][values[0].length];
		double[][] generationStdDeviations = new double[nRuns][values[0].length];

		int bestRunIndex = 0;

		for (int run = 0; run < values.length; run++) {
			double val = 0;

			for (int generation = 0; generation < values[run].length; generation++) {

				val = 0;

				for (int fitnesssample = 0; fitnesssample < values[run][generation].length; fitnesssample++) {
					val += values[run][generation][fitnesssample];
				}

				generationAverage[run][generation] = getAverage(values[run][generation]);
				generationStdDeviations[run][generation] = getStdDeviation(values[run][generation],
						generationAverage[run][generation]);

				val /= values[run][generation].length;

				if (val > bestFitness[run]) {
					bestGenerationIndex[run] = generation;
					bestFitness[run] = val;
				}
			}

			if (bestFitness[run] > bestFitness[bestRunIndex]) {
				bestRunIndex = run;
			}

		}

		int best = bestRunIndex + 1;
		double overallAverage = 0;

		String result = "#best: " + best + "\n";
		for (int run = 0; run < nRuns; run++) {
			result += (run + 1) + " ";

			for (double fitnessSampleVal : values[run][bestGenerationIndex[run]])
				result += fitnessSampleVal + " ";

			overallAverage += generationAverage[run][bestGenerationIndex[run]] / nRuns;

			result += " (" + generationAverage[run][bestGenerationIndex[run]] + " +- "
					+ generationStdDeviations[run][bestGenerationIndex[run]] + ") " + bestGenerationIndex[run] + "\n";
		}

		result += "Overall: " + overallAverage + " +- " + getOverallStdDeviation(generationAverage, nRuns);

		System.out.println(result);

		createFile(outputFolder, "post.txt", result);

		return best;

	}

	private void runEvolutions() throws Exception {

		if (!new File(outputFolder + "/post.txt").exists()) {
			int nRuns = 1;
			String runsVariable = main.getGlobalVariable("%runs");
			if (runsVariable != null)
				nRuns = Integer.parseInt(runsVariable);

			Worker[] workers = new Worker[nRuns];

			for (int i = 0; i < nRuns; i++) {
				workers[i] = new Worker(i + 1);
				workers[i].start();
			}

			for (Worker w : workers)
				w.join();
		}
	}

	private void createConfigFile(String folderName, String configName) throws Exception {
		createFile(folderName, configName, Arguments.beautifyString(controller.getCompleteConfiguration()));
	}

	protected void createFile(String folderName, String fileName, String contents) throws Exception {
		File f = new File(folderName);

		if (!f.exists())
			f.mkdir();

		f = new File(folderName + "/" + fileName);

		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write(contents);
		bw.flush();
		bw.close();
	}

	protected double getAverage(double[] values) {

		double avg = 0;

		for (double i : values)
			avg += i;

		return avg / values.length;
	}

	protected double getStdDeviation(double[] values, double avg) {

		double stdDeviation = 0;

		for (double d : values)
			stdDeviation += Math.pow(d - avg, 2);

		return Math.sqrt(stdDeviation / values.length);
	}

	protected double getOverallAverage(double[][] values, int runs) {
		double[] avgs = new double[runs];

		for (int i = 0; i < avgs.length; i++)
			avgs[i] = getAverage(values[i]);

		return getAverage(avgs);
	}

	protected double getOverallStdDeviation(double[][] values, int runs) {
		double[] avgs = new double[runs];
		double[] stds = new double[runs];

		for (int i = 0; i < avgs.length; i++) {
			avgs[i] = getAverage(values[i]);
			stds[i] = getStdDeviation(values[i], avgs[i]);
		}

		double overallAverage = getAverage(avgs);

		return getStdDeviation(avgs, overallAverage);
	}

	class Worker extends Thread {

		private int run;

		public Worker(int run) {
			this.run = run;
		}

		@Override
		public void run() {
			try {
				main.incrementEvolutions();

				File f = new File(outputFolder + "/" + run + "/_restartevolution.conf");

				long seed = run;

				if (controller.getArguments("--random-seed") != null)
					seed += Long.parseLong(controller.getArguments("--random-seed").getCompleteArgumentString());

				int generations = controller.getArguments("--population").getArgumentAsInt("generations");

				String runEvolution = outputFolder + "/" + controller.getName() + ".conf";
				String resumeEvolution = outputFolder + "/" + run + "/_restartevolution.conf --population +generations="
						+ generations;
				
				
				JBotEvolver jBotEvolver;

				if(f.exists()) {
					//evolution was already started before, we need to resume it
					jBotEvolver = new JBotEvolver(resumeEvolution.split(" "));
				} else {
					
					String[] originalArgs = Arguments.readOptionsFromFile(runEvolution);
					for(int i = 0 ; i < originalArgs.length ; i++) {
						originalArgs[i] = originalArgs[i].replaceAll("\\%run", this.run+"");
					}
					
					String[] extraArgs = ("--output " + outputFolder + "/" + run + " --random-seed " + seed).split(" ");
					
					String[] fullArgs = new String[originalArgs.length+extraArgs.length];
					
					int index;
					for(index = 0; index < originalArgs.length ; index++)
						fullArgs[index] = originalArgs[index];
					
					for(int i = 0; i < extraArgs.length ; i++)
						fullArgs[index++] = extraArgs[i];

					jBotEvolver = new JBotEvolver(fullArgs);
				}
				
				TaskExecutor taskExecutor;

				if (controller.getArguments("--executor") != null) {
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, controller.getArguments("--executor"));
				} else {
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver,
							jBotEvolver.getArguments().get("--executor"));
				}
				taskExecutor.start();
				evolutionaryrobotics.evolution.Evolution a = evolutionaryrobotics.evolution.Evolution
						.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
				a.executeEvolution();
				taskExecutor.stopTasks();

				main.decrementEvolutions();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}