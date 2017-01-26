package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;

import environment.target.FormationMultiTargetEnvironment;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.MetricsData;
import evolutionaryrobotics.evaluationfunctions.KeepPositionInTargetEvaluationFunction;
import gui.utils.FormationsMetricsGraphPlotter;
import gui.utils.FormationsMetricsGraphPlotter.MetricsType;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.util.Arguments;

public class FormationCIResultViewerGui extends CIResultViewerGui {
	private static final long serialVersionUID = -272519649519979276L;
	protected final String PLOTS_IMAGES_FOLDER_PATH = ".\\plots";
	protected final String SAVE_TO_IMAGE_EXTENSION = "png";

	private JButton timeInsideRobotMetricsButton;
	private JButton timeUntilFirstOccupationButton;
	private JButton permutationMetricButton;
	private JButton reocupationTimeButton;

	private JPanel faultInjectionPanel;
	private JTextField faultInjectionStepTextField;
	private boolean updateFaultInjectionPanel = false;

	private boolean gotMetricsGetMethods = false;
	private JPanel mainMetricsPanel;
	private JPanel metricsPanel = null;
	private JCheckBox collectMetricsCheckBox;
	private ArrayList<Method> metricsDataGetMethods;
	private HashMap<Method, JTextField> methodsTextFields;

	public FormationCIResultViewerGui(JBotSim jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
	}

	@Override
	protected void launchGraphPlotter(JBotEvolver jbot, Simulator sim) {
		new CIGraphPlotter(jbot, sim);
	}

