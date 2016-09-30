package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import evolutionaryrobotics.JBotEvolver;
import simulation.util.Arguments;

public class WeightsFileGenerator_SingleFile {
	private static final String IN_FOLDER = "C:/Users/BIOMACHINES/Desktop/Eclipse Data/JBotVasco/experiments/rendition_DEBUG_newSensor_orientation_backup/1";
	private static final int GENERATION = 599;
	private static final String OUT_FOLDER = "exported_controllers_final";

	public static void main(String[] args) throws Exception {

		File inputFolder = new File(IN_FOLDER);

		if (!inputFolder.isDirectory()) {
			System.out.println("Can't find folder " + IN_FOLDER);
			System.exit(0);
		}

		File outputFolder = new File(OUT_FOLDER);
		if (outputFolder.exists()) {
			if (JOptionPane.showConfirmDialog(null, "Output folder exists. Should continue?", "Output folder conflict",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				new WeightsFileGenerator_SingleFile(inputFolder, outputFolder);
			} else {
				System.exit(0);
			}
		} else {
			outputFolder.mkdir();
			new WeightsFileGenerator_SingleFile(inputFolder, outputFolder);
		}
	}

	public WeightsFileGenerator_SingleFile(File inputFolder, File outputFolder) throws Exception {
		if (inputFolder.isDirectory()) {
			System.out.printf("Exporting files on %s folder%n", inputFolder.getName());

			try {
				// Get arguments for run
				String genFile = inputFolder.getAbsolutePath() + "/show_best/showbest" + GENERATION + ".conf";
				JBotEvolver jbot = new JBotEvolver(new String[] { genFile, "--bestcontrollerweights", "enable" });
				String result = "type=(ControllerCIBehavior),";

				/*
				 * Sensors data
				 */
				Arguments sensors = new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("sensors"));
				result += "sensors=(";

				String sensorArgsStr = "";
				int sensorNumber = 1;

				for (int argNumber = 0; argNumber < sensors.getNumberOfArguments(); argNumber++) {
					Arguments wrapper = new Arguments(sensors.getArgumentAsString(sensors.getArgumentAt(argNumber)));
					String ci = wrapper.getArgumentAsString("ci");
					sensorArgsStr += "Sensor" + (sensorNumber++) + "=(" + ci + "),";
				}

				// Add sensors configuration
				Arguments sensorArgs = new Arguments(sensorArgsStr);
				result += sensorArgs.getCompleteArgumentString();

				/*
				 * Network data
				 */
				result += "),network=(";
				Arguments controllerArgs = jbot.getArguments().get("--controllers");
				controllerArgs = new Arguments(controllerArgs.getArgumentAsString("network"));

				// Get newtork weights
				Arguments populationArgs = jbot.getArguments().get("--population");
				populationArgs.setArgument("parentfolder", inputFolder.getPath());
				populationArgs.setArgument("load", "population" + GENERATION);
				jbot.getArguments().replace("--population", populationArgs);

				double[] weights = jbot.getPopulation().getBestChromosome().getAlleles();
				String weightsStr = convertDoubleArray(weights);
				controllerArgs.setArgument("weights", weightsStr);

				result += controllerArgs.getCompleteArgumentString() + ")";

				/*
				 * Produce final file
				 */
				result = Arguments.beautifyString(result);
				File outputFile = new File(outputFolder.getPath() + "/" + inputFolder.getName() + ".conf");

				if (!outputFile.exists() || (outputFile.exists() && JOptionPane.showConfirmDialog(null,
						"Output " + outputFile.getName() + " exists. Should continue?", "Output folder conflict",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)) {
					BufferedWriter wr = new BufferedWriter(new FileWriter(outputFile));
					wr.write(result);
					wr.flush();
					wr.close();

					System.out.printf("> Exported file %s.conf to %s.conf%n", "/show_best/showbest" + GENERATION,
							outputFolder.getName() + "/" + inputFolder.getName());
				} else {
					System.out.printf("Skipping %s file%n", "/show_best/showbest" + GENERATION + ".conf");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String convertDoubleArray(double[] vals) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < vals.length; i++) {
			sb.append(vals[i]);
			if (i != vals.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
}
