package evolutionaryrobotics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import evolutionaryrobotics.populations.Population;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.results.NEATPostEvaluationResult;
import taskexecutor.tasks.NEATMultipleSamplePostEvaluationTask;

public class NEATPostEvaluation {

	protected int startTrial = 0;
	protected int maxTrial = 0;
	protected int samples = 100;
	protected int sampleIncrement = 100;
	protected int fitnesssamples = 1;
	protected int steps = 0;
	protected double targetfitness = 0;
	protected String dir = "";

	protected boolean singleEvaluation = false;
	protected boolean localEvaluation = false;

	protected TaskExecutor taskExecutor;
	protected String[] args;

	protected boolean showOutput = false;
	protected boolean saveOutput = true;

	public NEATPostEvaluation(String[] args, String[] extraArgs) {
		this(args);
		this.args = extraArgs;
	}

	public NEATPostEvaluation(String[] args) {
		for (String s : args) {
			String[] a = s.split("=");
			if (a[0].equals("dir"))
				dir = a[1];
			if (a[0].equals("samples"))
				samples = Integer.parseInt(a[1]);
			if (a[0].equals("fitnesssamples"))
				fitnesssamples = Integer.parseInt(a[1]);
			if (a[0].equals("targetfitness"))
				targetfitness = Double.parseDouble(a[1]);
			if (a[0].equals("singleevaluation"))
				singleEvaluation = Integer.parseInt(a[1]) == 1;
			if (a[0].equals("localevaluation"))
				localEvaluation = Integer.parseInt(a[1]) == 1;
			if (a[0].equals("steps"))
				steps = Integer.parseInt(a[1]);
			if (a[0].equals("showoutput"))
				showOutput = (Integer.parseInt(a[1]) == 1);
			if (a[0].equals("sampleincrement"))
				sampleIncrement = Integer.parseInt(a[1]);
			if (a[0].equals("saveoutput"))
				saveOutput = Integer.parseInt(a[1]) == 1;
		}

		if (steps != 0) {
			this.args = new String[] { "--environment", "+steps=" + steps, "--evaluation", "+posteval=1" };
		} else {
			this.args = new String[] { "--evaluation", "+posteval=1" };
		}

		if (!dir.endsWith(File.separatorChar + ""))
			dir += File.separatorChar;

		File subFolder;
		int currentFolder = 0;
		do {
			currentFolder++;
			subFolder = new File(dir + currentFolder);
		} while (subFolder.exists());

		startTrial = 1;
		maxTrial = --currentFolder;

		if (singleEvaluation)
			maxTrial = 1;
	}

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

			generationNumber = getGenerationNumberFromFile(new File(folder,"/_generationnumber"));
			result = new double[maxTrial][generationNumber][fitnesssamples];

			String[] newArgs = args != null ? new String[args.length + 1] : new String[1];
			newArgs[0] = new File(folder,"_showbest_current.conf").getAbsolutePath();

			for (int i = 1; i < newArgs.length; i++)
				newArgs[i] = args[i - 1];

			JBotEvolver jBotEvolver = new JBotEvolver(newArgs);

			if (jBotEvolver.getArguments().get("--executor") != null) {
				if (localEvaluation)
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver,
							new Arguments("classname=ParallelTaskExecutor", true));
				else
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver,
							jBotEvolver.getArguments().get("--executor"));
				taskExecutor.start();
			}

			boolean setNumberOfTasks = false;
			int totalTasks = 0;

			StringBuffer data = new StringBuffer();
			FileWriter fw = null;

			if (saveOutput)
				fw = new FileWriter(new File(dir + "/post_details.txt"));

			for (int i = startTrial; i <= maxTrial; i++) {
				File directory;
				if (singleEvaluation) {
					directory = new File(dir, "show_best");
				} else {
					directory = new File(dir, i + File.separatorChar + "show_best");
				}

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

					newArgs[0] = directory.getAbsolutePath()+ f.getName();
					jBotEvolver = new JBotEvolver(newArgs);

					for (int fitnesssample = 0; fitnesssample < fitnesssamples; fitnesssample++) {
						for (int sample = 0; sample < samples; sample += sampleIncrement) {
							JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),
									jBotEvolver.getRandomSeed());
							Population pop = newJBot.getPopulation();
							evolutionaryrobotics.neuralnetworks.Chromosome chr = pop.getBestChromosome();
							NEATMultipleSamplePostEvaluationTask t = new NEATMultipleSamplePostEvaluationTask(i,
									generation, newJBot, fitnesssample, chr, sample, sample + sampleIncrement,
									targetfitness);
							taskExecutor.addTask(t);
							if (showOutput)
								System.out.print(".");
						}
						if (showOutput)
							System.out.println();
					}

					taskExecutor.setDescription(dir + i + "/" + (generation + 1) + " out of " + numberOfGenerations
							+ " (total tasks: " + totalTasks + ")");

					if (showOutput)
						System.out.println();
				}

				for (int gen = 0; gen < numberOfGenerations; gen++) {

					for (int fitnesssample = 0; fitnesssample < fitnesssamples; fitnesssample++) {

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
			}

			if (showOutput)
				System.out.println();

			if (saveOutput) {
				fw.append(data);
				fw.close();
			}

			/*
			 * for(int i = startTrial ; i <= maxTrial ; i++) {
			 * 
			 * if(singleEvaluation) file = dir+"/show_best/"; else file =
			 * dir+i+"/show_best/";
			 * 
			 * File directory = new File(file);
			 * 
			 * for (int j = 0; j < directory.listFiles().length; j++) { for(int
			 * fitnesssample = 0 ; fitnesssample < fitnesssamples ;
			 * fitnesssample++) { for(int sample = 0 ; sample < samples ;
			 * sample++) { NEATPostEvaluationResult sfr =
			 * (NEATPostEvaluationResult)taskExecutor.getResult();
			 * if(showOutput) System.out.println(sfr.getRun()+" "
			 * +sfr.getGeneration()+" "+sfr.getFitnesssample()+" "
			 * +sfr.getSample()+" "+sfr.getFitness());
			 * result[sfr.getRun()-1][sfr.getFitnesssample()][sfr.getGeneration(
			 * )]+= sfr.getFitness()/samples; } } }
			 * 
			 * }
			 */

			taskExecutor.stopTasks();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void sortByNumber(File[] files) {
		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				int n1 = extractNumber(o1.getName());
				int n2 = extractNumber(o2.getName());
				return n1 - n2;
			}

			private int extractNumber(String name) {
				int i = 0;
				name = name.replace("showbest", "");
				name = name.replace(".conf", "");
				try {
					i = Integer.parseInt(name);
				} catch (Exception e) {
					i = 0; // if filename does not match the format
							// then default to 0
				}
				return i;
			}
		});

	}

	protected int getGenerationNumberFromFile(File file) {
		Scanner s = null;
		try {
			s = new Scanner(file);

			return Integer.valueOf(s.next()) + 1;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (s != null)
				s.close();
		}
		return 0;
	}
}
