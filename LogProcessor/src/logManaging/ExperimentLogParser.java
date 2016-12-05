package logManaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.joda.time.DateTime;

import commoninterface.dataobjects.GPSData;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.ToLogData;
import fieldtests.data.DroneLogExporter;
import fieldtests.data.Experiment;
import fitnesstools.AssessFitness;

public class ExperimentLogParser {
	private final static String RAW_LOGS_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\logs";
	private final static String MERGED_LOGS_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";
	private int[] robots = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	private ArrayList<Experiment> experiments = new ArrayList<Experiment>();
	private HashMap<Integer, ArrayList<ToLogData>> completeLogs = new HashMap<Integer, ArrayList<ToLogData>>();

	private int nSamples = 10;

	public static void main(String[] args) throws Exception {
		new ExperimentLogParser();
	}

	public ExperimentLogParser() throws Exception {
		new LogFilesMerger(RAW_LOGS_FOLDER, MERGED_LOGS_FOLDER);
		HashMap<Integer, ArrayList<GPSData>> gpsData = new GPSLogFilesParser(MERGED_LOGS_FOLDER).getGPSData();
		HashMap<Integer, ArrayList<DecodedLog>> decodedLogData = new ValuesLogFilesParser(MERGED_LOGS_FOLDER)
				.getDecodedLogData();

		exp = getExperiment(s);
		new File("experiments/" + FOLDERS_TO_PROCESS[folder]).mkdir();

		FileOutputStream fout = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(exp);
		oos.close();
		fout.close();

		System.out.println("ASSESSING FITNESS");
		System.out.println();
		int j = 0;
		for (Experiment e : experiments) {
			double fitnessReal = AssessFitness.getRealFitness(e, 1);
			double fitnessSim = 0;
			for (int i = 1; i <= nSamples; i++) {
				double f = AssessFitness.getSimulatedFitness(e, i, false);
				fitnessSim += f;
				// System.out.print(f+" ");
			}
			// System.out.println();
			fitnessSim /= nSamples;
			System.out.println((j++) + "\t" + fitnessReal + "\t" + fitnessSim + "\t" + e);
			// System.out.println();
		}

	}

	public Experiment getsExperiment(String description) throws IOException {

		String[] split = description.split(";");

		String controller = split[0];
		int controllerNumber = Integer.parseInt(split[1]);
		int sample = Integer.parseInt(split[2]);
		int nRobots = Integer.parseInt(split[3]);
		String experimentName = split[4];
		int duration = Integer.parseInt(split[5]);

		DateTime startTime = null;

		ArrayList<Integer> participatingRobots = new ArrayList<Integer>();

		for (int i : robots) {
			DateTime timeFound = DroneLogExporter.getStartTime(completeLogs.get(i), experimentName);
			if (timeFound != null) {

				if (startTime != null && startTime.isBefore(timeFound))
					timeFound = startTime;

				startTime = timeFound;
				participatingRobots.add(i);
			}
		}

		if (split.length >= 7) {
			System.out.println("Manually defining the robots: " + split[6]);
			participatingRobots.clear();
			String[] splitRobots = split[6].split(",");
			for (String id : splitRobots)
				participatingRobots.add(Integer.parseInt(id));
		}

		if (startTime == null) {
			System.out.println("Can't find start time for experiment " + description);
			System.exit(0);
		}

		DateTime endTime = startTime.plus(duration * 1000);

		if (nRobots != participatingRobots.size()) {
			System.out.print("Missing logs for some of the robots " + description + " [");
			for (int i : participatingRobots) {
				System.out.print(i + ",");
			}
			System.out.println("]");
		}

		ArrayList<ToLogData> allData = new ArrayList<ToLogData>();

		for (int i : participatingRobots) {
			allData.addAll(DroneLogExporter.getLogs(completeLogs.get(i), startTime, endTime));
		}

		Collections.sort(allData);

		Experiment experiment = new Experiment();

		if (split.length == 8) {
			experiment.activeRobot = Integer.parseInt(split[7]);
		}

		experiment.setRobots(participatingRobots);
		experiment.setTimeSteps(duration * 10);
		experiment.setExperimentStart(startTime);
		experiment.setExperimentEnd(endTime);
		experiment.setControllerNumber(controllerNumber);
		experiment.setSample(sample);
		experiment.setControllerName(controller);
		experiment.setLogs(allData);

		return experiment;
	}

}
