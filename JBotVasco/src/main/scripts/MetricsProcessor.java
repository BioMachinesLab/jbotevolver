package main.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MetricsProcessor {
	private final String[] PATH = {
			"C:\\Users\\BIOMACHINES\\Desktop\\Eclipse Data\\JBotEvolver\\JBotVasco\\experiments_automator\\targetFollowing_automator\\targetFollowing_basicSetup_normal_correctedErrors",
			"C:\\Users\\BIOMACHINES\\Desktop\\Eclipse Data\\JBotEvolver\\JBotVasco\\experiments_automator\\targetFollowing_automator\\targetFollowing_basicSetup_hybrid_correctedErrors" };
	private final String[] POST_DATA_FILE = { "post", "txt" };
	private final String METRICS_FILE_PREFIX = "_metrics";
	private final String SUFIX = "_noFaults";
	// private final String SUFIX = "_withFaults";

	private final String DATA_FOLDER_PATH = ".\\data";
	private final String SAVED_DATA_EXTENSION = "csv";

	public static void main(String[] args) {
		new MetricsProcessor();
	}

	public MetricsProcessor() {
		HashMap<String, String[]> metricsLinesPerRun = new HashMap<String, String[]>();

		for (String path : PATH) {
			File folder = new File(path);

			if (folder.exists() && folder.isDirectory()) {
				String postFileName = POST_DATA_FILE[0] + SUFIX + "." + POST_DATA_FILE[1];
				File postFile = new File(folder, postFileName);
				if (postFile.exists() && postFile.isFile()) {
					HashMap<Integer, Integer> generationNumbers = getGenerationNumbersFromFile(postFile);
					String[] lines = getMetricLines(folder, generationNumbers);
					metricsLinesPerRun.put(path, lines);
				} else {
					System.out.println("Post file not found: " + postFile.getAbsolutePath());
				}
			} else {
				System.out.println("Invalid path: " + PATH);
			}
		}

		saveMetricsBestOf(metricsLinesPerRun);
		saveMetricsBestOfExpanded(metricsLinesPerRun);
		System.out.println("Done!");
	}

	private HashMap<Integer, Integer> getGenerationNumbersFromFile(File file) {
		FileReader fileReader = null;
		BufferedReader inputBuffReader = null;
		HashMap<Integer, Integer> generationNumbers = new HashMap<Integer, Integer>();

		try {
			fileReader = new FileReader(file);
			inputBuffReader = new BufferedReader(fileReader);

			String line = "";
			while ((line = inputBuffReader.readLine()) != null) {
				if (!line.startsWith("#") && line.matches("^[0-9 \\.()+-]*$")) {
					String[] split = line.split(" ");

					int runNumber = Integer.parseInt(split[0]);
					int generationNumber = Integer.parseInt(split[6]);
					generationNumbers.put(runNumber, generationNumber);
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

		return generationNumbers;
	}

	private String[] getMetricLines(File folder, HashMap<Integer, Integer> generationNumbers) {
		String[] metricLines = new String[generationNumbers.keySet().size() + 1];
		int index = 0;
		boolean firstLine = true;

		for (int run : generationNumbers.keySet()) {
			File runFolder = new File(folder, Integer.toString(run));
			if (runFolder.exists() && runFolder.isDirectory()) {

				File[] files = runFolder.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith(METRICS_FILE_PREFIX) && name.contains(SUFIX);
					}
				});

				if (files.length == 1) {
					if (firstLine) {
						metricLines[index++] = getMetricsHeader(files[0]);
						firstLine = false;
					}

					metricLines[index++] = getMetricsLine(files[0], generationNumbers.get(run));
				} else {
					if (files.length > 1) {
						System.out.println("Multiple metric files for run " + run);
					}
				}
			} else {
				System.out.println("Folder not found: " + runFolder.getAbsolutePath());
			}
		}

		return metricLines;
	}

	private String getMetricsHeader(File file) {
		if (file.exists() && file.isFile()) {
			FileReader fileReader = null;
			BufferedReader inputBuffReader = null;

			try {
				fileReader = new FileReader(file);
				inputBuffReader = new BufferedReader(fileReader);

				String line = "";
				while ((line = inputBuffReader.readLine()) != null) {
					if (line.startsWith("$")) {
						return line;
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

			return null;
		} else {
			System.out.println("Metrics file does not exist " + file.getAbsolutePath());
			return null;
		}
	}

	private String getMetricsLine(File file, int generationNumber) {
		if (file.exists() && file.isFile()) {
			FileReader fileReader = null;
			BufferedReader inputBuffReader = null;

			try {
				fileReader = new FileReader(file);
				inputBuffReader = new BufferedReader(fileReader);

				String line = "";
				while ((line = inputBuffReader.readLine()) != null) {
					if (line.startsWith(Integer.toString(generationNumber))) {
						return line;
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

			return null;
		} else {
			System.out.println("Metrics file does not exist " + file.getAbsolutePath());
			return null;
		}
	}

	private boolean saveMetricsBestOf(HashMap<String, String[]> metricsLinesPerRun) {
		int totalSize = 1;
		for (String path : metricsLinesPerRun.keySet()) {
			totalSize += metricsLinesPerRun.get(path).length;
		}

		String[] metricsLines = new String[totalSize];
		int index = 0;
		for (String path : metricsLinesPerRun.keySet()) {
			String[] lines = metricsLinesPerRun.get(path);

			String controllerType = "";
			if (path.contains("hybrid")) {
				controllerType = "Hybrid";
			} else if (path.contains("normal")) {
				controllerType = "Monolithic";
			}

			for (int i = 0; i < lines.length; i++) {
				if (lines[i] != null) {
					if (index == 0 && i == 0) {
						metricsLines[index++] = lines[i] + "\tcontroller";
					} else if (index > 0 && i != 0) {
						metricsLines[index++] = lines[i] + "\t" + controllerType;
					}
				}
			}
		}

		return saveLinesToFile(metricsLines, "bestof_metrics");
	}

	private boolean saveMetricsBestOfExpanded(HashMap<String, String[]> metricsLinesPerRun) {
		// Get headers to use
		String key = metricsLinesPerRun.keySet().iterator().next();
		String[] tempLines = metricsLinesPerRun.get(key);
		HashMap<Integer, String> labels = new HashMap<Integer, String>();
		for (String line : tempLines) {
			if (line.startsWith("$")) {
				String[] args = line.replaceAll("\\$\t", "").split("\t");

				for (int i = 0; i < args.length; i++) {
					labels.put(i, args[i]);
				}
			}
		}

		List<String> allLines = new ArrayList<String>();
		allLines.add("generation\tvalue\tname\tmeasure\tcontroller");
		for (String path : metricsLinesPerRun.keySet()) {
			String[] lines = metricsLinesPerRun.get(path);

			String controllerType = "";
			if (path.contains("hybrid")) {
				controllerType = "Hybrid";
			} else if (path.contains("normal")) {
				controllerType = "Monolithic";
			}

			// Starts in 1 to ignore the headers
			for (int i = 1; i < lines.length; i++) {
				if (lines[i] != null && i != 0) {
					String[] values = lines[i].split("\t");

					String generation = values[0];
					// Starts in 1 to ignore the generation
					for (int j = 1; j < values.length; j++) {
						String name = labels.get(j).split("_")[0];
						String measure = labels.get(j).split("_")[1];

						String line = generation + "\t" + values[j] + "\t" + name + "\t" + measure + "\t"
								+ controllerType;
						allLines.add(line);
					}
				}
			}
		}

		return saveLinesToFile(allLines.toArray(new String[allLines.size()]), "bestof_metrics_expanded");
	}

	private boolean saveLinesToFile(String[] lines, String filename) {
		FileWriter fileWriter = null;
		PrintWriter outputPrintWriter = null;

		try {
			fileWriter = new FileWriter(new File(DATA_FOLDER_PATH, filename + SUFIX + "." + SAVED_DATA_EXTENSION));
			outputPrintWriter = new PrintWriter(fileWriter);

			for (int i = 0; i < lines.length; i++) {
				if (lines[i] != null) {
					outputPrintWriter.println(lines[i].replaceAll("\\$\t", ""));
				}
			}
		} catch (IOException e) {
			System.err.println("Error creating file " + filename);
			return false;
		} finally {
			outputPrintWriter.close();

			try {
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error closing the file " + filename);
			}
		}

		return true;
	}
}
