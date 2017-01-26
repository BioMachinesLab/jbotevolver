package main.scripts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import javax.swing.JOptionPane;

import evolutionaryrobotics.JBotEvolver;
import simulation.util.Arguments;

public class WeightsFileGenerator {
	private static final String IN_FOLDER = "C:/Users/BIOMACHINES/Desktop/Eclipse Data/JBotVasco/experiments/rendition_DEBUG_NormalWithRandomPos";
	private static final String OUT_FOLDER = "exported_controllers_2";
	private static final int TOP_RUNS = 1;

	public static void main(String[] args) {

		File inputFolder = new File(IN_FOLDER);

		if (!inputFolder.isDirectory()) {
			System.out.println("Can't find folder " + IN_FOLDER);
			System.exit(0);
		}

		File outputFolder = new File(OUT_FOLDER);
		if (outputFolder.exists()) {
			if (JOptionPane.showConfirmDialog(null, "Output folder exists. Should continue?", "Output folder conflict",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
				new WeightsFileGenerator(inputFolder, outputFolder);
			} else {
				System.exit(0);
			}
		} else {
			outputFolder.mkdir();
			new WeightsFileGenerator(inputFolder, outputFolder);
		}

	}

	public WeightsFileGenerator(File inputFolder, File outputFolder) {
		for (File subFolder : inputFolder.listFiles()) {
			if (subFolder.isDirectory()) {
				System.out.printf("Exporting files on %s folder%n", subFolder.getName());

				try {
					// Organize experiments per fitness value
					ArrayList<RunData> data = getData(subFolder);
					System.out.println("Finished loading post evaluation information");

					if (data != null) {
						for (int i = 0; i < TOP_RUNS; i++) {
							// Get arguments for run
							RunData d = data.get(i);
							String genFile = subFolder.getParent() + "/" + d.run + "/show_best/showbest" + d.generation
									+ ".conf";
							JBotEvolver jbot = new JBotEvolver(
									new String[] { genFile, "--bestcontrollerweights", "enable" });
							String result = "type=(ControllerCIBehavior),";

							/*
							 * Sensors data
							 */
							Arguments sensors = new Arguments(
									jbot.getArguments().get("--robots").getArgumentAsString("sensors"));
							result += "sensors=(";

							String sensorArgsStr = "";
							int sensorNumber = 1;

							for (int argNumber = 0; argNumber < sensors.getNumberOfArguments(); argNumber++) {
								Arguments wrapper = new Arguments(
										sensors.getArgumentAsString(sensors.getArgumentAt(argNumber)));
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
							populationArgs.setArgument("parentfolder", subFolder.getPath() + "/" + d.run);
							populationArgs.setArgument("load", "population" + d.generation);
							jbot.getArguments().replace("--population", populationArgs);

							double[] weights = jbot.getPopulation().getBestChromosome().getAlleles();
							String weightsStr = convertDoubleArray(weights);
							controllerArgs.setArgument("weights", weightsStr);

							result += controllerArgs.getCompleteArgumentString() + ")";

							/*
							 * Produce final file
							 */
							result = Arguments.beautifyString(result);
							File outputFile = new File(outputFolder.getPath() + "/"
									+ new File(subFolder.getParent()).getName() + i + ".conf");

							if (!outputFile.exists() || (outputFile.exists() && JOptionPane.showConfirmDialog(null,
									"Output " + outputFile.getName() + " exists. Should continue?",
									"Output folder conflict", JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)) {
								BufferedWriter wr = new BufferedWriter(new FileWriter(outputFile));
								wr.write(result);
								wr.flush();
								wr.close();

								System.out.printf("> Exported file %s to %s%n",
										"/" + d.run + "/show_best/showbest" + d.generation + ".conf",
										outputFolder.getName() + "/" + new File(subFolder.getParent()).getName() + i);
							} else {
								System.out.printf("Skipping %s file%n",
										"/" + d.run + "/show_best/showbest" + d.generation + ".conf");
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ArrayList<RunData> getData(File subFolder) throws IOException {
		String post = subFolder.getParent() + "/post.txt";
		File postFile = new File(post);

		if (!postFile.exists()) {
			System.err.printf("File %s does not exist%n", post.replace("\\", "/"));
			return null;
		}

		Scanner scanner = new Scanner(postFile);
		scanner.nextLine();

		ArrayList<RunData> data = new ArrayList<RunData>();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if (!line.startsWith("O")) {
				Scanner lineScanner = new Scanner(line);
				RunData runData = new RunData();

				String s = lineScanner.next();
				runData.run = Integer.parseInt(s);

				s = lineScanner.next();
				runData.fitness = Double.parseDouble(s);

				String[] split = line.trim().split(" ");
				int generation = Integer.parseInt(split[split.length - 1]);
				runData.generation = generation;

				data.add(runData);
				lineScanner.close();
			}
		}

		scanner.close();
		Collections.sort(data);

		return data;
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

	public class RunData implements Comparable<RunData> {
		int run;
		double fitness;
		int generation;

		@Override
		public int compareTo(RunData o) {
			return (int) ((o.fitness - fitness) * 100000.0);
		}
	}
}
