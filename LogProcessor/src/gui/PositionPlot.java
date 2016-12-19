package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.joda.time.DateTime;

import commoninterface.dataobjects.GPSData;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.ObstacleLocation;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.entities.formation.Formation;
import commoninterface.entities.formation.Target;
import commoninterface.utils.logger.DecodedLog;
import commoninterface.utils.logger.EntityManipulation;
import commoninterface.utils.logger.LogData;
import gui.map.MapPanel;
import logManaging.EntitiesLogFilesParser;
import logManaging.ExperimentLogParser;
import logManaging.ExperimentLogParser.ExperimentData;
import logManaging.FileUtils;
import logManaging.FileUtils.ExperimentsDataOnFile;
import logManaging.GPSLogFilesParser;
import logManaging.LogFilesMerger;
import logManaging.ValuesLogFilesParser;

@SuppressWarnings("unused")
public class PositionPlot extends JFrame {
	private final boolean LOAD_DATA = true;
	private final static String RAW_LOGS_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\logs";
	private final String MERGED_LOGS_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";

	private final String PARSED_DATA_FILE_GPS = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs\\mergedLogs_gps.log";
	private final String PARSED_DATA_FILE_ENTITIES = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs\\mergedLogs_entities.log";
	private final String PARSED_DATA_FILE_LOG = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs\\mergedLogs_logData.log";
	private final String PARSED_DATA_FILE_EXPERIMENTS = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs\\mergedLogs_experiments.log";

	// GUI Components
	private Container centerPanel;
	private Container controlsPanel;
	private Container leftPanel;
	private Container centralAndRightPanelsWrapper;
	private InformationTree informationTree;
	private JTextArea informationTextArea;
	private MapPanel mapPanel;
	private JButton replayButton;
	private JButton playPauseButton;
	private JSlider playerSlider;
	private JTextField logLineTextField;
	private JComboBox<String> availableOptionsLogsCombobox;
	private PlayerThread playerThread;

	// Data parsers and loaders
	private enum DataSource {
		GPS, ENTITIES, LOG, EXPERIMENT, NONE;
	}

	private DataSource dataSource;

	private HashMap<Integer, List<GPSData>> gpsData = null;
	private HashMap<Integer, List<EntityManipulation>> entitiesManipulationData = null;
	private HashMap<Integer, List<DecodedLog>> decodedLogData = null;
	private HashMap<Integer, ExperimentData> experimentsData = null;

	public PositionPlot() {
		this(null);
	}

