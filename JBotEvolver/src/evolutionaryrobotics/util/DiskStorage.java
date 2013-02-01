package evolutionaryrobotics.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import evolutionaryrobotics.populations.Population;

import simulation.util.SimRandom;

public class DiskStorage {
	String outputDirectory;
	Vector<String> argumentsForShowBestIndividual = new Vector<String>();
	Vector<String> argumentsForRestartEvolution = new Vector<String>();

	protected final String generationNumberFilename = "_generationnumber";
	protected final String populationFilename = "population";
	protected final String fitnessLogFilename = "_fitness.log";
	protected final String argumentsFilename = "_arguments.conf";
	protected final String showBestFilename = "showbest";
	protected final String restartFilename = "_restartevolution.conf";

	protected PrintStream fitnessLog;

	public DiskStorage(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void start() throws FileNotFoundException {
		if (outputDirectory != null) {
			File f = new File(outputDirectory);
			if (!f.exists())
				if (!f.mkdirs()) {
					throw new RuntimeException(
							"Cannot create output directory: "
									+ outputDirectory);
				}
			File f_best = new File(outputDirectory + "/show_best");
			if (!f_best.exists())
				if (!f_best.mkdirs()) {
					throw new RuntimeException(
							"Cannot create output directory: "
									+ outputDirectory + "/show_best");
				}
			File f_pop = new File(outputDirectory + "/populations");
			if (!f_pop.exists())
				if (!f_pop.mkdirs()) {
					throw new RuntimeException(
							"Cannot create output directory: "
									+ outputDirectory + "/populations");
				}

			fitnessLog = openForWriting(outputDirectory + "/"
					+ fitnessLogFilename, true);
			fitnessLog.println("# Evoluation started on "
					+ new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
							.format(new Date()));
			fitnessLog.println("# Generation\t   Best \t   Average \t   Worst");
		}

	}

	public void saveCommandlineArguments(String[] arguments)
			throws FileNotFoundException {
		if (outputDirectory != null) {
			PrintStream argumentsFile = openForWriting(outputDirectory + "/"
					+ argumentsFilename);
			for (int i = 0; i < arguments.length; i += 2) {
				argumentsFile.println(arguments[i] + " " + arguments[i + 1]);
				if (!arguments[i].equalsIgnoreCase("--population")) {
					argumentsForShowBestIndividual.add(arguments[i]);
					argumentsForShowBestIndividual.add(arguments[i + 1]);
					argumentsForRestartEvolution.add(arguments[i]);
					argumentsForRestartEvolution.add(arguments[i + 1]);
				}
			}
			argumentsFile.close();
		}
	}

	public void close() {
		if (outputDirectory != null) {
			fitnessLog.close();
		}
	}

	public void savePopulation(Population populationA, Population populationB,
			SimRandom simRandom) throws IOException {
		if (outputDirectory != null) {
			updateFitnessLog(populationA, populationB);

			// Save population:
			savePopulationToFile(populationA, "A");
			savePopulationToFile(populationB, "B");

			// Save the generation number
			saveGenerationNumber(populationA);

			int randomSeed = simRandom.nextInt();

			// Save the show best file
			saveShowBestFile(populationA, populationB, randomSeed);

			// Save the show best file
			saveShowCurrentBest(populationA, populationB, randomSeed, "A","B");
			saveShowCurrentBest(populationB, populationA, randomSeed, "B","A");

			// Save the restart file
			saveRestartFile(populationA, populationB, randomSeed);

		}
	}

	public void savePopulation(Population population, SimRandom simRandom)
			throws IOException {
		if (outputDirectory != null) {
			updateFitnessLog(population);

			// Save population:
			savePopulationToFile(population, "");

			// Save the generation number
			saveGenerationNumber(population);

			int randomSeed = simRandom.nextInt();

			// Save the show best file
			saveShowBestFile(population, randomSeed);

			// Save the show best file
			saveShowCurrentBest(population, randomSeed);

			// Save the restart file
			saveRestartFile(population, randomSeed);
		}
	}

	private void updateFitnessLog(Population populationA, Population populationB) {
		fitnessLog
				.printf("\t%3d\t\t%8.3f\t%8.3f\t%8.3f\t%8.3f\t%8.3f\t%8.3f\n",
						populationA.getNumberOfCurrentGeneration(),
						populationA.getHighestFitness(),
						populationA.getAverageFitness(),
						populationA.getLowestFitness(),
						populationB.getHighestFitness(),
						populationB.getAverageFitness(),
						populationB.getLowestFitness());
		fitnessLog.flush();
	}

	private void updateFitnessLog(Population population) {
		fitnessLog.printf("\t%3d\t\t%8.3f\t%8.3f\t%8.3f\n",
				population.getNumberOfCurrentGeneration(),
				population.getHighestFitness(), population.getAverageFitness(),
				population.getLowestFitness());
		fitnessLog.flush();
	}

	private void saveShowCurrentBest(Population populationA,
			Population populationB, int randomSeed, String prefixA,
			String prefixB) throws FileNotFoundException {
		PrintStream currentShowBestFile = openForWriting(outputDirectory + "/_"
				+ prefixA + showBestFilename + "_current.conf");

		for (int i = 0; i < argumentsForShowBestIndividual.size(); i += 2) {
			currentShowBestFile.println(argumentsForShowBestIndividual
					.elementAt(i)
					+ " "
					+ argumentsForShowBestIndividual.elementAt(i + 1) + "\n");
		}
		currentShowBestFile.println("--population loadA=" + outputDirectory
				+ "/populations/" + prefixA + populationFilename
				+ populationA.getNumberOfCurrentGeneration() + ",loadB="
				+ outputDirectory + "/populations/" + prefixB
				+ populationFilename
				+ populationB.getNumberOfCurrentGeneration()
				+ ",showbestCoevolved,stepsperrun="
				+ populationA.getNumberOfStepsPerSample());
		currentShowBestFile.println("--gui name=debug");
		currentShowBestFile.println("--random-seed " + randomSeed);
		currentShowBestFile.close();
	}

	private void saveShowCurrentBest(Population population, int randomSeed)
			throws FileNotFoundException {
		PrintStream currentShowBestFile = openForWriting(outputDirectory + "/_"
				+ showBestFilename + "_current.conf");

		for (int i = 0; i < argumentsForShowBestIndividual.size(); i += 2) {
			currentShowBestFile.println(argumentsForShowBestIndividual
					.elementAt(i)
					+ " "
					+ argumentsForShowBestIndividual.elementAt(i + 1) + "\n");
		}
		currentShowBestFile.println("--population load=" + outputDirectory
				+ "/populations/" + populationFilename
				+ population.getNumberOfCurrentGeneration()
				+ ",showbest,stepsperrun="
				+ population.getNumberOfStepsPerSample());
		currentShowBestFile.println("--gui name=debug");
		currentShowBestFile.println("--random-seed " + randomSeed);
		currentShowBestFile.close();
	}

	private void saveRestartFile(Population populationA,
			Population populationB, int randomSeed)
			throws FileNotFoundException {
		PrintStream restartFile = openForWriting(outputDirectory + "/"
				+ restartFilename);

		for (int i = 0; i < argumentsForRestartEvolution.size(); i += 2) {
			restartFile.println(argumentsForRestartEvolution.elementAt(i) + " "
					+ argumentsForRestartEvolution.elementAt(i + 1) + "\n");
		}
		restartFile.println("--population loadA=" + outputDirectory
				+ "/populations/A" + populationFilename
				+ populationA.getNumberOfCurrentGeneration() + ",loadB="
				+ outputDirectory + "/populations/B" + populationFilename
				+ populationB.getNumberOfCurrentGeneration());
		restartFile.println("--random-seed " + randomSeed);
		restartFile.close();
	}

	private void saveRestartFile(Population population, int randomSeed)
			throws FileNotFoundException {
		PrintStream restartFile = openForWriting(outputDirectory + "/"
				+ restartFilename);

		for (int i = 0; i < argumentsForRestartEvolution.size(); i += 2) {
			restartFile.println(argumentsForRestartEvolution.elementAt(i) + " "
					+ argumentsForRestartEvolution.elementAt(i + 1) + "\n");
		}
		restartFile.println("--population load=" + outputDirectory
				+ "/populations/" + populationFilename
				+ population.getNumberOfCurrentGeneration());
		restartFile.println("--random-seed " + randomSeed);
		restartFile.close();
	}

	private void saveShowBestFile(Population populationA,
			Population populationB, int randomSeed)
			throws FileNotFoundException {
		PrintStream showBestFile = openForWriting(outputDirectory
				+ "/show_best/" + showBestFilename
				+ populationA.getNumberOfCurrentGeneration() + ".conf");

		for (int i = 0; i < argumentsForShowBestIndividual.size(); i += 2) {
			showBestFile.println(argumentsForShowBestIndividual.elementAt(i)
					+ " " + argumentsForShowBestIndividual.elementAt(i + 1)
					+ "\n");
		}
		showBestFile.println("--population loadA=" + outputDirectory
				+ "/populations/A" + populationFilename
				+ populationB.getNumberOfCurrentGeneration() + ",loadB="
				+ outputDirectory + "/populations/B" + populationFilename
				+ populationB.getNumberOfCurrentGeneration()
				+ ",showbestCoevolved,stepsperrun="
				+ populationA.getNumberOfStepsPerSample());
		showBestFile.println("--gui name=debug");
		showBestFile.println("--random-seed " + randomSeed);
		showBestFile.close();

	}

	private void saveShowBestFile(Population population, int randomSeed)
			throws FileNotFoundException {
		PrintStream showBestFile = openForWriting(outputDirectory
				+ "/show_best/" + showBestFilename
				+ population.getNumberOfCurrentGeneration() + ".conf");

		for (int i = 0; i < argumentsForShowBestIndividual.size(); i += 2) {
			showBestFile.println(argumentsForShowBestIndividual.elementAt(i)
					+ " " + argumentsForShowBestIndividual.elementAt(i + 1)
					+ "\n");
		}
		showBestFile.println("--population load=" + outputDirectory
				+ "/populations/" + populationFilename
				+ population.getNumberOfCurrentGeneration()
				+ ",showbest,stepsperrun="
				+ population.getNumberOfStepsPerSample());
		showBestFile.println("--gui name=debug");
		showBestFile.println("--random-seed " + randomSeed);
		showBestFile.close();
	}

	private void savePopulationToFile(Population population, String prefix)
			throws FileNotFoundException, IOException {
		FileOutputStream fileOut = new FileOutputStream(outputDirectory
				+ "/populations/" + prefix + populationFilename
				+ population.getNumberOfCurrentGeneration());
		GZIPOutputStream gzipOut = new GZIPOutputStream(fileOut);
		ObjectOutputStream out = new ObjectOutputStream(gzipOut);
		out.writeObject(population);
		out.close();
	}

	private void saveGenerationNumber(Population population)
			throws FileNotFoundException {
		PrintStream generation = openForWriting(outputDirectory + "/"
				+ generationNumberFilename);
		generation.println(population.getNumberOfCurrentGeneration());
		generation.close();
	}

	protected PrintStream openForWriting(String filename)
			throws FileNotFoundException {
		return new PrintStream(new FileOutputStream(filename));
	}

	protected PrintStream openForWriting(String filename, boolean append)
			throws FileNotFoundException {
		return new PrintStream(new FileOutputStream(filename, append));
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

}
