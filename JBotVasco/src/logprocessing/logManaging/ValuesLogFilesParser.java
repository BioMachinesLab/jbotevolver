package logprocessing.logManaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

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

	private File inputFolderFile;
	private HashMap<Integer, ArrayList<DecodedLog>> decodedLogData = new HashMap<Integer, ArrayList<DecodedLog>>();

	public ValuesLogFilesParser(String inputFolderPath) {
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
				System.out.printf("[%s] Processing %s%n", getClass().getSimpleName(), file.getAbsolutePath());
				preprocessLogFile(file);
			}
			System.exit(0);
			for (File file : files) {
				System.out.printf("[%s] Parsing %s%n", getClass().getSimpleName(), file.getAbsolutePath());

				int currentRobot = Integer.parseInt(file.getParentFile().getName());
				ArrayList<DecodedLog> data = parseLogFile(file);
				decodedLogData.put(currentRobot, data);
				System.out.printf("[%s] -----> %d log lines parsed%n", getClass().getSimpleName(), data.size());
			}
		} else {
			System.err.printf("[%s] Input folder does not exist%n", getClass().getSimpleName());
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

			String line = null;
			String messageString = "";
			boolean inMessage = false;

			while ((line = inputBuffReader.readLine()) != null) {
				if (!line.isEmpty() && line.matches("^[A-Za-z0-9\t =._:;\"\\[\\]!-]+$")) {
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
								if (line.matches(
										"^[A-Za-z0-9\t =._:;\\[\\]!-]+\"[A-Za-z0-9\t =._:;\\[\\]!-]+\"[ ]?$")) {
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
			e.printStackTrace();
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

			System.out.printf("[%s] -----> %d parsing errors%n", getClass().getSimpleName(), parsingErrors);
		}
	}

	private ArrayList<DecodedLog> parseLogFile(File inputFile) {
		ArrayList<DecodedLog> data = new ArrayList<DecodedLog>();

		FileReader inputReader = null;
		BufferedReader inputBuffReader = null;
		try {
			inputReader = new FileReader(inputFile);
			inputBuffReader = new BufferedReader(inputReader);

			String line = inputBuffReader.readLine();
			while (line != null) {
				data.add(LogCodex.decodeLog(line));
				line = inputBuffReader.readLine();
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

		return data;
	}

	public HashMap<Integer, ArrayList<DecodedLog>> getDecodedLogData() {
		return decodedLogData;
	}

	public static void main(String[] args) {
		System.out.printf("[%S] [INIT]%n", ValuesLogFilesParser.class.getSimpleName());
		new ValuesLogFilesParser(INPUT_FOLDER);
		System.out.printf("[%S] [FINISHED]%n", ValuesLogFilesParser.class.getSimpleName());
	}
}
