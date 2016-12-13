package logManaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Merge log files per type and per robot. Files should be in (Main File)\(Robot
 * number)\(log files) structure
 * 
 * @author Vasco Costa
 *
 */
public class LogFilesMerger {
	private final static String INPUT_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\logs";
	private final static String OUTPUT_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";
	private final static String[] FILES_PREFIXES = new String[] { "entity_", "GPSLog_", "values_" };

	private File inputFolderFile;
	private File outputFolderFile;

	public LogFilesMerger() throws FileNotFoundException, FileAlreadyExistsException, FileSystemException {
		this(INPUT_FOLDER, OUTPUT_FOLDER);
	}

	public LogFilesMerger(String inputFolderPath, String outputFolderPath)
			throws FileNotFoundException, FileAlreadyExistsException, FileSystemException {
		inputFolderFile = new File(inputFolderPath);

		if (inputFolderFile.exists() && inputFolderFile.isDirectory()) {
			outputFolderFile = new File(outputFolderPath);
			if (!outputFolderFile.exists()) {
				if (!outputFolderFile.mkdir()) {
					throw new FileSystemException("Unable to create output directory");
				}

				for (File tempInputFolder : inputFolderFile.listFiles()) {
					if (tempInputFolder.isDirectory()) {
						File tempOutputFolder = new File(outputFolderFile, tempInputFolder.getName());

						if (!tempOutputFolder.exists()) {
							tempOutputFolder.mkdir();
						}

						mergeLogs(tempInputFolder, tempOutputFolder);
					}
				}
			} else {
				throw new FileAlreadyExistsException("Output folder already exist (probably merging was already performed)");
			}
		} else {
			throw new FileNotFoundException("Input folder does not exist");
		}
	}

	private void mergeLogs(File inputFolder, File outputFolder) throws FileSystemException {
		System.out.printf("[%s] Merging files in %s%n", getClass().getSimpleName(), inputFolder.getAbsolutePath());

		for (final String prefix : FILES_PREFIXES) {
			FilenameFilter filenameFilter = new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith(prefix);
				}
			};

			File[] fileList = inputFolder.listFiles(filenameFilter);
			if (fileList.length != 0) {
				Pattern pattern = Pattern.compile("[A-Za-z]*_[0-9-]*");
				Matcher matcher = pattern.matcher(fileList[0].getName());

				if (matcher.find()) {
					String outputFileName = matcher.group(0) + ".log";
					File outputFile = new File(outputFolder, outputFileName);

					try {
						outputFile.createNewFile();
					} catch (IOException e) {
						throw new FileSystemException("Error creating output file " + outputFileName);
					}

					if (outputFile.exists()) {
						try {
							for (File file : inputFolder.listFiles(filenameFilter)) {
								List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
								Files.write(outputFile.toPath(), lines, StandardCharsets.UTF_8,
										StandardOpenOption.CREATE, StandardOpenOption.APPEND);
							}
						} catch (IOException e) {
							throw new FileSystemException("Error writing data to file " + e.getMessage());
						}
					}
				}
			}
		}
	}

	public static void main(String[] args)
			throws FileAlreadyExistsException, FileNotFoundException, FileSystemException {
		System.out.printf("[%S] [INIT]%n", LogFilesMerger.class.getSimpleName());
		new LogFilesMerger(INPUT_FOLDER, OUTPUT_FOLDER);
		System.out.printf("[%S] [FINISHED]%n", LogFilesMerger.class.getSimpleName());
	}
}
