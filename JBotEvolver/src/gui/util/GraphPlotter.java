package gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.CTRNNMultilayer;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;

public class GraphPlotter extends JFrame implements Updatable {
	private static final long serialVersionUID = -3465748965026180370L;
	protected final String PLOTS_IMAGES_FOLDER_PATH = ".\\plots";
	protected final String SAVE_TO_IMAGE_EXTENSION = "png";
	protected final String PLOTS_DATA_FOLDER_PATH = ".\\data";
	protected final String SAVE_TO_DATA_EXTENSION = "csv";

	protected LinkedList<JCheckBox> robotCheckboxes = new LinkedList<JCheckBox>();
	protected LinkedList<JCheckBox> inputCheckboxes = new LinkedList<JCheckBox>();
	protected LinkedList<JCheckBox> hiddenCheckboxes = new LinkedList<JCheckBox>();
	protected LinkedList<JCheckBox> outputCheckboxes = new LinkedList<JCheckBox>();

	protected LinkedList<JTextField> inputTextFields = new LinkedList<JTextField>();
	protected LinkedList<JTextField> hiddenTextFields = new LinkedList<JTextField>();
	protected LinkedList<JTextField> outputTextFields = new LinkedList<JTextField>();

	protected JEditorPane console = new JEditorPane();

	protected ArrayList<Robot> robots;
	protected Vector<NNInput> inputs;
	protected double[] hidden;
	protected Vector<NNOutput> outputs;

	protected ArrayList<double[][]> valuesList = new ArrayList<double[][]>();
	protected ArrayList<String> titlesList = new ArrayList<String>();

	protected double currentStep = 0;
	protected int currentIndex = 0;

	protected Simulator simulator;
	protected NeuralNetwork network;

	protected JBotEvolver jBotEvolver;

	public GraphPlotter() {
		super("Graph Plotter");
	}

