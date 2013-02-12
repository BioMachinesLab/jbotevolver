package evolutionaryrobotics.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import simulation.util.Arguments;
import evolutionaryrobotics.populations.Population;

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

	public void saveCommandlineArguments(HashMap<String,Arguments> args) throws FileNotFoundException {
		if (outputDirectory != null) {
			PrintStream argumentsFile = openForWriting(outputDirectory + "/" + argumentsFilename);
			
			for(String name : args.keySet()) {
				
				String fullArgument = args.get(name).getCompleteArgumentString();
				String beautifiedArgument = beautifyString(fullArgument);
				String save = name+"\n"+beautifiedArgument;
				
				argumentsFile.println(save);
				if (!name.equalsIgnoreCase("--population")) {
					argumentsForShowBestIndividual.add(save);
					argumentsForRestartEvolution.add(save);
				}
			}
			argumentsFile.close();
		}
	}
	
	private String beautifyString(String s) {
		
		int nParenthesis = 0;
		String newString = "\t";
		
		for(int i = 0 ; i < s.length(); i++) {
			char c = s.charAt(i);
			
			switch(c) {
				case ',':
					newString+=",\n";
					newString+=repeatString("\t", nParenthesis+1);
					break;
				case '(':
					nParenthesis++;
					newString+="(\n";
					newString+=repeatString("\t", nParenthesis+1);
					break;
				case ')':
					nParenthesis--;
					newString+="\n";
					newString+=repeatString("\t", nParenthesis+1);
					newString+=")";
					break;
				default:
					newString+=c;
			}
		}
		return newString;
	}
	
	private String repeatString(String s, int n) {
		String newString = "";
		
		for(int i = 0 ; i < n ; i++)
			newString+=s;
		
		return newString;
	}

	public void close() {
		if (outputDirectory != null) {
			fitnessLog.close();
		}
	}

	public void savePopulation(Population populationA, Population populationB, Random simRandom) throws IOException {
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

	public void savePopulation(Population population, Random simRandom)
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

		
		for(String s : argumentsForShowBestIndividual)
			currentShowBestFile.println(s);
		
		currentShowBestFile.println("--population loadA=" + outputDirectory
				+ "/populations/" + prefixA + populationFilename
				+ populationA.getNumberOfCurrentGeneration() + ",loadB="
				+ outputDirectory + "/populations/" + prefixB
				+ populationFilename
				+ populationB.getNumberOfCurrentGeneration()
				+ ",showbestCoevolved");
		currentShowBestFile.println("--random-seed " + randomSeed);
		currentShowBestFile.close();
	}

	private void saveShowCurrentBest(Population population, int randomSeed)
			throws FileNotFoundException {
		PrintStream currentShowBestFile = openForWriting(outputDirectory + "/_"
				+ showBestFilename + "_current.conf");

		for(String s : argumentsForShowBestIndividual)
			currentShowBestFile.println(s);
		
		currentShowBestFile.println("--population load=" + outputDirectory
				+ "/populations/" + populationFilename
				+ population.getNumberOfCurrentGeneration()
				+ ",showbest");
		currentShowBestFile.println("--random-seed " + randomSeed);
		currentShowBestFile.close();
	}

	private void saveRestartFile(Population populationA,
			Population populationB, int randomSeed)
			throws FileNotFoundException {
		PrintStream restartFile = openForWriting(outputDirectory + "/"
				+ restartFilename);

		for(String s : argumentsForRestartEvolution)
			restartFile.println(s);
		
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

		
		for(String s : argumentsForRestartEvolution)
			restartFile.println(s);

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

		for(String s : argumentsForShowBestIndividual)
			showBestFile.println(s);

		showBestFile.println("--population loadA=" + outputDirectory
				+ "/populations/A" + populationFilename
				+ populationB.getNumberOfCurrentGeneration() + ",loadB="
				+ outputDirectory + "/populations/B" + populationFilename
				+ populationB.getNumberOfCurrentGeneration()
				+ ",showbestCoevolved");
		showBestFile.println("--random-seed " + randomSeed);
		showBestFile.close();

	}

	private void saveShowBestFile(Population population, int randomSeed)
			throws FileNotFoundException {
		PrintStream showBestFile = openForWriting(outputDirectory
				+ "/show_best/" + showBestFilename
				+ population.getNumberOfCurrentGeneration() + ".conf");

		for(String s : argumentsForShowBestIndividual)
			showBestFile.println(s);
		
		showBestFile.println("--population load=" + outputDirectory + "/populations/" + populationFilename + population.getNumberOfCurrentGeneration()+ ",showbest");
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
