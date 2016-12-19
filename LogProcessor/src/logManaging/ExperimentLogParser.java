package logManaging;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

public class ExperimentLogParser implements Serializable {
	private static final long serialVersionUID = 695124312870952574L;
	private final boolean PRINT_INFORMATION_RESUME = true;
	private final String INPUT_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";
	private final String FILE_PREFIX = "entity_";
	private final String PARSED_DATA_FILE_EXPERIMENTS = "mergedLogs_experiments.log";
	private final String REGEX_VALID_LOG_LINE = "^[\\w $.,;:?~!#\"^=()\t\\-\\/\\{\\}\\[\\]]+$";

	private File inputFolderFile;
	private HashMap<Integer, ExperimentData> experimentsData = new HashMap<Integer, ExperimentData>();

	public ExperimentLogParser(String inputFolderPath) throws FileNotFoundException {
		if (inputFolderPath == null) {
			inputFolderPath = INPUT_FOLDER;
		}

		inputFolderFile = new File(inputFolderPath);
		if (inputFolderFile.exists() && inputFolderFile.isDirectory()) {
			ArrayList<File> files = new ArrayList<File>();

			for (File folder : inputFolderFile.listFiles()) {
				if (folder.isDirectory()) {
					FilenameFilter filenameFilter = new FilenameFilter() {

						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith(FILE_PREFIX);
						}
					};
					File[] fileList = folder.listFiles(filenameFilter);

					if (fileList.length == 1) {
						files.add(fileList[0]);
					} else if (fileList.length == 0) {
						System.err.printf("[%s] Missing log file%n", getClass().getSimpleName());
					} else {
						System.err.printf("[%s] Ambiguous log files%n", getClass().getSimpleName());
					}
				}
			}

			for (File file : files) {
				System.out.printf("[%s] Parsing %s%n", getClass().getSimpleName(), file.getAbsolutePath());

				HashMap<Integer, ExperimentData> data = parseExperimentsData(file);

				for (int key : data.keySet()) {
					if (experimentsData.containsKey(key)) {
						ExperimentData experimentData = mergeExperimentsData(experimentsData.remove(key),
								data.get(key));
						experimentsData.put(key, experimentData);
					} else {
						experimentsData.put(key, data.get(key));
					}
				}

				if (data.size() > 0) {
					System.out.printf("[%s] -----> %02d experiments parsed: ", getClass().getSimpleName(), data.size());

					for (int key : data.keySet()) {
						System.out.printf("%02d ", key);
					}
					System.out.println();
				}
			}

			if (PRINT_INFORMATION_RESUME) {
				System.out.printf("[%s] ###############################%n", getClass().getSimpleName());
				System.out.printf("[%s] Parse resume:%n", getClass().getSimpleName());
				System.out.printf("[%s] >> %d experiments parsed%n", getClass().getSimpleName(),
						experimentsData.keySet().size());

				for (int key : experimentsData.keySet()) {
					System.out.printf("[%s] >>>> Experiment: %02d\tTimesteps: %04.0f\tRobot(s) - %d: ",
							getClass().getSimpleName(), experimentsData.get(key).experimentNumber,
							experimentsData.get(key).timestepsCount,
							experimentsData.get(key).stepsData.keySet().size());

					for (int key2 : experimentsData.get(key).stepsData.keySet()) {
						System.out.printf("%d ", key2);
					}
					System.out.println();
				}

			}
		} else {
			throw new FileNotFoundException("Input folder does not exist");
		}
	}

	private HashMap<Integer, ExperimentData> parseExperimentsData(File inputFile) {
		HashMap<Integer, ExperimentData> experimentsData = new HashMap<Integer, ExperimentData>();

		int parsingErrors = 0;
		int currentRobot = Integer.parseInt(inputFile.getParentFile().getName());
		String[] data = segmentExperimentsData(inputFile);

		if (data.length > 0) {
			for (String experiment : data) {

				// https://www.mkyong.com/java/how-to-convert-string-to-inputstream-in-java/
				InputStream is = new ByteArrayInputStream(experiment.getBytes());
				BufferedReader br = new BufferedReader(new InputStreamReader(is));

				String line = "";
				int experimentNumber = -1;
				double timesteps = -1;
				int ordernumber = 0;
				List<ExperimentStep> experimentSteps = new ArrayList<ExperimentStep>();

				try {
					while ((line = br.readLine()) != null) {
						if (!line.isEmpty() && line.matches(REGEX_VALID_LOG_LINE)) {
							try {
								if (line.startsWith("###")) {
									String n = line.replaceAll("###exp", "");
									experimentNumber = Integer.parseInt(n);
								} else {
									String[] infoBlocks = line.split(LogCodex.MAIN_SEPARATOR);
									LogType logType = LogType
											.valueOf(infoBlocks[0].substring(3, infoBlocks[0].length()));

									if (logType == LogType.ENTITIES) {
										DecodedLog decodedLog = LogCodex.decodeLog(line);

										if (decodedLog.getPayloadType() == LogType.ENTITIES) {
											EntityManipulation entityManipulation = (EntityManipulation) decodedLog
													.getPayload()[1];

											ExperimentStep step = new ExperimentStep();
											step.entityManipulationData = entityManipulation;
											step.ordernumber = ordernumber++;

											if (entityManipulation.getTimestep() > timesteps) {
												timesteps = entityManipulation.getTimestep();
											}
										} else {
											throw new IllegalArgumentException("Invalied log type for this tool");
										}
									} else {
										throw new IllegalArgumentException("Invalied log type for this tool");
									}
								}
							} catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
								System.out.println("Error line: " + line);
								parsingErrors++;
							}
						}
					}
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				} finally {

					try {
						is.close();
					} catch (IOException e) {
						System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
					}

					try {
						br.close();
					} catch (IOException e) {
						System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
					}
				}

				HashMap<Integer, List<ExperimentStep>> stepsData = new HashMap<Integer, List<ExperimentStep>>();
				stepsData.put(currentRobot, experimentSteps);

				ExperimentData experimentData = new ExperimentData();
				experimentData.experimentNumber = experimentNumber;
				experimentData.timestepsCount = timesteps;
				experimentData.stepsData = stepsData;
				experimentsData.put(experimentNumber, experimentData);
			}
		}

		if (parsingErrors > 0) {
			System.out.printf("[%s] -----> %d parsing errors%n", getClass().getSimpleName(), parsingErrors);
		}

		return experimentsData;
	}

	private String[] segmentExperimentsData(File inputFile) {
		ArrayList<String> experiments = new ArrayList<>();

		FileReader fileReader = null;
		BufferedReader inputBuffReader = null;
		try {
			fileReader = new FileReader(inputFile);
			inputBuffReader = new BufferedReader(fileReader);

			String line = "";
			StringBuilder stringBuilder = new StringBuilder();
			boolean inExperiment = false;
			while ((line = inputBuffReader.readLine()) != null) {
				if (!line.isEmpty() && line.matches(REGEX_VALID_LOG_LINE)) {
					// In the beginning
					if (!inExperiment && line.startsWith("###")) {
						inExperiment = true;
						stringBuilder = new StringBuilder(line + "\n");
						// Between experiments
					} else if (inExperiment && line.startsWith("###")) {
						experiments.add(stringBuilder.toString());
						stringBuilder = new StringBuilder(line + "\n");
					} else if (inExperiment) {
						stringBuilder.append(line + "\n");
					}
				}
			}

			experiments.add(stringBuilder.toString());
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

		return experiments.toArray(new String[experiments.size()]);
	}

	private ExperimentData mergeExperimentsData(ExperimentData experimentData1, ExperimentData experimentData2) {
		if (experimentData1.experimentNumber != experimentData2.experimentNumber) {
			throw new IllegalArgumentException("The experiments numbers do not match");
		}

		ExperimentData experimentData = new ExperimentData();
		experimentData.experimentNumber = experimentData1.experimentNumber;
		if (experimentData2.timestepsCount >= experimentData1.timestepsCount) {
			experimentData.timestepsCount = experimentData2.timestepsCount;
		} else {
			experimentData.timestepsCount = experimentData1.timestepsCount;
		}

		for (int key : experimentData1.stepsData.keySet()) {
			experimentData.stepsData.put(key, experimentData1.stepsData.get(key));
		}

		// Experiment 1 data takes priority over 2
		for (int key : experimentData2.stepsData.keySet()) {
			if (experimentData.stepsData.get(key) == null) {
				experimentData.stepsData.put(key, experimentData2.stepsData.get(key));
			}
		}

		return experimentData;
	}

	public class ExperimentData implements Serializable {
		private static final long serialVersionUID = -405600509166311277L;
		public double timestepsCount;
		public int experimentNumber;
		public HashMap<Integer, List<ExperimentStep>> stepsData = new HashMap<Integer, List<ExperimentStep>>();
	}

	public class ExperimentStep implements Serializable {
		private static final long serialVersionUID = 4585308655579494159L;
		public int ordernumber;
		public EntityManipulation entityManipulationData;
	}

	public HashMap<Integer, ExperimentData> getExperimentsData() {
		return experimentsData;
	}

	public void saveParsedDataToFile(boolean override) throws FileAlreadyExistsException, FileSystemException {
		File file = new File(INPUT_FOLDER, PARSED_DATA_FILE_EXPERIMENTS);
		if (file.exists() && !override) {
			throw new FileAlreadyExistsException("File already exist");
		} else {
			FileUtils.ExperimentsDataOnFile data_experiment = new FileUtils.ExperimentsDataOnFile();
			data_experiment.setExperimentsData(experimentsData);
			if (!FileUtils.saveDataToCompressedFile(data_experiment, file, false)) {
				throw new FileSystemException("Error writing entities data to file");
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException, FileSystemException {
		System.out.printf("[%S] [INIT]%n", EntitiesLogFilesParser.class.getSimpleName());
		new ExperimentLogParser(null);
		System.out.printf("[%S] [FINISHED]%n", EntitiesLogFilesParser.class.getSimpleName());
	}
}
