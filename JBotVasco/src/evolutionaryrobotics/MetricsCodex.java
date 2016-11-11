package evolutionaryrobotics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.Random;

public class MetricsCodex {
	// Metrics
	public static final String TIME_INSIDE_SPOT_MIN = "timeInside_min";
	public static final String TIME_INSIDE_SPOT_AVG = "timeInside_avg";
	public static final String TIME_INSIDE_SPOT_MAX = "timeInside_max";
	public static final String TIME_FIRST_TOTAL_OCCUP = "timeFirstTotalOccup";
	public static final String NUM_DIFF_SPOTS_OCCUP_MIN = "numberDiffSpotsOccupied_min";
	public static final String NUM_DIFF_SPOTS_OCCUP_AVG = "numberDiffSpotsOccupied_avg";
	public static final String NUM_DIFF_SPOTS_OCCUP_MAX = "numberDiffSpotsOccupied_max";
	public static final String TIME_REOCUPATION_MIN = "reocupationTime_min";
	public static final String TIME_REOCUPATION_AVG = "reocupationTime_avg";
	public static final String TIME_REOCUPATION_MAX = "reocupationTime_max";

	// Separators
	private static final String GENERATION_NUMBER = "generation";
	private static final String COMMENT_INITIATOR = "#";
	private static final String HEADER_LINE_INITIATOR = "$";

