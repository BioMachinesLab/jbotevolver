package main;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;

public class FitnessProcessor {
	private static final String path = "C:\\Users\\BIOMACHINES\\Desktop\\Eclipse Data\\JBotEvolver\\JBotVasco\\experiments_automator\\targetFollowing_automator\\targetFollowing_basicSetup_normal_correctedErrors";
	private int runs = 10;
	private static String showBestFolder = "show_best";
	private static String showBestFileName = "showbest%d.conf";
	private final String PLOTS_DATA_FOLDER_PATH = ".\\data";
	private final String SAVE_TO_DATA_EXTENSION = "csv";

	private int generationNumber = 0;

	public static void main(String[] args) {
		new FitnessProcessor();
	}

	public FitnessProcessor() {
		HashMap<String, double[]> fitnessData = new HashMap<String, double[]>();
		for (int i = 1; i <= runs; i++) {
			File folder = new File(path, Integer.toString(i));

			if (folder.exists() && folder.isDirectory()) {
				fitnessData.put(folder.getParentFile().getName() + File.separatorChar + folder.getName(),
						getFitness(folder));
			}
		}

		if (saveDataToFile(fitnessData, new File(path).getName())) {
			System.out.println("Data saved with success!");
		} else {
			System.out.println("Error saving data!");
		}
	}

