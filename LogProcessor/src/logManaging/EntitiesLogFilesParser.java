package logManaging;

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

import commoninterface.entities.RobotLocation;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.LogCodex;
import commoninterface.utils.logger.LogCodex.LogType;

/**
 * Read NMEA GPS log files and convert them into GPSData objects for each robot
 * 
 * @author Vasco Costa
 *
 */
public class EntitiesLogFilesParser {
	private final static String INPUT_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";
	private final static String FILE_PREFIX = "entity_";

	private File inputFolderFile;
	private HashMap<Integer, ArrayList<EntityManipulation>> entitiesManipulationData = new HashMap<Integer, ArrayList<EntityManipulation>>();

	public EntitiesLogFilesParser(String inputFolderPath) {
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
				System.out.printf("[%s] Pre-processing %s%n", getClass().getSimpleName(), file.getAbsolutePath());
				preprocessLogFile(file);
			}

			for (File file : files) {
				System.out.printf("[%s] Parsing %s%n", getClass().getSimpleName(), file.getAbsolutePath());

				int currentRobot = Integer.parseInt(file.getParentFile().getName());
				ArrayList<EntityManipulation> data = parseEntitiesData(file);
				entitiesManipulationData.put(currentRobot, data);
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

			String line = "";
			while (line != null && (line = inputBuffReader.readLine()) != null) {
				if (!line.isEmpty() && !line.matches("^\\s?$")) {
					if (line.contains(RobotLocation.class.getSimpleName())
							&& line.contains(LogCodex.ENTITY_OP_SEP + EntityManipulation.Operation.REMOVE)) {
						String previousLine = line;

						if ((line = inputBuffReader.readLine()) != null) {
							if (line.contains(RobotLocation.class.getSimpleName())
									&& line.contains(LogCodex.ENTITY_OP_SEP + EntityManipulation.Operation.ADD)) {
								line = line.replace(EntityManipulation.Operation.ADD.name(),
										EntityManipulation.Operation.MOVE.name());
							} else {
								if (!previousLine.isEmpty() && !previousLine.matches("^\\s?$")) {
									outputBuffWriter.write(previousLine);
									outputBuffWriter.newLine();
									outputBuffWriter.flush();
								}
							}

							if (!line.isEmpty() && !line.matches("^\\s?$")) {
								outputBuffWriter.write(line);
								outputBuffWriter.newLine();
								outputBuffWriter.flush();
							}
						} else {
							if (!previousLine.isEmpty() && !previousLine.matches("^\\s?$")) {
								outputBuffWriter.write(previousLine);
								outputBuffWriter.newLine();
								outputBuffWriter.flush();
							}
						}
					} else {
						outputBuffWriter.write(line);
						outputBuffWriter.newLine();
						outputBuffWriter.flush();
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

			if (parsingErrors > 0)
				System.out.printf("[%s] -----> %d pre-processing errors%n", getClass().getSimpleName(), parsingErrors);
		}
	}

	private ArrayList<EntityManipulation> parseEntitiesData(File inputFile) {
		ArrayList<EntityManipulation> data = new ArrayList<EntityManipulation>();
		int parsingErrors = 0;

		FileReader fileReader = null;
		BufferedReader inputBuffReader = null;
		try {
			fileReader = new FileReader(inputFile);
			inputBuffReader = new BufferedReader(fileReader);

			String line = inputBuffReader.readLine();
			while ((line = inputBuffReader.readLine()) != null) {
				if (!line.isEmpty() && line.matches("^[A-Za-z0-9= \t_.,;:!{}()-\\]\\[]+")) {
					String[] infoBlocks = line.split(LogCodex.MAIN_SEPARATOR);

					try {
						LogType logType = LogType.valueOf(infoBlocks[0].substring(3, infoBlocks[0].length()));

						if (logType == LogType.ENTITIES) {
							DecodedLog decodedLog = LogCodex.decodeLog(line);

							if (decodedLog.getPayloadType() == LogType.ENTITIES) {
								System.out.println("Class: " + decodedLog.getPayload().getClass());
								data.add((EntityManipulation) decodedLog.getPayload()[1]);
							} else {
								throw new IllegalArgumentException("Invalied log type for this tool");
							}
						} else {
							throw new IllegalArgumentException("Invalied log type for this tool");
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

		System.out.printf("[%s] -----> %d parsing errors%n", getClass().getSimpleName(), parsingErrors);
		return data;
	}

	/**
	 * Gets the parsed GPS data
	 * 
	 * @return a map with the parsed GPS data where the robot index is the key
	 */
	public HashMap<Integer, ArrayList<EntityManipulation>> getEntitiesManipulationData() {
		return entitiesManipulationData;
	}

	public static void main(String[] args) {
		System.out.printf("[%S] [INIT]%n", EntitiesLogFilesParser.class.getSimpleName());
		new EntitiesLogFilesParser(INPUT_FOLDER);
		System.out.printf("[%S] [FINISHED]%n", EntitiesLogFilesParser.class.getSimpleName());
	}
}