	public static String encodeMetricsData(MetricsData data) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);

		formatter.format("%d\t%8.3f\t%8.3f\t%8.3f\t%d\t%8.3f\t%8.3f\t%8.3f\t%8.3f\t%8.3f\t%8.3f", data.getGeneration(),
				data.getTimeInside_min(), data.getTimeInside_avg(), data.getTimeInside_max(),
				data.getTimeFirstTotalOccup(), data.getNumberDiffSpotsOccupied_min(),
				data.getNumberDiffSpotsOccupied_avg(), data.getNumberDiffSpotsOccupied_max(),
				data.getReocupationTime_min(), data.getReocupationTime_avg(), data.getReocupationTime_max());

		formatter.close();
		return sb.toString();
	}

	public static String getEncodedMetricsFileHeader() {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);

		formatter.format("%s\tMetrics written on %s%n", COMMENT_INITIATOR,
				new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		formatter.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", HEADER_LINE_INITIATOR, GENERATION_NUMBER,
				TIME_INSIDE_SPOT_MIN, TIME_INSIDE_SPOT_AVG, TIME_INSIDE_SPOT_MAX, TIME_FIRST_TOTAL_OCCUP,
				NUM_DIFF_SPOTS_OCCUP_MIN, NUM_DIFF_SPOTS_OCCUP_AVG, NUM_DIFF_SPOTS_OCCUP_MAX, TIME_REOCUPATION_MIN,
				TIME_REOCUPATION_AVG, TIME_REOCUPATION_MAX);

		formatter.close();
		return sb.toString();
	}

	public static MetricsData decodeMetricsData(String line) {
		if (!line.startsWith(COMMENT_INITIATOR) && !line.startsWith(HEADER_LINE_INITIATOR)) {
			String[] args = line.split("\t");

			if (args.length < 11) {
				return null;
			} else {
				for (String str : args) {
					str = str.replace("\t", "").replace("\n", "").replace(System.getProperty("line.separator"), "")
							.trim();
				}

				MetricsData data = new MetricsData(Integer.parseInt(args[0]));
				data.setTimeInside_min(Double.parseDouble(args[1]));
				data.setTimeInside_avg(Double.parseDouble(args[2]));
				data.setTimeInside_max(Double.parseDouble(args[3]));
				data.setTimeFirstTotalOccup(Integer.parseInt(args[4]));
				data.setNumberDiffSpotsOccupied_min(Double.parseDouble(args[5]));
				data.setNumberDiffSpotsOccupied_avg(Double.parseDouble(args[6]));
				data.setNumberDiffSpotsOccupied_max(Double.parseDouble(args[7]));
				data.setReocupationTime_min(Double.parseDouble(args[8]));
				data.setReocupationTime_avg(Double.parseDouble(args[9]));
				data.setReocupationTime_max(Double.parseDouble(args[10]));
				return data;
			}
		} else {
			return null;
		}
	}

	public static ArrayList<MetricsData> decodeMetricsDataFile(File file) {
		ArrayList<MetricsData> data = new ArrayList<MetricsData>();

		if (file.exists() && file.isFile()) {
			FileReader fileReader = null;
			BufferedReader buffReader = null;

			try {
				fileReader = new FileReader(file);
				buffReader = new BufferedReader(fileReader);

				String line = buffReader.readLine();
				while (line != null) {
					MetricsData md = decodeMetricsData(line);

					if (md != null) {
						data.add(md);
					}

					line = buffReader.readLine();
				}
			} catch (IOException e) {
				System.err.printf("[%s] %s%n", MetricsCodex.class.getSimpleName(), e.getMessage());
			} finally {
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						System.err.printf("[%s] %s%n", MetricsCodex.class.getSimpleName(), e.getMessage());
					}
				}

				if (buffReader != null) {
					try {
						buffReader.close();
					} catch (IOException e) {
						System.err.printf("[%s] %s%n", MetricsCodex.class.getSimpleName(), e.getMessage());
					}
				}
			}
		}

		return data;
	}

	public static void main(String[] args) {
		// unitTest();
		generateRandomMetricsFile(
				"C:\\Users\\BIOMACHINES\\Desktop\\Eclipse Data\\JBotEvolver\\JBotVasco\\experiments_automator");
	}

	private static void generateRandomMetricsFile(String path) {
		FileWriter fileWriter = null;
		BufferedWriter outputBuffWriter = null;

		try {
			File folder = new File(path);
			File file = null;

			if (folder.exists() && folder.isDirectory()) {
				file = new File(folder, "_metrics.log");

				if (file.exists() || !file.createNewFile()) {
					throw new IllegalAccessError("The file already exists");
				}
			}

			fileWriter = new FileWriter(file);
			outputBuffWriter = new BufferedWriter(fileWriter);

			outputBuffWriter.write(getEncodedMetricsFileHeader());
			outputBuffWriter.newLine();

			Random r = new Random();
			for (int i = 0; i < 100; i++) {
				MetricsData data = new MetricsData(i);
				data.setTimeInside_min(r.nextDouble() * 10);
				data.setTimeInside_avg(r.nextDouble() * 10);
				data.setTimeInside_max(r.nextDouble() * 10);
				data.setTimeFirstTotalOccup(r.nextInt(10));
				data.setNumberDiffSpotsOccupied_min(r.nextDouble() * 10);
				data.setNumberDiffSpotsOccupied_avg(r.nextDouble() * 10);
				data.setNumberDiffSpotsOccupied_max(r.nextDouble() * 10);
				data.setReocupationTime_min(r.nextDouble() * 10);
				data.setReocupationTime_avg(r.nextDouble() * 10);
				data.setReocupationTime_max(r.nextDouble() * 10);

				outputBuffWriter.write(encodeMetricsData(data));
				outputBuffWriter.newLine();
			}

			System.out.printf("[%s] Generated!%n", MetricsCodex.class.getSimpleName());

		} catch (Exception e) {
			System.err.printf("[%s] %s%n", MetricsCodex.class.getSimpleName(), e.getMessage());
		} finally {
			if (outputBuffWriter != null) {
				try {
					outputBuffWriter.close();
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", MetricsCodex.class.getSimpleName(), e.getMessage());
				}
			}

			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException e) {
					System.err.printf("[%s] %s%n", MetricsCodex.class.getSimpleName(), e.getMessage());
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private static void unitTest() {
		// Custom made Unit test :P
		System.out.println(MetricsCodex.getEncodedMetricsFileHeader());

		MetricsData data = new MetricsData(100);
		data.setTimeInside_min(1);
		data.setTimeInside_avg(2);
		data.setTimeInside_max(3);
		data.setTimeFirstTotalOccup(4);
		data.setNumberDiffSpotsOccupied_min(5);
		data.setNumberDiffSpotsOccupied_avg(6);
		data.setNumberDiffSpotsOccupied_max(7);
		data.setReocupationTime_min(8);
		data.setReocupationTime_avg(9);
		data.setReocupationTime_max(10);

		String encodedLine = MetricsCodex.encodeMetricsData(data);
		System.out.println(encodedLine);

		MetricsData decodedData = MetricsCodex.decodeMetricsData(encodedLine);
		if (data.equals(decodedData)) {
			System.out.println("Encoding & decoding working fine!");
		} else {
			System.out.println("Error on encoding/decoding!");
		}
	}
}