	public PositionPlot(String pathToFile) {
		super();

		if (pathToFile != null) {
			File toOpenFolder = new File(pathToFile);
			if (!toOpenFolder.exists() || toOpenFolder.isFile()) {
				toOpenFolder = selectFolderToOpen();
			}

			if (toOpenFolder == null || !toOpenFolder.exists() || !toOpenFolder.isDirectory()) {
				System.err.printf("[%s] Unable to load data from RAW files!%n", getClass().getSimpleName());
				System.exit(1);
			} else {
				processOriginalLogFiles();
			}
		} else {
			File gpsDataFile = new File(PARSED_DATA_FILE_GPS);
			File entitiesDataFile = new File(PARSED_DATA_FILE_ENTITIES);
			File logDataFile = new File(PARSED_DATA_FILE_LOG);
			File experimentsDataFile = new File(PARSED_DATA_FILE_EXPERIMENTS);

			if (!gpsDataFile.exists() || !entitiesDataFile.exists() || !logDataFile.exists() || !logDataFile.exists()) {
				File toOpenFolder = selectFolderToOpen();

				if (toOpenFolder == null || !toOpenFolder.exists() || !toOpenFolder.isDirectory()) {
					System.err.printf("[%s] Unable to load data from RAW files!%n", getClass().getSimpleName());
					System.exit(1);
				} else {
					processOriginalLogFiles();
				}
			}
		}

		if (LOAD_DATA) {
			try {
				ExperimentsDataOnFile experimentsDataOnFile = FileUtils
						.loadDataFromCompressedParsedFile(PARSED_DATA_FILE_EXPERIMENTS);
				experimentsData = experimentsDataOnFile.getExperimentsData();

				for (int k : experimentsData.keySet()) {
					experimentsData.get(k).sortSteps();
				}

				// ExperimentLogParser experimentLogParser = new
				// ExperimentLogParser(MERGED_LOGS_FOLDER);
				// experimentData = experimentLogParser.getExperimentsData();
				dataSource = DataSource.EXPERIMENT;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			dataSource = DataSource.NONE;
		}

		if (!LOAD_DATA || experimentsData != null) {
			playerThread = new PlayerThread();
			buildGUI();
			setVisible(true);
		} else {
			System.err.printf("[%s] Missing data on parsed data file!%n", getClass().getSimpleName());
			System.exit(1);
		}
	}

	/*
	 * GUI Manipulations
	 */
	private File selectFolderToOpen() {
		JFileChooser chooser = new JFileChooser(".");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(false);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			if (chooser.getSelectedFile().isFile()) {
				JOptionPane.showMessageDialog(this, "Folder not found or invalid!", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			} else {
				return chooser.getSelectedFile();
			}
		} else {
			return null;
		}
	}

	private void buildGUI() {
		// Left panel
		leftPanel = new JPanel(new BorderLayout());

		informationTree = new InformationTree();
		informationTextArea = new JTextArea(4, 20);
		JScrollPane jScrollTextArea = new JScrollPane(informationTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JViewport viewport = jScrollTextArea.getViewport();
		viewport.setLayout(new ConstrainedViewPortLayout());

		JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, informationTree, jScrollTextArea);
		horizontalSplitPane.setOneTouchExpandable(true);
		horizontalSplitPane.setDividerLocation(informationTree.getPreferredSize().height);
		leftPanel.add(horizontalSplitPane, BorderLayout.CENTER);

		// Center panel
		mapPanel = new MapPanel();
		centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(mapPanel, BorderLayout.CENTER);

		centralAndRightPanelsWrapper = new JPanel(new BorderLayout());
		centralAndRightPanelsWrapper.add(centerPanel, BorderLayout.CENTER);

		controlsPanel = buildControlsPanel();
		centralAndRightPanelsWrapper.add(controlsPanel, BorderLayout.SOUTH);

		// Left/Center split pane
		JSplitPane verticalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
				centralAndRightPanelsWrapper);
		verticalSplitPane.setOneTouchExpandable(true);
		verticalSplitPane.setDividerLocation(leftPanel.getPreferredSize().width);

		setLayout(new BorderLayout());
		add(verticalSplitPane, BorderLayout.CENTER);

		// Other
		setMinimumSize(new Dimension(1400, 1000));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Log plotter");
	}