	/**
	 * Currently this only works for robots that use a NeuralNetwork as a
	 * Controller. This constructor creates a JFrame that allows you to choose
	 * Which neural network input/output activations to plot
	 * 
	 * @param simulator
	 *            The simulator should be initialized, not in the middle of a
	 *            Simulation.
	 */
	public GraphPlotter(JBotEvolver jBotEvolver, Simulator simulator) {
		super("Graph Plotter");
		try {
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

			this.simulator = simulator;
			this.jBotEvolver = jBotEvolver;
			robots = simulator.getRobots();

			// TODO this doesn't work for BehaviorControllers. Maybe a
			// BehaviorController should extends
			// NeuralNetworkController. Also, the name should be different. Like
			// HierarchicalController...
			NeuralNetworkController controller = (NeuralNetworkController) robots.get(0).getController();
			network = controller.getNeuralNetwork();
			inputs = network.getInputs();
			outputs = network.getOutputs();

			mainPanel.add(initRobotPanel());
			mainPanel.add(initInputsPanel());

			JPanel hiddenPanel = initHiddenPanel();
			if (hiddenPanel != null)
				mainPanel.add(hiddenPanel);

			mainPanel.add(initOutputsPanel());

			JPanel buttonsPanel = new JPanel(new GridLayout(1, 4));

			JButton checkAllButton = new JButton("Check all");
			buttonsPanel.add(checkAllButton);

			JButton uncheckAllButton = new JButton("Uncheck all");
			buttonsPanel.add(uncheckAllButton);

			JButton saveToFileButton = new JButton("Save to file");
			buttonsPanel.add(saveToFileButton);

			JButton plotButton = new JButton("Plot!");
			buttonsPanel.add(plotButton);

			checkAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					changeAllCheckboxes(true);
				}
			});

			uncheckAllButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					changeAllCheckboxes(false);
				}
			});

			plotButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					plotGraph();
				}
			});

			// saveToFileButton.addActionListener(new ActionListener() {
			// @Override
			// public void actionPerformed(ActionEvent arg0) {
			// saveGraphDataToFile();
			// }
			// });

			mainPanel.add(buttonsPanel);
			mainPanel.add(new JScrollPane(console));

			add(mainPanel);

			pack();
			setLocationRelativeTo(null);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This constructor receives a list of fitness.log files
	 * 
	 * @param files
	 *            list of file names of the fitness.log files to plot
	 */
	public GraphPlotter(final String[] files) {
		JPanel mainPanel = new JPanel(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		final Graph graph = new Graph();
		mainPanel.add(graph);

		int totalGenerations = 0;

		for (String file : files) {
			try {
				File fitnessFile = new File(file);
				File generationsFile = new File(fitnessFile.getParent() + File.separatorChar + "_generationnumber");

				Scanner sc = new Scanner(generationsFile);

				totalGenerations = Math.max(sc.nextInt() + 1, totalGenerations);

				sc.close();

				sc = new Scanner(fitnessFile);
				Double[] dataList = new Double[totalGenerations];

				while (sc.hasNextLine()) {
					String line = sc.nextLine();
					if (line.charAt(0) == '#' || (line.charAt(0) == '\t' && line.contains("#")))
						continue;
					else {
						line = line.replaceAll(" ", "");
						line = line.replaceAll("\t\t", "\t");
						String[] lineValues = line.trim().split("\t");
						lineValues = removeBlankSpaceOnArray(lineValues);
						int generation = Integer.valueOf(lineValues[0]);
						if (generation < totalGenerations) {
							Double value = Double.valueOf(lineValues[1]);
							dataList[generation] = value;
						}
					}

				}
				sc.close();

				graph.addDataList(dataList);

				if (System.getProperty("os.name").contains("Windows")) {
					String name = fitnessFile.getParentFile().getParentFile().getName();
					name += File.separatorChar + fitnessFile.getParentFile().getName();
					name += File.separatorChar + fitnessFile.getName();
					graph.addLegend(name);
				} else {
					graph.addLegend(fitnessFile.getAbsolutePath());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		graph.setxLabel("Generations (" + (totalGenerations) + ")");
		graph.setyLabel("Fitness");
		graph.setShowLast(totalGenerations);

		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		JButton saveToImageButton = new JButton("Save graph as image");
		saveToImageButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (saveGraphAsImage(graph, files)) {
					JOptionPane.showMessageDialog(null, "Saved image!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Error saving image!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonsPanel.add(saveToImageButton);

		JButton saveToFileButton = new JButton("Save graph data");
		saveToFileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (saveGraphDataToFile(graph, files)) {
					JOptionPane.showMessageDialog(null, "Saved data!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Error saving data!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonsPanel.add(saveToFileButton);

		mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

		setSize(800, 500 + graph.getHeaderSize());
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	protected String[] removeBlankSpaceOnArray(String[] lineValues) {
		ArrayList<String> list = new ArrayList<String>();

		for (int i = 0; i < lineValues.length; i++) {
			if (!lineValues[i].equals(""))
				list.add(lineValues[i]);
		}

		String[] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			result[i] = list.get(i);
		}

		return result;
	}

	protected void changeAllCheckboxes(boolean value) {

		for (JCheckBox c : robotCheckboxes)
			c.setSelected(value);

		for (JCheckBox c : inputCheckboxes)
			c.setSelected(value);

		for (JCheckBox c : hiddenCheckboxes)
			c.setSelected(value);

		for (JCheckBox c : outputCheckboxes)
			c.setSelected(value);
	}

	protected void plotGraph() {

		valuesList = new ArrayList<double[][]>();
		titlesList = new ArrayList<String>();

		simulator = jBotEvolver.createSimulator();
		simulator.addCallback(jBotEvolver.getEvaluationFunction()[0]);
		jBotEvolver.setupBestIndividual(simulator);

		simulator.addCallback(this);

		try {
			// Instantiate the needed arrays
			for (int i = 0; i < robotCheckboxes.size(); i++) {
				if (robotCheckboxes.get(i).isSelected()) {

					for (int j = 0; j < inputCheckboxes.size(); j++) {
						if (inputCheckboxes.get(j).isSelected()) {

							String[] chosenInputs = inputTextFields.get(j).getText().trim().split(",");
							int[] chosenInts = new int[chosenInputs.length];

							for (int z = 0; z < chosenInputs.length; z++)
								chosenInts[z] = Integer.parseInt(chosenInputs[z]);
							Arrays.sort(chosenInts);

							for (int z = 0; z < inputs.get(j).getNumberOfInputValues(); z++) {
								if (Arrays.binarySearch(chosenInts, z) >= 0) {
									titlesList.add(inputs.get(j).getSensor().getClass().getSimpleName() + " " + z);
									valuesList.add(new double[simulator.getEnvironment().getSteps()][2]);
								}
							}
						}
					}
					if (!hiddenCheckboxes.isEmpty() && hiddenCheckboxes.get(0).isSelected()) {

						String[] chosenHidden = hiddenTextFields.get(0).getText().trim().split(",");
						int[] chosenInts = new int[chosenHidden.length];

						for (int z = 0; z < chosenHidden.length; z++)
							chosenInts[z] = Integer.parseInt(chosenHidden[z]);
						Arrays.sort(chosenInts);

						for (int z = 0; z < hidden.length; z++) {
							if (Arrays.binarySearch(chosenInts, z) >= 0) {
								titlesList.add("Hidden State " + z);
								valuesList.add(new double[simulator.getEnvironment().getSteps()][2]);
							}
						}
					}
					for (int j = 0; j < outputCheckboxes.size(); j++) {
						if (outputCheckboxes.get(j).isSelected()) {

							String[] chosenOutputs = outputTextFields.get(j).getText().trim().split(",");
							int[] chosenInts = new int[chosenOutputs.length];

							for (int z = 0; z < chosenOutputs.length; z++)
								chosenInts[z] = Integer.parseInt(chosenOutputs[z]);
							Arrays.sort(chosenInts);

							for (int z = 0; z < outputs.get(j).getNumberOfOutputValues(); z++) {
								if (Arrays.binarySearch(chosenInts, z) >= 0) {
									titlesList.add(outputs.get(j).getClass().getSimpleName() + " " + z);
									valuesList.add(new double[simulator.getEnvironment().getSteps()][2]);
								}
							}
						}
					}
				}
			}

			Thread worker = new Thread(new GraphSimulationRunner(simulator));
			worker.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Simulator simulator) {

		currentStep = simulator.getTime();

		robots = simulator.getRobots();

		NeuralNetworkController controller = (NeuralNetworkController) robots.get(0).getController();
		NeuralNetwork network = controller.getNeuralNetwork();
		inputs = network.getInputs();
		outputs = network.getOutputs();

		int[] numberOfInputNeurons = new int[inputs.size()];
		for (int j = 0; j < inputs.size(); j++)
			numberOfInputNeurons[j] = inputs.get(j).getNumberOfInputValues();

		int[] numberOfHiddenInputs = new int[hidden != null && hidden.length > 0 ? hidden.length : 1];

		try {
			CTRNNMultilayer multilayer = (CTRNNMultilayer) network;
			hidden = multilayer.getHiddenStates();
			for (int j = 0; j < hidden.length; j++)
				numberOfHiddenInputs[j] = hidden.length;
		} catch (Exception e) {
		}

		int[] numberOfOutputNeurons = new int[outputs.size()];
		for (int j = 0; j < outputs.size(); j++)
			numberOfOutputNeurons[j] = outputs.get(j).getNumberOfOutputValues();

		for (int i = 0; i < robotCheckboxes.size(); i++) {
			if (robotCheckboxes.get(i).isSelected()) {

				currentIndex = 0;

				controller = (NeuralNetworkController) robots.get(i).getController();
				network = controller.getNeuralNetwork();

				inputs = network.getInputs();
				outputs = network.getOutputs();

				processLayer(inputCheckboxes, inputTextFields, network.getInputNeuronStates(), numberOfInputNeurons);
				processLayer(outputCheckboxes, outputTextFields, network.getOutputNeuronStates(),
						numberOfOutputNeurons);
				processLayer(hiddenCheckboxes, hiddenTextFields, hidden, numberOfHiddenInputs);
			}
		}
	}

	protected void processLayer(LinkedList<JCheckBox> checkboxes, LinkedList<JTextField> textFields, double[] states,
			int[] numberOfNeurons) {

		int currentNeuron = 0;

		for (int j = 0; j < checkboxes.size(); j++) {

			if (checkboxes.get(j).isSelected()) {

				String[] chosenNeurons = textFields.get(j).getText().trim().split(",");
				int[] chosenInts = new int[chosenNeurons.length];

				try {
					for (int z = 0; z < chosenNeurons.length; z++)
						chosenInts[z] = Integer.parseInt(chosenNeurons[z]);
					Arrays.sort(chosenInts);

					for (int z = 0; z < numberOfNeurons[j]; z++, currentNeuron++) {
						if (Arrays.binarySearch(chosenInts, z) >= 0) {
							valuesList.get(currentIndex)[(int) currentStep][0] = currentStep;
							valuesList.get(currentIndex++)[(int) currentStep][1] = states[currentNeuron];
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			} else
				currentNeuron += numberOfNeurons[j];
		}
	}

	protected JPanel initRobotPanel() {
		JPanel robotPanel = new JPanel();
		int number = (int) Math.ceil(simulator.getRobots().size() / 2);
		robotPanel.setLayout(new GridLayout(number, 2));
		robotPanel.setBorder(BorderFactory.createTitledBorder("Robots"));

		for (int i = 0; i < robots.size(); i++) {
			JCheckBox checkbox = new JCheckBox("Robot " + i);
			checkbox.setSelected(true);
			robotCheckboxes.add(checkbox);
			robotPanel.add(checkbox);
		}
		return robotPanel;
	}

	protected JPanel initInputsPanel() {
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(inputs.size(), 2));
		inputPanel.setBorder(BorderFactory.createTitledBorder("Inputs"));

		for (int i = 0; i < inputs.size(); i++) {
			NNInput input = inputs.get(i);

			String name = input.getSensor().getClass().getSimpleName() + " (" + input.getNumberOfInputValues() + ")";
			JCheckBox checkbox = new JCheckBox(name);
			checkbox.setSelected(true);

			String text = "";
			for (int j = 0; j < input.getNumberOfInputValues(); j++)
				text += j == input.getNumberOfInputValues() - 1 ? j : j + ",";

			inputCheckboxes.add(checkbox);
			inputTextFields.add(new JTextField(text));

			inputPanel.add(checkbox);
			inputPanel.add(inputTextFields.peekLast());
		}
		return inputPanel;
	}

	protected void printHiddenTaus() {

		String text = "";

		for (int i = 0; i < robotCheckboxes.size(); i++) {
			if (robotCheckboxes.get(i).isSelected()) {
				text += "Hidden Taus for Robot #" + i + "\n";
				Robot r = robots.get(i);
				CTRNNMultilayer network = (CTRNNMultilayer) ((NeuralNetworkController) r.getController())
						.getNeuralNetwork();
				double[] hiddenTaus = network.getHiddenTaus();

				for (int j = 0; j < hiddenTaus.length; j++) {
					text += hiddenTaus[j];
					text += "\n";
				}
				text += "\n";
			}
		}
		new ConsoleFrame(text);
	}

	protected JPanel initHiddenPanel() {
		try {
			JPanel hiddenPanel = new JPanel();
			hiddenPanel.setLayout(new GridLayout(2, 2));
			hiddenPanel.setBorder(BorderFactory.createTitledBorder("Hidden"));

			CTRNNMultilayer multilayer = (CTRNNMultilayer) network;

			hidden = multilayer.getHiddenStates();

			JCheckBox checkbox = new JCheckBox("Hidden States");
			checkbox.setSelected(true);

			String text = "";

			for (int i = 0; i < hidden.length; i++)
				text += i == hidden.length - 1 ? i : i + ",";

			hiddenCheckboxes.add(checkbox);
			hiddenTextFields.add(new JTextField(text));

			JButton hiddenTausButton = new JButton("Print Hidden Taus");
			hiddenTausButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					printHiddenTaus();
				}
			});

			hiddenPanel.add(checkbox);
			hiddenPanel.add(hiddenTextFields.peekLast());

			hiddenPanel.add(hiddenTausButton);

			return hiddenPanel;

		} catch (Exception e) {
		}

		return null;
	}

	protected JPanel initOutputsPanel() {
		JPanel outputPanel = new JPanel();
		outputPanel.setLayout(new GridLayout(outputs.size(), 2));
		outputPanel.setBorder(BorderFactory.createTitledBorder("Outputs"));

		for (int i = 0; i < outputs.size(); i++) {
			NNOutput a = outputs.get(i);
			String name = a.getClass().getSimpleName() + " (" + a.getNumberOfOutputValues() + ")";
			JCheckBox checkbox = new JCheckBox(name);
			checkbox.setSelected(true);

			String text = "";
			for (int j = 0; j < outputs.get(i).getNumberOfOutputValues(); j++)
				text += j == outputs.get(i).getNumberOfOutputValues() - 1 ? j : j + ",";

			outputCheckboxes.add(checkbox);
			outputTextFields.add(new JTextField(text));

			outputPanel.add(checkbox);
			outputPanel.add(outputTextFields.peekLast());
		}
		return outputPanel;
	}

	protected class ConsoleFrame extends JFrame {
		private static final long serialVersionUID = -4643824615294958785L;

		public ConsoleFrame(String s) {
			JTextArea a = new JTextArea(s);
			a.setMargin(new Insets(5, 5, 5, 5));
			add(a);
			pack();
			setVisible(true);
		}
	}

	public class GraphSimulationRunner implements Runnable {
		protected Simulator sim;

		public GraphSimulationRunner(Simulator sim) {
			this.sim = sim;
		}

		@Override
		public void run() {
			sim.simulate();

			for (int i = 0; i < valuesList.size(); i++) {
				double[][] currentValues = valuesList.get(i);
				double[][] newValues = new double[(int) currentStep + 1][2];
				for (int j = 0; j < currentStep + 1; j++) {
					newValues[j][0] = currentValues[j][0];
					newValues[j][1] = currentValues[j][1];
				}
				valuesList.set(i, newValues);
			}

			JFrame window = new JFrame();
			JPanel graphPanel = new JPanel(new BorderLayout());
			window.getContentPane().add(graphPanel);
			Graph graph = new Graph();
			graphPanel.add(graph);

			int dataSize = 0;
			for (int i = 0; i < valuesList.size(); i++) {
				double[][] aux = valuesList.get(i);
				Double[] data = new Double[aux.length];
				for (int x = 0; x < aux.length; x++) {
					data[x] = (aux[x][1]);
				}

				if (data.length > dataSize)
					dataSize = data.length;

				graph.addDataList(data);
			}

			for (String s : titlesList)
				graph.addLegend(s);

			graph.setxLabel("Timesteps (" + dataSize + ")");
			graph.setyLabel("Activations");
			graph.setShowLast(dataSize);

			window.setSize(800, 500 + graph.getHeaderSize());
			window.setLocationRelativeTo(null);
			window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			window.setVisible(true);
		}
	}

	protected boolean saveGraphAsImage(Graph graph, String[] files) {
		try {
			if (files != null && files.length > 0) {
				Rectangle originalBounds = getBounds();
				Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
				setBounds(0, 0, screen.width, screen.height);
				validate();
				repaint();
				graph.enableMouseLines(false);

				BufferedImage img = new BufferedImage(graph.getWidth(), graph.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = img.createGraphics();
				graph.printAll(g2d);
				g2d.dispose();

				graph.enableMouseLines(true);
				setBounds(originalBounds);

				File f = new File(files[0]);
				String name = "";
				if (files.length == 1) {
					name = f.getParentFile().getParentFile().getName() + "_" + f.getParentFile().getName() + "_fitness."
							+ SAVE_TO_IMAGE_EXTENSION;
				} else {
					name = f.getParentFile().getParentFile().getName() + "_fitness." + SAVE_TO_IMAGE_EXTENSION;
				}

				if (!new File(PLOTS_IMAGES_FOLDER_PATH).exists()) {
					new File(PLOTS_IMAGES_FOLDER_PATH).mkdir();
				}

				ImageIO.write(img, SAVE_TO_IMAGE_EXTENSION, new File(PLOTS_IMAGES_FOLDER_PATH, name));
			}
		} catch (IOException e) {
			System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
			return false;
		}

		return true;
	}

	protected boolean saveGraphDataToFile(Graph graph, String[] files) {
		return saveGraphDataToFile(graph, files, "fitness", false);
	}

	protected boolean saveGraphDataToFile(Graph graph, String[] files, String fileName, boolean metricsFile) {
		try {
			/*
			 * Get the name to use on file to create
			 */
			File f = new File(files[0]);
			String name = "";
			if (files.length == 1) {
				name = f.getParentFile().getParentFile().getName() + "_" + f.getParentFile().getName() + "_" + fileName
						+ "." + SAVE_TO_DATA_EXTENSION;
			} else {
				name = f.getParentFile().getParentFile().getName() + "_" + fileName + "." + SAVE_TO_DATA_EXTENSION;
			}

			if (!new File(PLOTS_DATA_FOLDER_PATH).exists()) {
				new File(PLOTS_DATA_FOLDER_PATH).mkdir();
			}

			/*
			 * Get the max vector size in the list
			 */
			Vector<Vector<Double>> dataLists = graph.getDataLists();
			int maxVectorSize = Integer.MIN_VALUE;

			for (int i = 0; i < dataLists.size(); i++) {
				if (dataLists.get(i).size() > maxVectorSize) {
					maxVectorSize = dataLists.get(i).size();
				}
			}

			/*
			 * Create a bi-dimensional array with all values to be write to file
			 */
			double[][] data = new double[maxVectorSize][dataLists.size() + 1];

			// Add the generation number
			for (int line = 0; line < maxVectorSize; line++) {
				data[line] = new double[dataLists.size() + 1];
				data[line][0] = line;
			}

			// Collect all data in the array
			for (int column = 0; column < dataLists.size(); column++) {
				Vector<Double> vector = dataLists.get(column);

				for (int line = 0; line < vector.size(); line++) {
					data[line][column + 1] = vector.get(line);
				}
			}

			/*
			 * Write everything to a file
			 */
			RequestDataParamsPane panel = new RequestDataParamsPane();
			if (panel.triggerDialog() == JOptionPane.OK_OPTION) {
				PrintWriter pw = new PrintWriter(new FileOutputStream(new File(PLOTS_DATA_FOLDER_PATH, name)));

				if (panel.addColumnsIdentifiers()) {
					if (panel.addGenerationNumber()) {
						pw.print("generation" + panel.getArgumentsSeparator());
					}
					Vector<String> legends = graph.getLegends();
					for (int i = 0; i < legends.size(); i++) {
						String[] substrings = legends.get(i).split("\\\\");
						String legend = substrings[substrings.length - 1];

						if (metricsFile) {
							String[] splittedNames = legend.split("\\.log");
							if (splittedNames.length > 1) {
								legend = splittedNames[1];
							}
						}
						
						if (legend.startsWith("_")) {
							legend = legend.substring(1, legend.length());
						}

						pw.print(legend);

						if (i < legends.size() - 1) {
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

	protected class RequestDataParamsPane {
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

		public int getDialogResult() {
			return dialogResult;
		}
	}
}