package logManaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

/**
 * Read NMEA GPS log files and convert them into GPSData objects for each robot
 * 
 * @author Vasco Costa
 *
 */
public class ValuesLogFilesParser {
	private final static String INPUT_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";
	private final static String FILE_PREFIX = "values_";
	private final String PARSED_DATA_FILE_LOG = "mergedLogs_logData.log";
	private final String REGEX_VALID_LOG_LINE = "^[\\w $.,;:?~!#\"^=()\t\\-\\/\\{\\}\\[\\]]+$";
	private final String REGEX_VALID_MESSAGE_LOG_LINE = "^[\\w $.,;:?~!#\"^=()\t\\-\\/\\{\\}\\[\\]]+\"[\\w $.,;:?~!#\"^=()\t\\-\\/\\{\\}\\[\\]]+\"[ ]?$";

	private File inputFolderFile;
	private HashMap<Integer, List<DecodedLog>> decodedLogData = new HashMap<Integer, List<DecodedLog>>();

	public ValuesLogFilesParser(boolean includePreprocessing) throws FileNotFoundException {
		this(INPUT_FOLDER, includePreprocessing);
	}

	public ValuesLogFilesParser(String inputFolderPath, boolean includePreprocessing) throws FileNotFoundException {
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
						throw new FileNotFoundException("Missing log file");
					} else {
						throw new FileNotFoundException("Ambiguous log files");
					}
				}
			}

			if (includePreprocessing) {
				for (File file : files) {
					System.out.printf("[%s] Pre-processing %s%n", getClass().getSimpleName(), file.getAbsolutePath());
					preprocessLogFile(file);
				}
			}

			for (File file : files) {
				System.out.printf("[%s] Parsing %s%n", getClass().getSimpleName(), file.getAbsolutePath());

				int currentRobot = Integer.parseInt(file.getParentFile().getName());
				List<DecodedLog> data = parseLogFile(file);
				decodedLogData.put(currentRobot, data);

				if (data.size() > 0) {
					System.out.printf("[%s] -----> %d log lines parsed%n", getClass().getSimpleName(), data.size());
				}
			}
		} else {
			throw new FileNotFoundException("Input folder does not exist");
		}
	}

	private void preprocessLogFile(File inputFile) {
		FileReader fileReader = null;
		BufferedReader inputBuffReader = null;

		File temporaryFile = null;
		FileWriter fileWriter = null;
		BufferedWriter outputBuffWriter = null;
		int parsingErrors = 0;

		try {
			fileReader = new FileReader(inputFile);
			inputBuffReader = new BufferedReader(fileReader);

			temporaryFile = File.createTempFile(getClass().getSimpleName() + "_" + inputFile.getName(), ".tmp");
			fileWriter = new FileWriter(temporaryFile);
			outputBuffWriter = new BufferedWriter(fileWriter);

			String line = "";
			String messageString = "";
			boolean inMessage = false;

			while ((line = inputBuffReader.readLine()) != null) {
				if (!line.isEmpty() && line.matches(REGEX_VALID_LOG_LINE)) {
					if (inMessage) {
						messageString += line.replaceAll("\n", "\t");
						messageString += " ";

						if (line.contains("\"")) {
							inMessage = false;
							outputBuffWriter.write(messageString);
							outputBuffWriter.newLine();
							outputBuffWriter.flush();
						}
					} else {
						String[] infoBlocks = line.split(LogCodex.MAIN_SEPARATOR);

						try {
							LogType logType = LogType.valueOf(infoBlocks[0].substring(3, infoBlocks[0].length()));
							if (logType == LogType.MESSAGE) {
								if (line.matches(REGEX_VALID_MESSAGE_LOG_LINE)) {
									inMessage = false;
									outputBuffWriter.write(messageString);
									outputBuffWriter.newLine();
									outputBuffWriter.flush();
								} else {
									inMessage = true;
									messageString = line.replaceAll("\n", "\t");
								}
							} else {
								outputBuffWriter.write(line);
								outputBuffWriter.newLine();
								outputBuffWriter.flush();
							}
						} catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
							System.out.println("Error line: " + line);
							parsingErrors++;
						}
					}
				}
			}
		} catch (IOException e) {
			System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getLocalizedMessage());
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

			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				}
			}

			if (outputBuffWriter != null) {
				try {
					outputBuffWriter.close();
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				}
			}

			if (temporaryFile != null) {
				try {
					Files.move(temporaryFile.toPath(), inputFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				}
			}

			if (parsingErrors > 0)
				System.out.printf("[%s] -----> %d pre-processing errors%n", getClass().getSimpleName(), parsingErrors);
		}
	}

	private List<DecodedLog> parseLogFile(File inputFile) {
		List<DecodedLog> data = new ArrayList<DecodedLog>();

		FileReader inputReader = null;
		BufferedReader inputBuffReader = null;
		int parsingErrors = 0;
		try {
			inputReader = new FileReader(inputFile);
			inputBuffReader = new BufferedReader(inputReader);

			String line = "";
			while ((line = inputBuffReader.readLine()) != null) {
				if (!line.startsWith("#")) {
					data.add(LogCodex.decodeLog(line));
				}
			}
		} catch (IOException e) {
			System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
		} finally {
			if (inputReader != null) {
				try {
					inputReader.close();
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

		if (parsingErrors > 0)
			System.out.printf("[%s] -----> %d parsing errors%n", getClass().getSimpleName(), parsingErrors);
		return data;
	}

	public HashMap<Integer, List<DecodedLog>> getDecodedLogData() {
		return decodedLogData;
	}

	public void saveParsedDataToFile() throws FileAlreadyExistsException, FileSystemException {
		File file = new File(INPUT_FOLDER, PARSED_DATA_FILE_LOG);
		if (file.exists()) {
			throw new FileAlreadyExistsException("File already exist");
		} else {
			FileUtils.ExperiencesDataOnFile data_gps = new FileUtils.ExperiencesDataOnFile();
			data_gps.setDecodedLogData(decodedLogData);
			if (!FileUtils.saveDataToFile(data_gps, PARSED_DATA_FILE_LOG, true)) {
				throw new FileSystemException("Error writing log data data to file");
			}
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		System.out.printf("[%S] [INIT]%n", ValuesLogFilesParser.class.getSimpleName());
		new ValuesLogFilesParser(INPUT_FOLDER, true);
		System.out.printf("[%S] [FINISHED]%n", ValuesLogFilesParser.class.getSimpleName());
	}
}