	@Override
	protected Container initRightWrapperPanel() {

		Container panel = super.initRightWrapperPanel();

		if (enableDebugOptions) {
			faultInjectionPanel = new JPanel(new GridLayout(1, 2));
			debugOptions.add(faultInjectionPanel);

			Component[] components = extraOptionsPanel.getComponents();
			JPanel originalComponentsPanel = new JPanel(new GridLayout(4, 1));
			for (Component component : components) {
				originalComponentsPanel.add(component);
			}

			extraOptionsPanel.setLayout(new BorderLayout());
			extraOptionsPanel.add(originalComponentsPanel, BorderLayout.CENTER);

			JButton exportRenderToImageButton = new JButton("Export renderer to image");
			exportRenderToImageButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					exportRendererToImage();
				}
			});
			extraOptionsPanel.add(exportRenderToImageButton, BorderLayout.SOUTH);

			// Metrics info
			mainMetricsPanel = new JPanel(new BorderLayout());
			mainMetricsPanel.setBorder(BorderFactory.createTitledBorder("Metrics"));
			collectMetricsCheckBox = new JCheckBox("Collect metrics");
			collectMetricsCheckBox.setSelected(false);
			collectMetricsCheckBox.setHorizontalAlignment(JCheckBox.CENTER);

			mainMetricsPanel.add(collectMetricsCheckBox, BorderLayout.NORTH);
			debugOptions.add(mainMetricsPanel);
		}

		if (panel instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) panel).getViewport();
			viewport.setLayout(new ConstrainedViewPortLayout());
			return panel;
		} else {
			JScrollPane jScroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			JViewport viewport = jScroll.getViewport();
			viewport.setLayout(new ConstrainedViewPortLayout());
			return jScroll;
		}
	}

	@Override
	protected JPanel initLeftWrapperPanel() {
		/*
		 * Top section
		 */
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.add(currentFileTextField);
		currentFileTextField.setAlignmentX(Component.CENTER_ALIGNMENT);

		// File tree
		treeWrapper = new JPanel(new BorderLayout());
		fileTree = new FileTree(new File("."));
		topPanel.add(fileTree);
		fileTree.setAlignmentX(Component.CENTER_ALIGNMENT);

		topPanel.add(fitnessSummary);
		fitnessSummary.setAlignmentX(Component.CENTER_ALIGNMENT);

		// Buttons
		JPanel editLoadPanel = new JPanel(new GridLayout(1, 3));
		editLoadPanel.add(openButton);
		editLoadPanel.add(editButton);
		editLoadPanel.add(loadButton);
		editLoadPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel actionsButtonsPanel = new JPanel(new BorderLayout());
		actionsButtonsPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
		actionsButtonsPanel.add(editLoadPanel, BorderLayout.NORTH);
		actionsButtonsPanel.add(compareFitnessButton, BorderLayout.CENTER);
		actionsButtonsPanel.add(plotFitnessButton, BorderLayout.SOUTH);

		actionsButtonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(actionsButtonsPanel);

		// Metrics panel
		JPanel metricsPanel = new JPanel(new GridLayout(4, 1));
		metricsPanel.setBorder(BorderFactory.createTitledBorder("Metrics plot"));

		timeInsideRobotMetricsButton = new JButton("Time inside spots");
		timeUntilFirstOccupationButton = new JButton("Time until first occupation");
		permutationMetricButton = new JButton("Occupied spots count");
		reocupationTimeButton = new JButton("Reocupation time");

		metricsPanel.add(timeInsideRobotMetricsButton);
		metricsPanel.add(timeUntilFirstOccupationButton);
		metricsPanel.add(permutationMetricButton);
		metricsPanel.add(reocupationTimeButton);
		topPanel.add(metricsPanel);

		/*
		 * Arguments panel
		 */
		JPanel argumentsPanel = new JPanel(new BorderLayout());
		extraArguments = new JEditorPane();
		argumentsPanel.setBorder(BorderFactory.createTitledBorder("Extra arguments"));
		argumentsPanel.add(new JScrollPane(extraArguments), BorderLayout.CENTER);
		argumentsPanel.add(newRandomSeedButton, BorderLayout.SOUTH);

		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");

		/*
		 * Join everything
		 */
		treeWrapper.add(topPanel, BorderLayout.NORTH);
		treeWrapper.add(argumentsPanel, BorderLayout.CENTER);
		treeWrapper.setBorder(BorderFactory.createTitledBorder("Experiments"));

		return treeWrapper;
	}

	@Override
	protected void updateStatus() {
		if (enableDebugOptions) {
			if (collectMetricsCheckBox.isSelected() && evaluationFunction.getMetricsData() != null) {
				if (!gotMetricsGetMethods && simulator != null && jBotEvolver.getArguments() != null
						&& evaluationFunction != null) {
					gotMetricsGetMethods = getMetricsGetMethods();

					if (gotMetricsGetMethods) {
						buildMetricsPanel(metricsDataGetMethods);
					}
				} else {
					if (metricsPanel == null && !metricsDataGetMethods.isEmpty()) {
						buildMetricsPanel(metricsDataGetMethods);
					}

					synchronized (methodsTextFields) {
						MetricsData metricsData = evaluationFunction.getMetricsData();
						for (Method method : methodsTextFields.keySet()) {
							try {
								Object object = method.invoke(metricsData);

								if (method.getName().contains("getGeneration") && ((int) object) == -1) {
									methodsTextFields.get(method).setText(Integer
											.toString(jBotEvolver.getPopulation().getNumberOfCurrentGeneration()));
								} else {
									if (object instanceof Double) {
										methodsTextFields.get(method)
												.setText(new DecimalFormat("#.######").format(object));
									} else {
										methodsTextFields.get(method).setText(object.toString());
									}
								}
							} catch (IllegalAccessException | InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} else if (evaluationFunction.isCollectingMetrics() && metricsPanel != null) {
				clearMetricsPanel();
			}

			if (updateFaultInjectionPanel) {
				FormationMultiTargetEnvironment environment = (FormationMultiTargetEnvironment) simulator
						.getEnvironment();
				faultInjectionStepTextField.setText(Integer.toString(environment.getTimestepToInjectFaults()));
				updateFaultInjectionPanel = false;
			}
		}

		super.updateStatus();

	}

	@Override
	public Simulator loadSimulator() {
		Simulator simulator = super.loadSimulator();
		if (simulator != null) {
			if (simulator.getEnvironment() instanceof FormationMultiTargetEnvironment
					&& ((FormationMultiTargetEnvironment) simulator.getEnvironment()).isFaultInjectionActive()) {
				buildFaultInjectionPanel();
				updateFaultInjectionPanel = true;
			} else {
				clearFaultInjectionPanel();
				updateFaultInjectionPanel = false;
			}

			if (evaluationFunction.getMetricsData() == null) {
				evaluationFunction.setCollectMetrics(collectMetricsCheckBox.isSelected());

				if (collectMetricsCheckBox.isSelected()) {
					gotMetricsGetMethods = false;
				}
			}

			if (metricsPanel != null) {
				clearMetricsPanel();

				gotMetricsGetMethods = false;
				metricsDataGetMethods.clear();
			}
		}

		return simulator;

	}

	@Override
	protected void initListeners() {
		super.initListeners();

		if (enableDebugOptions) {
			timeInsideRobotMetricsButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.TIME_INSIDE_FORMATION);
				}
			});

			timeUntilFirstOccupationButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.TIME_TO_FIRST_OCCUPATION);
				}
			});

			permutationMetricButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.PERMUTATION_METRICS);
				}
			});

			reocupationTimeButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotGraph(MetricsType.REOCUPATION_TIME);
				}
			});

			collectMetricsCheckBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (simulator != null) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							if (!evaluationFunction.isCollectingMetrics()) {
								int currentTime = simulator.getTime().intValue();
								int status = simulationState;

								loadCurrentFile();
								simulateUntil = currentTime;
								simulationState = status;
								startPauseButton();
							}

							if (simulationState == PAUSED || simulationState == ENDED) {
								updateStatus();
							}
						} else if (e.getStateChange() == ItemEvent.DESELECTED
								&& (simulationState == PAUSED || simulationState == ENDED)) {
							updateStatus();
						}
					}
				}
			});
		}
	}

	private boolean getMetricsGetMethods() {
		MetricsData metricsData = evaluationFunction.getMetricsData();

		if (metricsData != null && MetricsData.class.isInstance(metricsData)) {
			Class<? extends MetricsData> metricsDataClass = metricsData.getClass();

			metricsDataGetMethods = new ArrayList<Method>();
			for (Method method : metricsDataClass.getDeclaredMethods()) {
				if (method.getName().contains("get")) {
					metricsDataGetMethods.add(method);
				}
			}
			metricsDataGetMethods.sort(new Comparator<Method>() {

				@Override
				public int compare(Method method1, Method method2) {
					String method1Name = method1.getName();
					String method2Name = method2.getName();

					return method1Name.compareTo(method2Name);
				}
			});

			return true;
		} else {
			return false;
		}
	}

	private void buildMetricsPanel(Collection<Method> methods) {
		JPanel labelsPanel = new JPanel();
		JPanel textFieldsPanel = new JPanel();
		methodsTextFields = new HashMap<Method, JTextField>();
		for (Method method : methods) {
			String[] split = method.getName().split("\\.");
			String name = split[split.length - 1].replace("get", "");

			labelsPanel.add(new JLabel(name + ":"));

			JTextField textField = new JTextField("N/A", 8);
			textField.setHorizontalAlignment(JTextField.CENTER);
			textField.setEditable(false);
			textFieldsPanel.add(textField);

			methodsTextFields.put(method, textField);
		}

		labelsPanel.setLayout(new GridLayout(methodsTextFields.keySet().size(), 1));
		textFieldsPanel.setLayout(new GridLayout(methodsTextFields.keySet().size(), 1));

		metricsPanel = new JPanel();
		metricsPanel.setLayout(new BorderLayout());
		metricsPanel.add(labelsPanel, BorderLayout.WEST);
		metricsPanel.add(textFieldsPanel, BorderLayout.CENTER);

		mainMetricsPanel.add(metricsPanel, BorderLayout.CENTER);
		mainMetricsPanel.validate();
		mainMetricsPanel.repaint();

		debugOptions.repaint();

		if (rightWrapperPanel instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) rightWrapperPanel).getViewport();
			Component[] components = viewport.getComponents();

			for (Component component : components) {
				component.validate();
				component.repaint();
			}
		}
		rightWrapperPanel.repaint();
		validate();

		if (evaluationFunction instanceof KeepPositionInTargetEvaluationFunction) {
			((KeepPositionInTargetEvaluationFunction) evaluationFunction).setContinuousMetricsUpdate(true);
		}
	}

	private void clearMetricsPanel() {
		metricsPanel.removeAll();
		metricsPanel = null;

		methodsTextFields.clear();

		mainMetricsPanel.validate();
		mainMetricsPanel.repaint();

		debugOptions.repaint();

		if (rightWrapperPanel instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) rightWrapperPanel).getViewport();
			Component[] components = viewport.getComponents();

			for (Component component : components) {
				component.validate();
				component.repaint();
			}
		}

		rightWrapperPanel.repaint();
		validate();
	}

	private void buildFaultInjectionPanel() {
		faultInjectionPanel.removeAll();

		faultInjectionPanel.setBorder(BorderFactory.createTitledBorder("Fault injection"));
		faultInjectionPanel.add(new JLabel("Injection step:"));

		faultInjectionStepTextField = new JTextField("N/A", 8);
		faultInjectionStepTextField.setHorizontalAlignment(JTextField.CENTER);
		faultInjectionStepTextField.setEditable(false);
		faultInjectionPanel.add(faultInjectionStepTextField);

		faultInjectionPanel.validate();
		faultInjectionPanel.repaint();
		debugOptions.repaint();

		if (rightWrapperPanel instanceof JScrollPane) {
			JViewport viewport = ((JScrollPane) rightWrapperPanel).getViewport();
			Component[] components = viewport.getComponents();

			for (Component component : components) {
				component.validate();
				component.repaint();
			}
		}

		rightWrapperPanel.repaint();
		validate();
	}

	private void clearFaultInjectionPanel() {
		if (faultInjectionPanel != null) {
			faultInjectionPanel.removeAll();
			faultInjectionPanel.setBorder(null);

			debugOptions.repaint();

			if (rightWrapperPanel instanceof JScrollPane) {
				JViewport viewport = ((JScrollPane) rightWrapperPanel).getViewport();
				Component[] components = viewport.getComponents();

				for (Component component : components) {
					component.validate();
					component.repaint();
				}
			}

			rightWrapperPanel.repaint();
			validate();
		}
	}

	private void plotGraph(MetricsType type) {
		TreePath[] selectedFiles = fileTree.getSelectedFilesPaths();
		ArrayList<String> paths = new ArrayList<String>();

		// List all the selected folders and files
		if (selectedFiles == null) {
			JOptionPane.showMessageDialog(this, "No folders or files selected!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			String parentpath = "";
			for (TreePath treePath : selectedFiles) {
				if (treePath.getParentPath() == null) {
					paths.add(treePath.getLastPathComponent().toString());
				} else {
					if (parentpath.equals("")) {
						parentpath = treePath.getParentPath().toString().replace("[", "");
						parentpath = parentpath.replace("]", "");

						if (parentpath.endsWith(".")) {
							parentpath = parentpath.substring(0, parentpath.lastIndexOf("."));
						}
					}

					if (System.getProperty("os.name").contains("Windows")) {
						paths.add(parentpath + File.separatorChar + treePath.getLastPathComponent());
					} else {
						paths.add(parentpath + "/" + treePath.getLastPathComponent());
					}
				}
			}
		}

		// Join all the selected folders and files paths
		final List<String> files = new ArrayList<String>();
		for (int i = 0; i < paths.size(); i++) {
			File file = new File(paths.get(i));
			files.add(file.getAbsolutePath());
		}

		// Workaround to remove duplicates (in case they exist)
		Set<String> hashSet = new HashSet<String>(files);
		files.clear();
		files.addAll(hashSet);

		Thread t = new Thread(new GraphTread(files, type));
		t.start();
	}

	private void exportRendererToImage() {
		if (renderer != null && simulator != null) {
			boolean wasRunning = false;
			if (simulationState == RUN) {
				startPauseButton();
				wasRunning = true;
			}

			String originalTitle = renderer.getTitleText();
			renderer.setTitleText("");
			renderer.drawFrame();

			try {
				BufferedImage img = new BufferedImage(renderer.getWidth(), renderer.getHeight(),
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = img.createGraphics();
				renderer.printAll(g2d);
				g2d.dispose();

				String name = originalTitle;
				if (name.isEmpty()) {
					name = "noname";
				} else {
					name = name.substring(6, name.length());

					String[] splittedString = name.split("\\\\");
					if (splittedString.length > 3) {
						name = splittedString[splittedString.length - 3] + "_"
								+ splittedString[splittedString.length - 2]
								+ splittedString[splittedString.length - 1].replaceAll("\\.conf", "");
					} else {
						name = splittedString[splittedString.length - 1].replaceAll("\\.conf", "").replaceAll("_", "");
					}
				}

				if (!new File(PLOTS_IMAGES_FOLDER_PATH).exists()) {
					new File(PLOTS_IMAGES_FOLDER_PATH).mkdir();
				}

				boolean save = true;
				if (new File(PLOTS_IMAGES_FOLDER_PATH, name + "." + SAVE_TO_IMAGE_EXTENSION).exists()) {
					final String nameCopy = name;
					File[] files = new File(PLOTS_IMAGES_FOLDER_PATH).listFiles(new FileFilter() {

						@Override
						public boolean accept(File arg0) {
							String name = arg0.getName();
							return name.contains(nameCopy);
						}
					});

					if (files != null && files.length > 0) {
						String lastFileName = files[files.length - 1].getName();
						lastFileName = lastFileName.replaceAll("\\." + SAVE_TO_IMAGE_EXTENSION, "");

						String[] splittedName = lastFileName.split("_");

						if (splittedName.length > 1) {
							try {
								int number = Integer.parseInt(splittedName[splittedName.length - 1]) + 1;
								name += "_" + number;
							} catch (NumberFormatException e) {
								name += "_1";
							}
						} else {
							name += "_1";
						}

						name += "." + SAVE_TO_IMAGE_EXTENSION;
					} else {
						System.err.printf("[%s] Error getting existing files names%n", getClass().getSimpleName());
						save = false;
					}
				} else {
					name += "." + SAVE_TO_IMAGE_EXTENSION;
				}

				if (save) {
					ImageIO.write(img, SAVE_TO_IMAGE_EXTENSION, new File(PLOTS_IMAGES_FOLDER_PATH, name));
					JOptionPane.showMessageDialog(null, "Saved image!", "Sucess", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Error saving image!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {
				System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				JOptionPane.showMessageDialog(null, "Error saving image!", "Error", JOptionPane.ERROR_MESSAGE);
			}

			renderer.setTitleText(originalTitle);
			renderer.drawFrame();

			if (wasRunning) {
				startPauseButton();
			}
		} else {
			JOptionPane.showMessageDialog(null, "No simulation running!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected class GraphTread implements Runnable {
		private List<String> folders;
		private MetricsType type;

		public GraphTread(List<String> folders, MetricsType type) {
			this.folders = folders;
			this.type = type;
		}

		@Override
		public void run() {
			// Get a list of all the metrics files
			ArrayList<String> files = new ArrayList<String>();
			for (String folder : folders) {
				String[] fs = getMetricsFiles(folder).split("###");

				for (String str : fs) {
					if (!str.isEmpty()) {
						files.add(str);
					}
				}
			}

			if (files != null && !files.isEmpty()) {
				new FormationsMetricsGraphPlotter(files.toArray(new String[files.size()]), type);
			} else {
				JOptionPane.showMessageDialog(null, "No files to compare!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}

		/*
		 * Recursively look for a file in the supplied folder
		 */
		private String getMetricsFiles(String folder) {
			if (folder == null) {
				JOptionPane.showMessageDialog(null, "No folders selected!", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}

			if (new File(folder).isFile()) {
				return new File(folder).getAbsolutePath();
			} else {
				File folderFile = new File(folder);
				File[] files = folderFile.listFiles(new FileFilter() {

					@Override
					public boolean accept(File pathname) {
						return pathname.getName().contains("_metrics");
					}
				});

				String result = "";
				for (File file : files) {
					if (file.isFile()) {
						result += file.getAbsolutePath() + "###";
					} else {
						String[] directories = (new File(folder)).list(new FilenameFilter() {
							@Override
							public boolean accept(File dir, String name) {
								return (new File(dir, name)).isDirectory();
							}
						});
						if (directories != null) {
							for (String dir : directories) {
								String dirResult = getMetricsFiles(folder + File.separatorChar + dir);
								if (!dirResult.isEmpty())
									result += dirResult + "###";
							}
						}
					}
				}
				return result;
			}
		}
	}
}
