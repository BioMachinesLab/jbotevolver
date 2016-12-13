package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import logManaging.GPSLogFilesParser;
import logManaging.LogFilesMerger;
import logManaging.ValuesLogFilesParser;

public class PositionPlot extends JFrame {
	private final boolean DEBUG = false;
	private final String RAW_LOGS_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\logs";
	private final String MERGED_LOGS_FOLDER = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs";

	private final String PARSED_DATA_FILE_GPS = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs\\mergedLogs_gps.log";
	private final String PARSED_DATA_FILE_ENTITIES = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs\\mergedLogs_entities.log";
	private final String PARSED_DATA_FILE_LOG = "C:\\Users\\BIOMACHINES\\Desktop\\mergedLogs\\mergedLogs_logData.log";

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
	private JComboBox<String> availableRobotLogsCombobox;
	private PlayerThread playerThread;

	// Data parsers and loaders
	private enum DataSource {
		GPS, ENTITIES, LOG;
	}

	private DataSource dataSource;
	private HashMap<Integer, ArrayList<GPSData>> gpsData = null;
	private HashMap<Integer, ArrayList<EntityManipulation>> entitiesManipulationData = null;
	private HashMap<Integer, ArrayList<DecodedLog>> decodedLogData = null;

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

			if (toOpenFolder == null || !toOpenFolder.exists() || !toOpenFolder.isDirectory()
					|| !processOriginalLogFiles()) {
				System.err.printf("[%s] Unable to load data from RAW files!%n", getClass().getSimpleName());
				System.exit(1);
			}
		} else {
			File gpsDataFile = new File(PARSED_DATA_FILE_GPS);
			File entitiesDataFile = new File(PARSED_DATA_FILE_ENTITIES);
			File logDataFile = new File(PARSED_DATA_FILE_LOG);

			if (!gpsDataFile.exists() || !entitiesDataFile.exists() || !logDataFile.exists()) {
				File toOpenFolder = selectFolderToOpen();

				if (toOpenFolder == null || !toOpenFolder.exists() || !toOpenFolder.isDirectory()
						|| !processOriginalLogFiles()) {
					System.err.printf("[%s] Unable to load data from RAW files!%n", getClass().getSimpleName());
					System.exit(1);
				}
			}
		}

		if (!DEBUG) {
			try {
				EntitiesLogFilesParser entitiesLogFilesParser = new EntitiesLogFilesParser(MERGED_LOGS_FOLDER, false);
				entitiesManipulationData = entitiesLogFilesParser.getEntitiesManipulationData();
				dataSource = DataSource.ENTITIES;
			} catch (FileNotFoundException | FileSystemException e) {
				e.printStackTrace();
			}
		}

		if (DEBUG || entitiesManipulationData != null) {
			buildGUI();
			playPauseButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					playerThread.togglePlayStatus();
				}
			});
			replayButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					playerThread.replay();
				}
			});

			playerThread = new PlayerThread();
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
		playPauseButton = new JButton("Play");
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
				// currentStep = playerSlider.getValue();
				// playerSlider.setToolTipText("" + playerSlider.getValue());
				// if (!playerThread.isPlaying())
				// moveTo(playerSlider.getValue());
			}
		});
		centralWrapperPanel.add(playerSlider, BorderLayout.CENTER);

		JPanel logLinePanel = new JPanel(new SpringLayout());
		logLineTextField = new JTextField("0", 5);
		logLineTextField.setEditable(false);
		logLineTextField.setHorizontalAlignment(JTextField.CENTER);

		logLinePanel.add(new JLabel("Log line:"));
		logLinePanel.add(logLineTextField);
		SpringUtilities.makeGrid(logLinePanel, 1, 2, 0, 0, 5, 0);
		centralWrapperPanel.add(logLinePanel, BorderLayout.EAST);

		// Left part with combo box that selects the data to load
		JPanel leftControlPanel = new JPanel(new SpringLayout());
		availableRobotLogsCombobox = new JComboBox<String>(getRobotLogsLabels());
		availableRobotLogsCombobox.setPreferredSize(replayButton.getPreferredSize());
		availableRobotLogsCombobox.setEditable(false);
		((JLabel) availableRobotLogsCombobox.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
		availableRobotLogsCombobox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateSliderLabels();
			}
		});
		if (availableRobotLogsCombobox.getItemCount() == 0) {
			availableRobotLogsCombobox.setEnabled(false);
		} else {
			availableRobotLogsCombobox.setSelectedItem(0);
			updateSliderLabels();
		}

		leftControlPanel.add(new JLabel("Robot:"));
		leftControlPanel.add(availableRobotLogsCombobox);
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
		default:
			return new String[] {};
		}
	}

	private void updateSliderLabels() {
		int robot = Integer.parseInt((String) availableRobotLogsCombobox.getSelectedItem());
		int maximum = 0;

		switch (dataSource) {
		case GPS:
			maximum = gpsData.get(robot).size();
			break;
		case ENTITIES:
			maximum = entitiesManipulationData.get(robot).size();
			break;
		case LOG:
			maximum = decodedLogData.get(robot).size();
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
	private boolean processOriginalLogFiles() {
		try {
			System.out.printf("[%s] ######################## Merging logs%n", getClass().getSimpleName());
			new LogFilesMerger(RAW_LOGS_FOLDER, MERGED_LOGS_FOLDER);
		} catch (FileNotFoundException e) {
			System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
			return false;
		} catch (FileAlreadyExistsException e) {
			System.out.printf("[%s] %s%n", getClass().getSimpleName(), e.getMessage());
		} catch (FileSystemException e) {
			System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
		}

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}

		try {
			/*
			 * Due to problems with GC and since it is rare the need to process
			 * RAW log files, after each parsing we write the objects to a file
			 * And release all the used resources
			 */
			if (!new File(PARSED_DATA_FILE_GPS).exists()) {
				System.out.printf("[%s] ######################## Parsing GPS logs%n", getClass().getSimpleName());
				GPSLogFilesParser gpsLogFilesParser = new GPSLogFilesParser(MERGED_LOGS_FOLDER);
				ExperiencesDataOnFile data_gps = new ExperiencesDataOnFile();
				data_gps.setGPSData(gpsLogFilesParser.getGPSData());
				if (!saveDataToFile(data_gps, PARSED_DATA_FILE_GPS, true)) {
					System.err.printf("[%s] Error writing GPS data to file%n", getClass().getSimpleName());
				} else {
					// Release everything so GC finds space in memory
					gpsLogFilesParser = null;
					data_gps = null;
					System.gc();
				}
			} else {
				System.out.printf("[%s] ######################## GPS logs already parsed%n",
						getClass().getSimpleName());
			}

			if (!new File(PARSED_DATA_FILE_ENTITIES).exists()) {
				System.out.printf("[%s] ######################## Parsing entities logs%n", getClass().getSimpleName());
				EntitiesLogFilesParser entitiesLogFilesParser = new EntitiesLogFilesParser(MERGED_LOGS_FOLDER, true);
				ExperiencesDataOnFile data_entities = new ExperiencesDataOnFile();
				data_entities.setEntitiesManipulationData(entitiesLogFilesParser.getEntitiesManipulationData());
				if (!saveDataToFile(data_entities, PARSED_DATA_FILE_ENTITIES, true)) {
					System.err.printf("[%s] Error writing entities manipulation data to file%n",
							getClass().getSimpleName());
					return false;
				} else {
					// Release everything so GC finds space in memory
					entitiesLogFilesParser = null;
					data_entities = null;
					System.gc();
				}
			} else {
				System.out.printf("[%s] ######################## Entites logs already parsed%n",
						getClass().getSimpleName());
			}

			if (!new File(PARSED_DATA_FILE_LOG).exists()) {
				System.out.printf("[%s] ######################## Parsing values logs%n", getClass().getSimpleName());
				ValuesLogFilesParser valuesLogFilesParser = new ValuesLogFilesParser(MERGED_LOGS_FOLDER, true);
				ExperiencesDataOnFile data_logs = new ExperiencesDataOnFile();
				data_logs.setDecodedLogData(valuesLogFilesParser.getDecodedLogData());
				if (!saveDataToFile(data_logs, PARSED_DATA_FILE_LOG, true)) {
					System.err.printf("[%s] Error writing decoded log data to file%n", getClass().getSimpleName());
					return false;
				} else {
					// Release everything so GC finds space in memory
					valuesLogFilesParser = null;
					data_logs = null;
					System.gc();
				}
			} else {
				System.out.printf("[%s] ######################## Values logs already parsed%n",
						getClass().getSimpleName());
			}
		} catch (FileNotFoundException e) {
			System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
			return false;
		} catch (FileSystemException e) {
			System.err.printf("[%s] File system error! %s%n", getClass().getSimpleName(), e.getMessage());
			return false;
		}

		return true;
	}

	private static ExperiencesDataOnFile loadDataFromParsedFile(String file) {
		File inputFile = new File(file);
		if (inputFile.exists()) {
			FileInputStream fin = null;
			ObjectInputStream ois = null;

			System.out.printf("[%s] Loading data from file %s%n", PositionPlot.class.getSimpleName(), file);
			try {
				fin = new FileInputStream(inputFile);
				ois = new ObjectInputStream(fin);

				Object obj = ois.readObject();

				if (obj instanceof ExperiencesDataOnFile) {
					return (ExperiencesDataOnFile) obj;
				} else {
					return null;
				}
			} catch (IOException | ClassNotFoundException e) {
				System.err.printf("[%s] Error reading object from file! %s%n", PositionPlot.class.getSimpleName(),
						e.getMessage());
				return null;
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
						System.err.printf("[%s] Error closing file input stream %s%n",
								PositionPlot.class.getSimpleName(), e.getMessage());
					}
				}

				if (ois != null) {
					try {
						ois.close();
					} catch (IOException e) {
						System.err.printf("[%s] Error closing object input stream %s%n",
								PositionPlot.class.getSimpleName(), e.getMessage());
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(null, "Data file does not exist", "Error reading file!",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	private static boolean saveDataToFile(ExperiencesDataOnFile data, String file, boolean askoverride) {
		File outputFile = new File(file);
		if (askoverride && outputFile.exists()) {
			int result = JOptionPane.showConfirmDialog(null, "Output file already exists. Override?", "Question",
					JOptionPane.OK_CANCEL_OPTION);

			if (result != JOptionPane.OK_OPTION) {
				return false;
			}
		}

		FileOutputStream fout = null;
		ObjectOutputStream oos = null;
		boolean toReturn = true;

		System.out.printf("[%s] Saving data to file %s%n", PositionPlot.class.getSimpleName(), file);
		try {
			fout = new FileOutputStream(outputFile);
			oos = new ObjectOutputStream(fout);

			oos.writeObject(data);
		} catch (IOException e) {
			System.err.printf("[%s] Error writting object to file! %s%n", PositionPlot.class.getSimpleName(),
					e.getMessage());
			toReturn = false;
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					System.err.printf("[%s] Error closing file output stream %s%n", PositionPlot.class.getSimpleName(),
							e.getMessage());
				}
			}

			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					System.err.printf("[%s] Error closing object output stream %s%n",
							PositionPlot.class.getSimpleName(), e.getMessage());
				}
			}
		}

		return toReturn;
	}

	private class ExperiencesDataOnFile implements Serializable {
		private static final long serialVersionUID = -8244038888675417327L;
		private HashMap<Integer, ArrayList<GPSData>> gpsData;
		private HashMap<Integer, ArrayList<EntityManipulation>> entitiesManipulationData;
		private HashMap<Integer, ArrayList<DecodedLog>> decodedLogData;

		public ExperiencesDataOnFile() {
			this.gpsData = null;
			this.entitiesManipulationData = null;
			this.decodedLogData = null;
		}

		public void setGPSData(HashMap<Integer, ArrayList<GPSData>> gpsData) {
			this.gpsData = gpsData;
		}

		public HashMap<Integer, ArrayList<GPSData>> getGPSData() {
			return gpsData;
		}

		public void setEntitiesManipulationData(
				HashMap<Integer, ArrayList<EntityManipulation>> entitiesManipulationData) {
			this.entitiesManipulationData = entitiesManipulationData;
		}

		public HashMap<Integer, ArrayList<EntityManipulation>> getEntitiesManipulationData() {
			return entitiesManipulationData;
		}

		public void setDecodedLogData(HashMap<Integer, ArrayList<DecodedLog>> decodedLogData) {
			this.decodedLogData = decodedLogData;
		}

		public HashMap<Integer, ArrayList<DecodedLog>> getDecodedLogData() {
			return decodedLogData;
		};
	}

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
		private boolean exit = false;
		private int currentStep = -1;
		private double multiplier = 1.0;

		@Override
		public void run() {
			while (!exit) {
				try {
					synchronized (this) {
						if (!play)
							wait();
					}

					incrementPlay();

					long time = (long) (compareTimeWithNextStep() * multiplier);

					if (time > 1000)
						time = 1000;

					Thread.sleep(time);
				} catch (Exception e) {
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
			// TODO
		}

		public void togglePlayStatus() {
			if (play)
				pause();
			else
				play();
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

		public void stopThread() {
			exit = true;
			interrupt();
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
		new PositionPlot();
	}
}