	private Container buildControlsPanel() {
		BorderLayout centralWrapperLayout = new BorderLayout();
		centralWrapperLayout.setHgap(centralWrapperLayout.getHgap() + 10);
		JPanel centralWrapperPanel = new JPanel(centralWrapperLayout);

		// Right part
		JPanel leftControlsPanel = new JPanel(new BorderLayout());
		replayButton = new JButton("Replay");
		replayButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				playerThread.replay();
			}
		});
		playPauseButton = new JButton("Play");
		playPauseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				playerThread.togglePlayStatus();
			}
		});
		playPauseButton.setPreferredSize(replayButton.getPreferredSize());

		leftControlsPanel.add(replayButton, BorderLayout.WEST);
		leftControlsPanel.add(playPauseButton, BorderLayout.EAST);
		centralWrapperPanel.add(leftControlsPanel, BorderLayout.WEST);

		// Central part
		playerSlider = new JSlider(0, 100);
		playerSlider.setValue(0);
		playerSlider.setPaintTicks(true);
		playerSlider.setPaintLabels(true);
		playerSlider.setEnabled(false);
		playerSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int currentStep = playerSlider.getValue();
				playerSlider.setToolTipText(Integer.toString(playerSlider.getValue()));
				if (playerThread != null && !playerThread.isPlaying()) {
					// playerThread.moveTo(playerSlider.getValue());
				}
			}
		});
		centralWrapperPanel.add(playerSlider, BorderLayout.CENTER);

		JPanel logLinePanel = new JPanel(new SpringLayout());
		logLineTextField = new JTextField("0", 5);
		logLineTextField.setEditable(false);
		logLineTextField.setHorizontalAlignment(JTextField.CENTER);

		switch (dataSource) {
		case GPS:
		case ENTITIES:
		case LOG:
			logLinePanel.add(new JLabel("Log line:"));
			break;
		case EXPERIMENT:
			logLinePanel.add(new JLabel("Timestep:"));
			break;
		case NONE:
			logLinePanel.add(new JLabel("N/A:"));
			break;
		}

		logLinePanel.add(logLineTextField);
		SpringUtilities.makeGrid(logLinePanel, 1, 2, 0, 0, 5, 0);
		centralWrapperPanel.add(logLinePanel, BorderLayout.EAST);

		// Left part with combo box that selects the data to load
		JPanel leftControlPanel = new JPanel(new SpringLayout());
		availableOptionsLogsCombobox = new JComboBox<String>(getRobotLogsLabels());
		availableOptionsLogsCombobox.setPreferredSize(replayButton.getPreferredSize());
		availableOptionsLogsCombobox.setEditable(false);
		((JLabel) availableOptionsLogsCombobox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		availableOptionsLogsCombobox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				playerThread.pause();
				updateSliderLabels();
				playerThread.load();
			}
		});
		if (availableOptionsLogsCombobox.getItemCount() == 0) {
			availableOptionsLogsCombobox.setEnabled(false);
		} else {
			availableOptionsLogsCombobox.setSelectedItem(0);
			updateSliderLabels();
			playerThread.load();
		}

		switch (dataSource) {
		case GPS:
		case ENTITIES:
		case LOG:
			leftControlPanel.add(new JLabel("Robot:"));
			break;
		case EXPERIMENT:
			leftControlPanel.add(new JLabel("Experiment:"));
			break;
		case NONE:
			leftControlPanel.add(new JLabel("N/A:"));
			break;
		}

		leftControlPanel.add(availableOptionsLogsCombobox);
		SpringUtilities.makeGrid(leftControlPanel, 1, 2, 0, 0, 5, 0);

		// Join everything in the main panel
		BorderLayout controlPanelLayout = new BorderLayout();
		controlPanelLayout.setHgap(controlPanelLayout.getHgap() + 10);

		JPanel controlPanel = new JPanel(controlPanelLayout);
		controlPanel.add(leftControlPanel, BorderLayout.WEST);
		controlPanel.add(centralWrapperPanel, BorderLayout.CENTER);
		return controlPanel;
	}

	private void printText(String str) {
		StringBuilder builder = new StringBuilder(informationTextArea.getText());
		builder.append(str);
		informationTextArea.setText(builder.toString());
	}

	private String[] getRobotLogsLabels() {
		switch (dataSource) {
		case GPS:
			if (gpsData != null) {
				List<String> labels = new ArrayList<String>();

				for (Integer i : gpsData.keySet()) {
					List<GPSData> list = gpsData.get(i);
					if (list != null && !list.isEmpty()) {
						labels.add(Integer.toString(i));
					}
				}

				return labels.toArray(new String[labels.size()]);
			} else {
				return new String[] {};
			}
		case ENTITIES:
			if (entitiesManipulationData != null) {
				List<String> labels = new ArrayList<String>();

				for (Integer i : entitiesManipulationData.keySet()) {
					List<EntityManipulation> list = entitiesManipulationData.get(i);
					if (list != null && !list.isEmpty()) {
						labels.add(Integer.toString(i));
					}
				}

				return labels.toArray(new String[labels.size()]);
			} else {
				return new String[] {};
			}
		case LOG:
			if (decodedLogData != null) {
				List<String> labels = new ArrayList<String>();

				for (Integer i : decodedLogData.keySet()) {
					List<DecodedLog> list = decodedLogData.get(i);
					if (list != null && !list.isEmpty()) {
						labels.add(Integer.toString(i));
					}
				}

				return labels.toArray(new String[labels.size()]);
			} else {
				return new String[] {};
			}
		case EXPERIMENT:
			if (experimentsData != null) {
				List<String> labels = new ArrayList<String>();

				for (Integer i : experimentsData.keySet()) {
					labels.add(Integer.toString(i));
				}

				return labels.toArray(new String[labels.size()]);
			} else {
				return new String[] {};
			}
		case NONE:
		default:
			return new String[] {};
		}
	}

	private void updateSliderLabels() {
		int index = Integer.parseInt((String) availableOptionsLogsCombobox.getSelectedItem());
		int maximum = 0;

		switch (dataSource) {
		case GPS:
			maximum = gpsData.get(index).size();
			break;
		case ENTITIES:
			maximum = entitiesManipulationData.get(index).size();
			break;
		case LOG:
			maximum = decodedLogData.get(index).size();
			break;
		case EXPERIMENT:
			maximum = (int) experimentsData.get(index).timestepsCount;
			break;
		case NONE:
			maximum = playerSlider.getMinimum();
			break;
		}

		playerSlider.setMajorTickSpacing(maximum / 10);
		playerSlider.setMinorTickSpacing(maximum / 25);
		playerSlider.setMaximum(maximum);
		playerSlider.setEnabled(true);
		playerSlider.validate();
		playerSlider.repaint();
	}

	/*
	 * Plot and data management "things"
	 */
	private void processOriginalLogFiles() {
		// Merge log files
		boolean continueProcessing = true;
		try {
			System.out.printf("[%s] ######################## Merging logs%n", getClass().getSimpleName());
			new LogFilesMerger(RAW_LOGS_FOLDER, MERGED_LOGS_FOLDER);
		} catch (FileNotFoundException e) {
			System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
			continueProcessing = false;
		} catch (FileAlreadyExistsException e) {
			System.out.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
		} catch (FileSystemException e) {
			System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
			continueProcessing = false;
		}

		if (continueProcessing) {
			// Just for the system outs to be nice :P
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}

			// GPS logs
			if (!new File(PARSED_DATA_FILE_GPS).exists()) {
				try {
					System.out.printf("[%s] ######################## Parsing GPS logs%n", getClass().getSimpleName());
					GPSLogFilesParser gpsLogFilesParser = new GPSLogFilesParser(MERGED_LOGS_FOLDER);
					gpsLogFilesParser.saveParsedDataToFile(false);
				} catch (FileNotFoundException e) {
					System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileAlreadyExistsException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileSystemException e) {
					System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
				}
			} else {
				System.out.printf("[%s] ######################## GPS logs already parsed%n",
						getClass().getSimpleName());
			}

			// Entities logs
			if (!new File(PARSED_DATA_FILE_ENTITIES).exists()) {
				try {
					System.out.printf("[%s] ######################## Parsing entities logs%n",
							getClass().getSimpleName());
					EntitiesLogFilesParser entitiesLogFilesParser = new EntitiesLogFilesParser(MERGED_LOGS_FOLDER,
							true);
					entitiesLogFilesParser.saveParsedDataToFile(false);
				} catch (FileNotFoundException e) {
					System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileAlreadyExistsException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileSystemException e) {
					System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
				}
			} else {
				int result = JOptionPane.showConfirmDialog(null, "Entites logs already parsed. Re-parse?", "Question",
						JOptionPane.OK_CANCEL_OPTION);

				if (result == JOptionPane.OK_OPTION) {
					try {
						System.out.printf(
								"[%s] ######################## Entites logs already parsed, but re-generating parses without pre-processing%n",
								getClass().getSimpleName());
						EntitiesLogFilesParser entitiesLogFilesParser = new EntitiesLogFilesParser(MERGED_LOGS_FOLDER,
								false);
						entitiesLogFilesParser.saveParsedDataToFile(true);
					} catch (FileNotFoundException e) {
						System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
					} catch (FileAlreadyExistsException e) {
						System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
					} catch (FileSystemException e) {
						System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
					}
				}
			}

			// Log lines logs
			if (!new File(PARSED_DATA_FILE_LOG).exists()) {
				try {
					System.out.printf("[%s] ######################## Parsing values logs%n",
							getClass().getSimpleName());
					ValuesLogFilesParser valuesLogFilesParser = new ValuesLogFilesParser(MERGED_LOGS_FOLDER, true);
					valuesLogFilesParser.saveParsedDataToFile(false);
				} catch (FileNotFoundException e) {
					System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileAlreadyExistsException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileSystemException e) {
					System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
				}
			} else {
				System.out.printf("[%s] ######################## Values logs already parsed%n",
						getClass().getSimpleName());
			}

			// Experiments logs
			if (!new File(PARSED_DATA_FILE_EXPERIMENTS).exists()) {
				try {
					System.out.printf("[%s] ######################## Parsing experiments logs%n",
							getClass().getSimpleName());
					ExperimentLogParser experimentsLogFilesParser = new ExperimentLogParser(MERGED_LOGS_FOLDER);
					experimentsLogFilesParser.saveParsedDataToFile(false);
				} catch (FileNotFoundException e) {
					System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileAlreadyExistsException e) {
					System.err.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
				} catch (FileSystemException e) {
					System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
				}
			} else {
				System.out.printf("[%s] ######################## Experiments logs already parsed%n",
						getClass().getSimpleName());
			}
		}
	}

	@SuppressWarnings("unused")
	private void updateGeoEntityInstance(EntityManipulation entityManipulation) {
		informationTree.updateEntity(entityManipulation);

		for (Entity entity : entityManipulation.getEntities()) {
			String classname = entityManipulation.getEntitiesClass();
			if (classname.equals(Waypoint.class.getSimpleName())) {
				mapPanel.addWaypoint((Waypoint) entity);
			} else if (classname.equals(Target.class.getSimpleName())) {
				mapPanel.addTarget((Target) entity);
			} else if (classname.equals(GeoFence.class.getSimpleName())) {
				mapPanel.addGeoFence((GeoFence) entity);
			} else if (classname.equals(Formation.class.getSimpleName())) {
				mapPanel.addFormation((Formation) entity);
			} else if (classname.equals(ObstacleLocation.class.getSimpleName())) {
				mapPanel.addObstacle((ObstacleLocation) entity);
			}
		}
	}

	public class PlayerThread extends Thread {
		private boolean play = false;
		private int playLimit = -1;
		private int currentStep = -1;
		private double multiplier = 1.0;

		@Override
		public void run() {
			while (true) {
				try {
					synchronized (this) {
						if (!play) {
							wait();
						}
					}

					incrementPlay();
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		}

		/*
		 * Actions and commands
		 */
		public synchronized void play() {
			play = true;
			notify();
			playPauseButton.setText("Pause");
		}

		public void pause() {
			play = false;
			playPauseButton.setText("Play");
		}

		public synchronized void replay() {
			pause();
			load();
		}

		public void load() {
			switch (dataSource) {
			case GPS:

				break;
			case ENTITIES:

				break;
			case LOG:

				break;
			case EXPERIMENT:

				break;
			case NONE:
				break;
			}
		}

		public void togglePlayStatus() {
			if (play) {
				pause();
			} else {
				play();
			}
		}

		public void playFaster() {
			multiplier *= 0.5;
			interrupt();
		}

		public void playSlower() {
			if (multiplier < Math.pow(2, 4)) {
				multiplier *= 2;
				interrupt();
			}
		}

		/*
		 * Getter and setters
		 */
		public boolean isPlaying() {
			return play;
		}

		/*
		 * Other
		 */
		private void incrementPlay() {
			if (currentStep + 1 > allData.size()) {
				playerThread.pause();
				return;
			}

			if (lastIncrementStep == currentStep) {
				currentStep++;
				slider.setValue(currentStep);
				LogData d = allData.get(currentStep);

				map.displayData(new RobotLocation(d.ip, d.latLon, d.compassOrientation, d.droneType));

				messageArea.setText("WATER TEMP: " + d.temperatures[1] + "\tCPU TEMP: " + d.temperatures[0]);

				// TODO
				// if (d.entities != null && d.ip.equals(IPforEntities)) {
				// map.replaceEntities(d.entities);
				// }
				updateCurrentStepLabel();
			} else {
				moveTo(currentStep);
			}

			lastIncrementStep = currentStep;
		}

		private long compareTimeWithNextStep() {

			if (currentStep + 1 < allData.size()) {
				DateTime d1 = DateTime.parse(allData.get(currentStep).systemTime, dateFormatter);
				DateTime d2 = DateTime.parse(allData.get(currentStep + 1).systemTime, dateFormatter);
				return d2.getMillis() - d1.getMillis();
			}

			return 0;
		}
	}

	public static void main(String[] args) {
		new PositionPlot(RAW_LOGS_FOLDER);
	}
}