	private double[] getFitness(File folder) {
		generationNumber = getGenerationNumberFromFile(new File(folder, "_generationnumber"));
		double[] result = new double[generationNumber];

		for (int i = 0; i < generationNumber; i++) {
			try {
				JBotEvolver jBot = new JBotEvolver(null);

				String filename = String.format(showBestFileName, i);
				File file = new File(new File(folder, showBestFolder), filename);

				if (file.exists()) {
					String populationFile = new File(folder, "populations\\population" + i).getPath();
					jBot.loadFile(file.getAbsolutePath(), populationFile);
					jBot.getArguments().get("--population").setArgument("load", populationFile);

					EvaluationFunction evaluationFuntion = jBot.getEvaluationFunction()[0];
					Simulator simulator = jBot.createSimulator();

					jBot.setupBestIndividual(simulator);
					simulator.setupEnvironment();
					simulator.addCallback(evaluationFuntion);
					simulator.simulate();
					result[i] = evaluationFuntion.getFitness();
					System.out.printf("Folder: %s\tGeneration: %d\tFitness: %s%n", folder.getName(), i,
							String.format("%.8f", result[i]));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	protected int getGenerationNumberFromFile(File file) {
		Scanner s = null;
		try {
			s = new Scanner(file);

			return Integer.valueOf(s.next()) + 1;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (s != null)
				s.close();
		}
		return 0;
	}

	private boolean saveDataToFile(HashMap<String, double[]> fitnessData, String fileName) {
		try {
			/*
			 * Get the name to use on file to create
			 */
			String name = new File(path).getName() + "_fitness." + SAVE_TO_DATA_EXTENSION;
			if (!new File(PLOTS_DATA_FOLDER_PATH).exists()) {
				new File(PLOTS_DATA_FOLDER_PATH).mkdir();
			}

			/*
			 * Create a bi-dimensional array with all values to be write to file
			 */
			double[][] data = new double[generationNumber][runs + 1];

			// Add the generation number
			for (int line = 0; line < generationNumber; line++) {
				data[line] = new double[runs + 1];
				data[line][0] = line;
			}

			// Collect all data in the array
			for (int column = 1; column <= runs; column++) {
				double[] array = fitnessData
						.get(new File(path).getName() + File.separatorChar + Integer.toString(column));
				for (int line = 0; line < array.length; line++) {
					data[line][column] = array[line];
				}
			}

			/*
			 * Write everything to a file
			 */
			RequestDataParamsPane panel = new RequestDataParamsPane();
			if (panel.triggerDialog() == JOptionPane.OK_OPTION) {
				PrintWriter pw = new PrintWriter(new FileOutputStream(new File(PLOTS_DATA_FOLDER_PATH, name)));

				if (panel.addColumnsIdentifiers()) {
					pw.print("generation" + panel.getArgumentsSeparator());

					for (int i = 0; i < runs; i++) {
						pw.print(i);

						if (i < runs - 1) {
							pw.print(panel.getArgumentsSeparator());
						}
					}
					pw.println();
				}

				for (int line = 0; line < data.length; line++) {
					for (int column = 0; column < data[line].length; column++) {
						if (column == 0) {
							if (panel.addGenerationNumber()) {
								pw.print((int) data[line][column]);

								if (column < data[line].length - 1) {
									pw.print(panel.getArgumentsSeparator());
								}
							}
						} else {
							pw.print(String.format("%.8f", data[line][column]));

							if (column < data[line].length - 1) {
								pw.print(panel.getArgumentsSeparator());
							}
						}
					}

					if (line < data.length - 1) {
						pw.println();
					}
				}

				pw.close();
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private class RequestDataParamsPane {
		private JCheckBox addColumnsIdentifiers = null;
		private JCheckBox addGenerationNumber = null;
		private JRadioButton argumentsSeparator_tab = null;
		private JRadioButton argumentsSeparator_comma = null;
		private JRadioButton argumentsSeparator_semicolon = null;
		private ButtonGroup argumentsSeparatorGroup = null;

		private int dialogResult = 0;

		public int triggerDialog() {
			addColumnsIdentifiers = new JCheckBox("Columns identifiers");
			addGenerationNumber = new JCheckBox("Generation number");
			addColumnsIdentifiers.setSelected(false);
			addGenerationNumber.setSelected(false);

			argumentsSeparator_tab = new JRadioButton("Tab (\\t)");
			argumentsSeparator_comma = new JRadioButton("Comma (,)");
			argumentsSeparator_semicolon = new JRadioButton("Semicolon (;)");

			argumentsSeparatorGroup = new ButtonGroup();
			argumentsSeparatorGroup.add(argumentsSeparator_tab);
			argumentsSeparatorGroup.add(argumentsSeparator_comma);
			argumentsSeparatorGroup.add(argumentsSeparator_semicolon);

			argumentsSeparator_tab.setSelected(true);

			JPanel radioButtonsPanel = new JPanel(new GridLayout(4, 1));
			radioButtonsPanel.add(new JLabel("Select data separator"));
			radioButtonsPanel.add(argumentsSeparator_tab);
			radioButtonsPanel.add(argumentsSeparator_comma);
			radioButtonsPanel.add(argumentsSeparator_semicolon);

			String message = "Select options:";
			Object[] params = { message, addColumnsIdentifiers, addGenerationNumber, radioButtonsPanel };
			dialogResult = JOptionPane.showConfirmDialog(null, params, "Plot parameters", JOptionPane.YES_NO_OPTION);
			return dialogResult;
		}

		public boolean addColumnsIdentifiers() {
			return addColumnsIdentifiers != null && addColumnsIdentifiers.isSelected();
		}

		public boolean addGenerationNumber() {
			return addGenerationNumber != null && addGenerationNumber.isSelected();
		}

		public char getArgumentsSeparator() {
			for (Enumeration<AbstractButton> buttons = argumentsSeparatorGroup.getElements(); buttons
					.hasMoreElements();) {
				AbstractButton button = buttons.nextElement();

				if (button.isSelected()) {
					String text = button.getText();
					if (text.equals(argumentsSeparator_tab.getText())) {
						return '\t';
					} else if (text.equals(argumentsSeparator_comma.getText())) {
						return ',';
					} else if (text.equals(argumentsSeparator_semicolon.getText())) {
						return ';';
					} else {
						return '\0';
					}
				}
			}

			// return null object
			return '\0';
		}
	}
}
