package main.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/*
 * Creates a merged log of the evolution fitness
 */
public class PostEvolutionFitnessProcessor {
	private final String PATH = "C:\\Users\\BIOMACHINES\\Desktop\\Eclipse Data\\JBotEvolver\\JBotVasco\\experiments_automator\\targetFollowing_automator\\targetFollowing_basicSetup_normal_correctedErrors";
	// private final String PATH = "C:\\Users\\BIOMACHINES\\Desktop\\Eclipse
	// Data\\JBotEvolver\\JBotVasco\\experiments_automator\\targetFollowing_automator\\targetFollowing_basicSetup_hybrid_correctedErrors";

	private final String[] POST_DETAILS_DATA_FILE = { "post_details", "txt" };
	private final String SUFIX = "_noFaults";
	// private final String SUFIX = "_withFaults";

	private final String DATA_FOLDER_PATH = ".\\data";
	private final String SAVED_DATA_EXTENSION = "csv";

	private final int RUNS_QNT = 10;
	private final int GENERATIONS_QNT = 400;
	private final int SAMPLES_QNT = 6;

	public static void main(String[] args) {
		Locale.setDefault(new Locale("en", "US"));
		new PostEvolutionFitnessProcessor();
	}

	public PostEvolutionFitnessProcessor() {
		File folder = new File(PATH);
		if (folder.exists() && folder.isDirectory()) {
			String postFileName = POST_DETAILS_DATA_FILE[0] + SUFIX + "." + POST_DETAILS_DATA_FILE[1];
			File postFile = new File(folder, postFileName);
			if (postFile.exists() && postFile.isFile()) {
				double[][][] experimentFitnessData = getFitness(postFile);
				double[][] processedFitness = processFitness(experimentFitnessData);

				if (saveExperimentsToFile(processedFitness, folder.getName())) {
					System.out.println("Data saved with success!");
				} else {
					System.out.println("Error saving data!");
				}
			} else {
				System.out.println("Post details file not found: " + postFile.getAbsolutePath());
			}
		} else {
			System.out.println("Invalid path: " + PATH);
		}
	}

	private double[][][] getFitness(File file) {
		if (file.exists() && file.isFile()) {
			FileReader fileReader = null;
			BufferedReader inputBuffReader = null;
			double[][][] experimentsData = new double[RUNS_QNT][GENERATIONS_QNT + 1][SAMPLES_QNT];

			try {
				fileReader = new FileReader(file);
				inputBuffReader = new BufferedReader(fileReader);

				String line = "";
				while ((line = inputBuffReader.readLine()) != null) {
					if (!line.startsWith("$") && !line.startsWith("#")) {
						String[] elements = line.split(" ");

						if (elements.length == 5) {
							int runNumber = Integer.parseInt(elements[0]) - 1;
							int generationNumber = Integer.parseInt(elements[1]);
							int sampleNumber = Integer.parseInt(elements[3]);
							double fitnessValue = Double.parseDouble(elements[4]);

							experimentsData[runNumber][generationNumber][sampleNumber] = fitnessValue;
						} else {
							System.out.println("Invalid line! " + line);
						}
					}
				}
			} catch (IOException e) {
				System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
			} finally {
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
					}
				}

				if (inputBuffReader != null) {
					try {
						inputBuffReader.close();
					} catch (IOException e) {
						System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
					}
				}
			}

			return experimentsData;
		} else {
			System.out.println("Metrics file does not exist " + file.getAbsolutePath());
			return null;
		}
	}

	private double[][] processFitness(double[][][] experimentsData) {
		double[][] processedData = new double[RUNS_QNT][GENERATIONS_QNT + 1];

		for (int run = 0; run < RUNS_QNT; run++) {
			for (int generation = 0; generation <= GENERATIONS_QNT; generation++) {
				double meanValue = 0;

				for (int sample = 0; sample < SAMPLES_QNT; sample++) {
					meanValue += experimentsData[run][generation][sample];
				}

				processedData[run][generation] = meanValue / SAMPLES_QNT;
			}
		}

		return processedData;
	}

	private boolean saveExperimentsToFile(double[][] fitnessData, String filename) {
		String name = "postEvaluationFitness_" + filename + SUFIX + "." + SAVED_DATA_EXTENSION;
		PrintWriter pw = null;

		if (!new File(DATA_FOLDER_PATH).exists()) {
			new File(DATA_FOLDER_PATH).mkdir();
		}

		try {
			pw = new PrintWriter(new FileOutputStream(new File(DATA_FOLDER_PATH, name)));
			for (int generation = 0; generation < GENERATIONS_QNT; generation++) {
				for (int run = 0; run < RUNS_QNT; run++) {
					pw.print(String.format("%.8f", fitnessData[run][generation]));

					if (run < RUNS_QNT - 1) {
						pw.print("\t");
					}
				}

				if (generation < GENERATIONS_QNT - 1) {
					pw.println();
				}
			}

			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
}
