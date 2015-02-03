package gui.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import simulation.JBotSim;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import taskexecutor.TaskExecutor;
import controllers.Controller;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.populations.Population;
import gui.Gui;
import gui.renderer.Renderer;

public class ConfigurationGui extends Gui{
	
	private static final long serialVersionUID = -5613912585135405020L;
	private static final int OPTIONS_GRID_LAYOUT_SIZE = 15;
	
	private String[] keys =
		{"output","robots", "controllers", "population", "environment",
			"executor","evolution", "evaluation", "random-seed"};

	private JPanel configurationPanelLeft;
	private JPanel optionsPanelCenter;
	private JPanel optionsPanelRight;
	
	private JPanel rendererPanel;
	
	private JTextPane configResult;
	private JTextArea helpArea;
	
	private JButton optionsButton;
	private JButton saveArgumentsFileButton;
	private JButton runEvolution;
	private JButton testArgumentsFileButton;
	
	private JButton loadArgumentsButton;
	
	private JComboBox<String> currentComboBox;

	private ArrayList<AutomatorOptionsAttribute> optionsAttributes;
	private HashMap<String, Component> argumentsComponents;
	
	private DefaultListModel<String> sensorsActuatorsListModel;
	private JList<String> sensorsActuatorsList;

	private String currentOptions;
	private String currentClassName;
	
	private String editedAttributeName;
	
	private RobotsResult robotConfig;
	private ConfigurationResult result;
	
	private Arguments rendererArgs;

	private ArrayList<String> blackList;
	
	private boolean showPreview = true;
	
	private String extraArgs = "";
	
	public ConfigurationGui(JBotSim jBotSim, Arguments args) {
		super(jBotSim,args);
		init();
	}
	
	public ConfigurationGui(JBotSim jBotSim, Arguments args, String[] keys) {
		super(jBotSim,args);
		this.keys = keys;
		init();
	}
	
	public void init() {
		optionsAttributes = new ArrayList<AutomatorOptionsAttribute>();
		argumentsComponents = new HashMap<String, Component>();
		
		blackList = new ArrayList<String>();
		
		fillBlackList();
		
		robotConfig = new RobotsResult();
		result = new ConfigurationResult(keys);
		
		setLayout(new BorderLayout());
		add(initResultPanel(), BorderLayout.EAST);
		add(initSelectionPanel(), BorderLayout.WEST);
		add(initConfigurationPanel(), BorderLayout.CENTER);
		
		initListeners();

		setSize(1100, 800);
		setVisible(true);
	}

	private void fillBlackList() {
		/*
		blackList.add("ConillonTaskExecutor");
		blackList.add("CompassSensor");
		blackList.add("ConeTypeSensor");
		blackList.add("DoubleParameterSensor");
		blackList.add("EpuckIRSensor");
		blackList.add("EpuckLightSensor");
		blackList.add("LightTypeSensor");
		blackList.add("NearRobotSensor");
		blackList.add("NestSensor");
		blackList.add("PerimeterSimpleLightTypeSensor");
		blackList.add("PerimeterSimpleRobotColorSensor");
		blackList.add("PositionSensor");
		blackList.add("PreySensor");
		blackList.add("RobotColorSensor");
		blackList.add("RobotRGBColorSensor");
		blackList.add("RobotSensor");
		blackList.add("Sensor");
		blackList.add("SimpleMouseSensor");
		blackList.add("SimpleNestSensorPerimeter");
		blackList.add("SimplePreySensorPerimeter");
		blackList.add("SimpleTeamNestSensor");
		blackList.add("WallButtonSensor");
		blackList.add("WallSensor");
		blackList.add("WaypointSensor");
		blackList.add("XRayPreySensor");
		blackList.add("OpenDoorActuator");
		blackList.add("RobotColorActuator");
		blackList.add("RobotRGBColorActuator");
		blackList.add("StopActuator");
		blackList.add("Epuck");
		blackList.add("BehaviorController");
		blackList.add("MulitlayerPerceptron");
		blackList.add("DoNothingController");
		blackList.add("DummyController");
		blackList.add("KeyboardController");
		blackList.add("RandomRobotController");
		blackList.add("DestructiveBehavior");
		blackList.add("DummyBehavior");
		blackList.add("MoveForwardBehavior");
		blackList.add("OpenDoorBehavior");
		blackList.add("TurnLeftBehavior");
		blackList.add("TurnRightBehavior");
		blackList.add("ProgrammedForager");
		blackList.add("ClutteredMazeEnvironment");
		blackList.add("GPSEnvironment");
		blackList.add("GroupedPreyEnvironment");
		blackList.add("RoundPreyForageEnvironment");
		blackList.add("RoundRobotForageEnvironment");
		blackList.add("TMazeEnvironment");
		blackList.add("TwoNestCenterDeployPreysEnvironment");
		blackList.add("TwoNestForageEnvironment");
		blackList.add("TwoRoomsEnvironment");
		blackList.add("AverageDistanceFromCenterEvaluationFunction");
		blackList.add("CenterOfMassAndClustersEvaluationFunction");
		blackList.add("ClutteredMazeEvaluationFunction");
		blackList.add("CollaboratingTwoNetsForagingEvaluationFunction");
		blackList.add("ColorMatchEvaluationFunction");
		blackList.add("ColorNearPreyEvaluationFunction");
		blackList.add("CommunicationEvaluationFunction");
		blackList.add("CompetingTwoNetsForagingEvaluationFunction");
		blackList.add("CrossRoomsEvaluationFunction");
		blackList.add("EmptyEvaluationFunction");
		blackList.add("ERSimbadForagingEvaluationFunction");
		blackList.add("FindWallButtonEvaluationFunction");
		blackList.add("FoodWaterEvaluationFunction");
		blackList.add("GoToButtonEvaluationFunction");
		blackList.add("GoToWaypointEvaluationFunction");
		blackList.add("OpenDoorEvaluationFunction");
		blackList.add("PickAndDropsEvaluationFunction");
		blackList.add("PreyAggregationEvaluationFunction");
		blackList.add("PreyCatchEvaluationFunction");
		blackList.add("PreyEscapeEvaluationFunction");
		blackList.add("RoomMazeBackBehaviors2EvaluationFunction");
		blackList.add("RoomMazeBackBehaviorsEvaluationFunction");
		blackList.add("RoomMazeBackEvaluationFunction");
		blackList.add("StepsWithPreyEvaluationFunction");
		blackList.add("TMazeEvaluationFunction");
		blackList.add("TwoRoomsEvaluationFunction");
		blackList.add("CoEvolution");
		blackList.add("SingleSampleGenerationalEvolution");
		blackList.add("SimpleRobotColorSensor");
		*/
	}

	private Component initSelectionPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel argumentsWrapper = new JPanel();
		argumentsWrapper.setLayout(new BorderLayout());
				
		JPanel north = new JPanel(new BorderLayout());
		JPanel subNorth = new JPanel(new BorderLayout());
		
		JPanel subPanelNorth = new JPanel(new GridLayout(1,1));
		subPanelNorth.add(initOutputWrapperPanel()); // 1. Output
		subNorth.add(subPanelNorth, BorderLayout.NORTH);

		JPanel subPanelNorth2 = new JPanel(new GridLayout(1,1));
		subPanelNorth2.add(createJComboBoxPanel(new Class<?>[]{Robot.class, Sensor.class, Actuator.class})); // 2. Robots
		subNorth.add(subPanelNorth2);
		
		JPanel subPanelNorth3 = new JPanel(new GridLayout(1,1));
		subPanelNorth3.add(createJComboBoxPanel(new Class<?>[]{Controller.class, NeuralNetwork.class})); // 3. Controllers
		subNorth.add(subPanelNorth3, BorderLayout.SOUTH);
		
		north.add(subNorth, BorderLayout.NORTH);
		north.add(createJComboBoxPanel(new Class<?>[]{Population.class, Environment.class, TaskExecutor.class, Evolution.class, EvaluationFunction.class}));
		
		argumentsWrapper.add(north, BorderLayout.NORTH);
		argumentsWrapper.setBorder(BorderFactory.createTitledBorder("Arguments"));

		panel.add(argumentsWrapper, BorderLayout.NORTH);
		
		JPanel jListContentWrapper = new JPanel(new BorderLayout());
		sensorsActuatorsListModel = new DefaultListModel<String>();
		sensorsActuatorsList = new JList<String>(sensorsActuatorsListModel);
		
		JPanel buttonsPanel = new JPanel();
		
		JButton jListRemoveButton = new JButton("Remove");
		
		jListRemoveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeElementFromSensorsActuatorsList();
				cleanOptionsPanel();
			}
		});
		
		loadArgumentsButton = new JButton("Load File");
		loadArgumentsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadArgumentsFromFile();
			}
		});
		
		buttonsPanel.setPreferredSize(new Dimension(300,50));
		buttonsPanel.add(jListRemoveButton);
		buttonsPanel.add(loadArgumentsButton);
		
		jListContentWrapper.add(new JScrollPane(sensorsActuatorsList));
		jListContentWrapper.add(buttonsPanel, BorderLayout.SOUTH);
		jListContentWrapper.setBorder(BorderFactory.createTitledBorder("Attributes List"));
		panel.add(jListContentWrapper, BorderLayout.CENTER);
		
		panel.setPreferredSize(new Dimension(300,300));
		
		return panel;
	}
	
	private Component initResultPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Result"));
		panel.setPreferredSize(new Dimension(370,350));
		panel.setLayout(new BorderLayout());
		
		JPanel resultContentWrapper = new JPanel(new BorderLayout());
		resultContentWrapper.setBorder(BorderFactory.createTitledBorder("Configuration"));
		
		configResult = new JTextPane();
		configResult.setContentType("text/html");
		
		DefaultCaret caret = (DefaultCaret) configResult.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		configResult.setEditable(false);
		updateConfigurationText();
		JScrollPane scroll = new JScrollPane(configResult);
		resultContentWrapper.add(scroll, BorderLayout.CENTER);
		
		panel.add(resultContentWrapper, BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel(new BorderLayout());
		
		rendererPanel = new JPanel(new BorderLayout());
		rendererPanel.setPreferredSize(new Dimension(370, 370));
		rendererPanel.setMinimumSize(new Dimension(370, 370));
		rendererPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
		southPanel.add(rendererPanel, BorderLayout.NORTH);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setPreferredSize(new Dimension(370,50));
		buttonPanel.add(createResultContentButtons());
		southPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		panel.add(southPanel, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private Component initConfigurationPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(410,getHeight()));
		panel.setLayout(new BorderLayout());
		
		configurationPanelLeft = new JPanel();
		optionsPanelCenter = new JPanel();
		optionsPanelRight = new JPanel();
		
		optionsButton = new JButton("Add to File");
		optionsButton.setVisible(false);
		
		JPanel borderLayout = new JPanel(new BorderLayout());
		
		JPanel splitLayout = new JPanel(new BorderLayout());
		splitLayout.add(configurationPanelLeft, BorderLayout.CENTER);
		
		JPanel panelRight = new JPanel(new BorderLayout());
		
		panelRight.add(optionsPanelCenter, BorderLayout.CENTER);
		panelRight.add(optionsPanelRight, BorderLayout.EAST);
		
		splitLayout.add(panelRight, BorderLayout.EAST);
		
		JScrollPane scroll = new JScrollPane(splitLayout,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createLineBorder(new Color(238,238,238), 2));
		borderLayout.add(scroll, BorderLayout.CENTER);
		
		helpArea = new JTextArea();
		helpArea.setWrapStyleWord(true);
		helpArea.setLineWrap(true);
		helpArea.setEditable(false);
		helpArea.setBorder(BorderFactory.createTitledBorder("Help"));
		helpArea.setBackground(new Color(238,238,238));
		helpArea.setPreferredSize(new Dimension(borderLayout.getWidth(), 60));
		borderLayout.add(helpArea,BorderLayout.SOUTH);
		
		panel.add(borderLayout, BorderLayout.CENTER);
		panel.add(optionsButton, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createTitledBorder("Options"));
		
		return panel;
	}

	private Component initOutputWrapperPanel() {
		JPanel outputWrapper = new JPanel();
		outputWrapper.setLayout(new GridLayout(2,1));
		
		JTextField outputTextField = new JTextField(10);
		JTextField randomSeedTextField = new JTextField(10);
		
		outputTextField.getDocument().addDocumentListener(new TextFieldListener(outputTextField, "output"));
		randomSeedTextField.getDocument().addDocumentListener(new TextFieldListener(randomSeedTextField, "random-seed"));
		
		argumentsComponents.put("output", outputTextField);
		argumentsComponents.put("random-seed", randomSeedTextField);
		
		outputWrapper.add(new JLabel("Output Name: "));
		outputWrapper.add(outputTextField);
		outputWrapper.add(new JLabel("Random Seed: "));
		outputWrapper.add(randomSeedTextField);

		outputWrapper.setBorder(BorderFactory.createTitledBorder(""));
		return outputWrapper;
	}

	private Component createJComboBoxPanel(Class<?>[] classNames) {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(classNames.length,1));
		
		LinkedList<String> nameExceptions = new LinkedList<String>(Arrays.asList(new String[]{"controller", "robot", "actuator", "sensor"}));
		
		for(Class<?> c : classNames) {
			JComboBox<String> currentBox = loadClassNamesToComboBox(c);
			
			String configName = c.getSimpleName().toLowerCase();
			configName = configName.replace("neuralnetwork", "network");
			configName = configName.replace("taskexecutor", "executor");
			configName = configName.replace("evaluationfunction", "evaluation");
			configName = nameExceptions.contains(configName) ? configName+"s" : configName;
			
			currentBox.addActionListener(new OptionsAtributesListener(c, configName));
			panel.add(new JLabel(c.getSimpleName()+": "));
			panel.add(currentBox);
			argumentsComponents.put(configName, currentBox);
		}
		
		panel.setBorder(BorderFactory.createTitledBorder(""));
		return panel;
	}
	
	private void initListeners() {

		optionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getOptionsFromPanel();
			}
		});
		
		sensorsActuatorsList.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent event) { }
			public void mousePressed(MouseEvent event) { }
			public void mouseExited(MouseEvent event) { }
			public void mouseEntered(MouseEvent event) { }
			public void mouseClicked(MouseEvent event) {
				editOptionPanelAttribute();
				seeSensors();
			}
		});
		
		saveArgumentsFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(!correctConfiguration()) {
					return;
				}
				createArgumentFile();
			}
		});
		
		testArgumentsFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(!correctConfiguration()) {
					return;
				}
				String output = "test_" + new Random().nextInt();
				
				try {
					String[] transformedArgs = Arguments.readOptionsFromString(result.toString());
					HashMap<String,Arguments> argumentsHash = Arguments.parseArgs(transformedArgs);
					
					argumentsHash.put("--output", new Arguments(output));
					
					Arguments environmentArgs = argumentsHash.get("--environment");
					environmentArgs.setArgument("steps", 5);
					argumentsHash.put("--environment", environmentArgs);
					
					Arguments populationArgs = argumentsHash.get("--population");
					populationArgs.setArgument("size", 50);
					populationArgs.setArgument("samples", 1);
					populationArgs.setArgument("generations", 1);
					argumentsHash.put("--population", populationArgs);
					
					String[] finalArgs = Arguments.readOptionsFromString(createConfigFileStringFromHash(argumentsHash));
					
					JBotEvolver jBotEvolver = new JBotEvolver(finalArgs);
					TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
					taskExecutor.start();
					Evolution evo = Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
					evo.executeEvolution();
					taskExecutor.stopTasks();
					
					JOptionPane.showMessageDialog(ConfigurationGui.this, "OK");
					
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(ConfigurationGui.this, "ERROR " + e.getStackTrace() );
				}finally{
					File f = new File(output);
					deleteDirectory(f);
				}
				
			}
			
			private String createConfigFileStringFromHash(HashMap<String,Arguments> argumentsHash) {
				String finalArgs = "";
				for (String key : argumentsHash.keySet()) {
					finalArgs += key + " " + argumentsHash.get(key) + "\n";
				}
				Arguments.beautifyString(finalArgs);
				return finalArgs;
			}
			
			private void deleteDirectory(File f) {
				for (File subFile : f.listFiles()) {
					if(subFile.isDirectory()){
						deleteDirectory(subFile);
					}else{
						subFile.delete();
					}
				}
				f.delete();  
			}
			
		});
		
		runEvolution.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				PrintWriter printWriter = null;
				String outputText = result.getArgument("output").getCompleteArgumentString().trim();
				
				if(!correctConfiguration()) {
					return;
				}
				
				try {
					printWriter = new PrintWriter(new File(outputText + ".conf"));
					printWriter.write(result.toString()+extraArgs);
					printWriter.close ();
					
//					configurationAutomator.startEvolution(outputText);
					wakeUpWaitingThreads();
			    } catch (FileNotFoundException e) {
			    	e.printStackTrace();
			    } finally {
			    	printWriter.close ();
			    }
			}
		});
	}
	
	private boolean correctConfiguration() {
		
		String msg = "";
		
		for(String k : keys) {
			if(result.getArgument(k).getNumberOfArguments() == 0) {
				String argMsg = "Missing argument \"--"+k+"\""; 
				msg+= msg.isEmpty() ? argMsg : "\n" + argMsg; 
			}
		}
		
		if(!msg.isEmpty()) {
			JOptionPane.showConfirmDialog(this, msg);
			return false;
		}
		
		return true;
	}

	private void updateConfigurationText() {
		configResult.setText(result.toHTMLString(extraArgs));
//		for(String s:keys){
//			tree.setArgumet(s,result.getArgument(s));
//		}
	}
	
	private JPanel createResultContentButtons() {
		JPanel panel = new JPanel();
		saveArgumentsFileButton = new JButton("Save to File");
		panel.add(saveArgumentsFileButton);
		testArgumentsFileButton = new JButton("Test File");
		panel.add(testArgumentsFileButton);
		runEvolution = new JButton("Run Evolution");
		panel.add(runEvolution);
		return panel;
	}
	
	private void seeSensors() {
		try {
			showPreview();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showPreview() throws Exception{
		if(!showPreview)
			return;
		
		Renderer renderer = setupRenderer();
		
		rendererPanel.removeAll();
		
		if (renderer != null) {
			renderer.addMouseListener(new MouseListener() {
				public void mouseReleased(MouseEvent e) {}
				public void mousePressed(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseClicked(MouseEvent e) {
					try {
						showPreview();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			
			rendererPanel.add(renderer);
			rendererPanel.revalidate();
			rendererPanel.repaint();
			
			revalidate();
			repaint();
		}
	}
	
	private Renderer setupRenderer() {
		
		String extraRendererArguments = "";
		
		String selectedID = sensorsActuatorsList.getSelectedValue();
		
		if(selectedID != null && selectedID.contains("Sensor")) {
			extraRendererArguments = ",conesensorid=";
			String id = robotConfig.getSensorActuator(selectedID).getArgumentAsString("id");
			extraRendererArguments += id;
		}
		
		rendererArgs = new Arguments("classname=TwoDRendererDebug" +extraRendererArguments,true);
		
		String robotArgs = "";
		if(result.getArgument("robots").getNumberOfArguments() > 0) {
			robotArgs = "--robots " + result.getArgument("robots") + "\n";
		}
		
		String environmentArgs = "";
		if(result.getArgument("environment").getNumberOfArguments() > 0) {
			environmentArgs = "--environment " + result.getArgument("environment");
		} else {
			environmentArgs = "--environment classname=EmptyEnvironment,width=4,height=4";
		}
		
		try {
			Renderer renderer = Renderer.getRenderer(rendererArgs);
			
			String[] args = Arguments.readOptionsFromString(robotArgs + environmentArgs );
			
			HashMap<String, Arguments> arguments = Arguments.parseArgs(args);
			
			Simulator simulator = new Simulator(new Random(), arguments);
			
			if(!robotArgs.isEmpty()) {
				ArrayList<Robot> robots = Robot.getRobots(simulator, arguments.get("--robots"));
				simulator.addRobots(robots);
			}
			
			if(!environmentArgs.isEmpty()) {
				simulator.setupEnvironment();
			}
			
			renderer.enableInputMethods(true);
			renderer.setSimulator(simulator);
			renderer.drawFrame();
			
			return renderer;
		
		} catch(Exception e) {
			e.printStackTrace();
			//Gotta catch 'em all! Silence the exceptions that might occur because of problems in the
			//initialization of Environments
		}
		
		return null;
	}

	private void createArgumentFile() {
		
		String outputText = result.getArgument("output").getCompleteArgumentString().trim();
		
		if(outputText.length() > 0){
			JFileChooser fileChooser = new JFileChooser(".");
			fileChooser.setSelectedFile(new File(outputText + ".conf"));
			fileChooser.setDialogTitle("Save File");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				PrintWriter printWriter = null;
				try {
					String filePath = fileChooser.getSelectedFile().getAbsolutePath();
					
					printWriter = new PrintWriter(new File(filePath));
					printWriter.write(result.toString());
			    	JOptionPane.showMessageDialog(this, "File " + outputText + ".conf, created!");
			    } catch (FileNotFoundException e) {
			    	e.printStackTrace();
			    } finally {
			    	printWriter.close ();
			    }
			}
		}else{
			JOptionPane.showMessageDialog(this, "No Output Name defined.");
		}
	}
	
	private void loadArgumentsFromFile() {
		showPreview = false;
		
		File file = chooseFile();
		if(file != null){
			Scanner scanner = null;
			try {
				scanner = new Scanner(file);
				String loadedFileArgs = "";
				boolean first = true;
				while(scanner.hasNext()){
					String line = scanner.nextLine();
					if(line.startsWith("--") && !first){
						loadedFileArgs += "\n";
					}
					first=false;
					loadedFileArgs += line;
				}
				scanner.close();
				String[] transformedArgs = Arguments.readOptionsFromString(loadedFileArgs);
				clear();
				loadArgumentsToGui(transformedArgs);

				cleanOptionsPanel();
				showPreview = true;
				seeSensors();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}finally{
				if(scanner != null)
					scanner.close();
			}
		}
	}
	
	private void clear(){
		while(!sensorsActuatorsListModel.isEmpty()){
			robotConfig.remove(sensorsActuatorsListModel.get(0));
			updateSensorsActuatorsList();
			updateConfigurationText();
		}

		result.setArgument("robots", robotConfig.getCompleteArguments());
		
		for (String key : argumentsComponents.keySet()) {
			Component component = argumentsComponents.get(key);
			if(component instanceof JComboBox){
				JComboBox<String> comboBox = (JComboBox<String>) component;
				comboBox.setSelectedIndex(0);
			}else if(component instanceof JTextField){
				if(key.equals("random-seed"))
					continue;
				JTextField textfield = (JTextField) component;
				textfield.setText("");
			}
		}
		rendererPanel.removeAll();
		extraArgs = "";
	}
	
	private File chooseFile() {
		File file = null;
		
		JFileChooser fileChooser = new JFileChooser(".");
		fileChooser.setDialogTitle("Load File");
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			file = new File(fileChooser.getSelectedFile().getAbsolutePath());
			
		}
		return file;
	}
	
	private void loadArgumentsToGui(String[] args) throws ClassNotFoundException, IOException {
		HashMap<String,Arguments> argumentsHash = Arguments.parseArgs(args);

		for (String key : argumentsHash.keySet()) {
			
			String correctKey = key.replace("--", "");
			Arguments completeArguments = argumentsHash.get(key);
			String className = "";
			if(correctKey.equals("output")){
				className = completeArguments.getArguments().get(0);
			}else if(correctKey.equals("random-seed")){
				className = completeArguments.getArguments().get(0);;
			}else{
				className = completeArguments.getArgumentAsString("classname");
				if(className == null) {
					extraArgs+=key+=" "+argumentsHash.get(key).getCompleteArgumentString()+"\n";
					continue;
				}
				className = className.split("\\.")[className.split("\\.").length-1];
				
				specialAddToConfigFile(correctKey, completeArguments, className);
			}
			setLoadedArgToComponent(correctKey, className);
		}
	}

	private void specialAddToConfigFile(String key, Arguments arguments,String className) {
		if(key.equals("robots")){
			Arguments sensorArguments = new Arguments(arguments.getArgumentAsString("sensors"));
			Arguments actuatorsArguments = new Arguments(arguments.getArgumentAsString("actuators"));
			
			arguments.removeArgument("sensors");
			arguments.removeArgument("actuators");
			addLoadedArgumentsToConfigFile(key,arguments);
			
			for (int i = 0; i < sensorArguments.getArguments().size(); i++) {
				currentClassName = new Arguments(sensorArguments.getValues().get(i)).getArgumentAsString("classname");
				currentClassName = currentClassName.split("\\.")[currentClassName.split("\\.").length-1];
				Arguments newSensorAgrs = new Arguments(sensorArguments.getValues().get(i));
				newSensorAgrs.removeArgument("id");
				currentComboBox = (JComboBox<String>) argumentsComponents.get("sensors");
				addLoadedArgumentsToConfigFile("sensors", newSensorAgrs);
			}
			
			for (int i = 0; i < actuatorsArguments.getArguments().size(); i++) {
				currentClassName = new Arguments(actuatorsArguments.getValues().get(i)).getArgumentAsString("classname");
				currentClassName = currentClassName.split("\\.")[currentClassName.split("\\.").length-1];
				Arguments newActuatorAgrs = new Arguments(actuatorsArguments.getValues().get(i));
				newActuatorAgrs.removeArgument("id");
				currentComboBox = (JComboBox<String>) argumentsComponents.get("actuators");
				addLoadedArgumentsToConfigFile("actuators", newActuatorAgrs);
			}
			
		}else if(key.equals("controllers")){
			Arguments networkArguments = new Arguments(arguments.getArgumentAsString("network"));
			
			arguments.removeArgument("network");
			addLoadedArgumentsToConfigFile(key,arguments);
			
			addLoadedArgumentsToConfigFile("network",networkArguments);
		}else{
			addLoadedArgumentsToConfigFile(key,arguments);
		}
	}

	private void addLoadedArgumentsToConfigFile(String argumentKey, Arguments arguments){
		currentOptions = argumentKey;
		writeAttributes(arguments.toString());
		
		updateSensorsActuatorsList();
		updateConfigurationText();
	}
	
	private void setLoadedArgToComponent(String key, String className){
		Component component = argumentsComponents.get(key);
		if(component instanceof JComboBox){
			JComboBox<String> comboBox = (JComboBox<String>) component;
			ComboBoxModel<String> comboBoxModel = comboBox.getModel();
			for (int i = 0; i < comboBoxModel.getSize(); i++) {
				if(comboBoxModel.getElementAt(i).equals(className)){
					comboBox.setSelectedIndex(i);
					break;
				}
			}
		}else if(component instanceof JTextField){
			JTextField textfield = (JTextField) component;
			if(key.equals("output")){
				textfield.setText(className);
				result.setArgument("output", new Arguments(textfield.getText()));
			}else if(key.equals("random-seed")){
				textfield.setText(className);
				result.setArgument("random-seed", new Arguments(textfield.getText()));
			}
			updateConfigurationText();
		}
	}
	
	private void removeElementFromSensorsActuatorsList() {
		String selectedID = sensorsActuatorsList.getSelectedValue();
		
		robotConfig.remove(selectedID);
		result.setArgument("robots", robotConfig.getCompleteArguments());
		
		updateSensorsActuatorsList();
		updateConfigurationText();
	}
	
	private JComboBox<String> loadClassNamesToComboBox(Class<?> className) {
		JComboBox<String> comboBox = new JComboBox<String>();
		ArrayList<Class<?>> aux = ClassLoadHelper.findClassesContainingName(className);
		
		comboBox.addItem("");
		
		boolean addClass = true;
		for (Class<?> cl : aux){
			String cls = cl.getName().split("\\.")[cl.getName().split("\\.").length-1];
			for (String blackListClass : blackList) {
				if(cls.equals(blackListClass))
					addClass = false;
			}
			
			if(addClass){
				comboBox.addItem(cl.getSimpleName());
			}else{
				addClass = true;
			}
			
		}
		
		return comboBox;
	}

	private void getOptionsFromPanel() {
		
		String arguments ="classname=" + currentClassName;
		for (AutomatorOptionsAttribute attribute : optionsAttributes) {
			if(!arguments.isEmpty())
				arguments+= createAttributeString(attribute, true);
		}
		
		writeAttributes(arguments);
		
		updateSensorsActuatorsList();
		
		updateConfigurationText();
		optionsAttributes.clear();
		
		cleanOptionsPanel();
		seeSensors();
	}
	
	private void updateSensorsActuatorsList() {
		sensorsActuatorsListModel.clear();
		for (String id : robotConfig.getSensorActuatorsIds()) {
			sensorsActuatorsListModel.addElement(id);
		}
	}
	
	private void writeAttributes(String arguments) {
		switch (currentOptions) {
			case "robots":
				robotConfig.setAttributes(arguments);
				result.setArgument("robots",robotConfig.getCompleteArguments());
				break;
			case "actuators":
				editedAttributeName = "";
				//no break on purpose
			case "sensors":
				robotConfig.addSensorActuator(editedAttributeName,currentClassName, arguments);
				editedAttributeName = "";
				currentComboBox.setSelectedIndex(0);
				result.setArgument("robots",robotConfig.getCompleteArguments());
				break;
			case "network":
				Arguments controllerArgs = result.getArgument("controllers");
				controllerArgs.setArgument("network", arguments);
				break;
			default:
				result.setArgument(currentOptions, new Arguments(arguments, false));
			break;
		}
	}

	private String createAttributeString(AutomatorOptionsAttribute attribute, boolean insertComma) {
		String result = "";
		if(attribute.getComponent() instanceof JTextField){
			JTextField textField = (JTextField)attribute.getComponent();
			if(attribute.getCheckBox().isSelected() && !textField.getText().isEmpty() && !attribute.getName().isEmpty()){
				if(insertComma)
					result = "," + attribute.getName() + "=" + textField.getText();
				else
					result = attribute.getName() + "=" + textField.getText();
			}
		}else if(attribute.getComponent() instanceof JComboBox<?>){
			JComboBox<?> comboBox = (JComboBox<?>) attribute.getComponent();
			String selection = (String)comboBox.getSelectedItem();
			if(attribute.getCheckBox().isSelected() && !selection.isEmpty() && !attribute.getName().isEmpty()){
				if(insertComma)
					result = "," + attribute.getName() + "=" + comboBox.getSelectedItem();
				else
					result = attribute.getName() + "=" + comboBox.getSelectedItem();
			}
		}else{
			System.err.println("Component received on the method createAttributeString not supported!");
		}
		
		return result;
	}

	private void populateOptionsPanel(ArrayList<Class<?>> classesList, String className, ArrayList<AutomatorOptionsAttribute> optAttributes) throws ClassNotFoundException {
		cleanOptionsPanel();
		
		Class<?> cls = null;
		for (Class<?> c : classesList) {
			if(c.getSimpleName().equals(className) || c.getName().equals(className)){
				cls = Class.forName(c.getName());
				break;
			}
		}
		
		ArrayList<ArgumentsAnnotation> annotations = addAnnotationsToList(cls.getDeclaredFields());
		
		Class<?> upperClass = cls.getSuperclass();
		boolean stop = false;
		while(!stop){
			if(upperClass.getSuperclass().equals(Object.class)){
				stop = true;
			}
			annotations.addAll(addAnnotationsToList(upperClass.getDeclaredFields()));
			upperClass = upperClass.getSuperclass();
		}
		
		int height = annotations.size() + 1 >= OPTIONS_GRID_LAYOUT_SIZE ? annotations.size() + 1 : OPTIONS_GRID_LAYOUT_SIZE;
		
		int spaceToFillGrid = OPTIONS_GRID_LAYOUT_SIZE - height;
		
		configurationPanelLeft.setLayout(new GridLayout(height,1));
		optionsPanelCenter.setLayout(new GridLayout(height,1));
		optionsPanelRight.setLayout(new GridLayout(height,1));
		
		String classNameSplit = currentClassName.split("\\.")[currentClassName.split("\\.").length-1];
		JTextField jtf = new JTextField(classNameSplit);
		jtf.setEnabled(false);
		JCheckBox jcb = new JCheckBox();
		jcb.setEnabled(false);
		jcb.setVisible(false);
		configurationPanelLeft.add(new JLabel("classname"));
		optionsPanelCenter.add(jtf);
		optionsPanelRight.add(jcb);
		
		
		for (ArgumentsAnnotation annotation : annotations) {
			
			String text = annotation.name();
			
			JLabel label = new JLabel(text);
			label.setToolTipText(text);
				
			if(!annotation.help().isEmpty())
				label.addMouseListener(new LabelMouseListener(annotation.help()));
			
			configurationPanelLeft.add(label);
				
			AutomatorOptionsAttribute attribute = new AutomatorOptionsAttribute();
			attribute.setName(annotation.name());
			String defaultValue = annotation.defaultValue();
			
			JTextField textField = null;
			JComboBox<String> combo = null;
			AutomatorOptionsAttribute attributeToEdit = null;
			
			if(optAttributes != null){
				for (AutomatorOptionsAttribute optAttribute : optAttributes) {
					if(optAttribute.getName().equals(annotation.name())){
						attributeToEdit = optAttribute;
						break;
					}
				}
			}
			
			boolean isEdited = false;
			
			if(annotation.values().length == 0){
				if(attributeToEdit == null)
					textField = createOptionPanelTextField(attribute,defaultValue, false);
				else{
					textField = createOptionPanelTextField(attribute,attributeToEdit.getDefaultValue(), true);
					isEdited = true;
				}
			}else{
				if(attributeToEdit == null)
					combo = createOptionPanelComboBox(annotation, attribute);
				else{
					combo = editOptionPanelComboBox(annotation, attribute,attributeToEdit.getDefaultValue());
					isEdited = true;
				}
			}
				
			if(textField != null){
				optionsPanelRight.add(createCheckBox(textField, attribute, isEdited));
			}else if(combo != null){
				optionsPanelRight.add(createCheckBox(combo, attribute, isEdited));
			}
				
			attribute.setDefaultValue(defaultValue);
			optionsAttributes.add(attribute);
		}
		
		for (int i = 0; i < spaceToFillGrid; i++) {
			JLabel left = new JLabel();
			left.setVisible(false);
			configurationPanelLeft.add(left);
			JLabel center = new JLabel();
			left.setVisible(false);
			optionsPanelCenter.add(center);
			JLabel right = new JLabel();
			left.setVisible(false);
			optionsPanelRight.add(right);
		}
		
		optionsButton.setVisible(true);
		helpArea.setVisible(true);
		invalidate();
		validate();
	}

	private JTextField createOptionPanelTextField(AutomatorOptionsAttribute attribute, String defaultValue, boolean editMode) {
		JTextField textField;
		textField = new JTextField();
		textField.setEnabled(editMode);
		textField.setText(defaultValue);
		attribute.setComponent(textField);
		optionsPanelCenter.add(textField);
		return textField;
	}
	
	private JComboBox<String> createOptionPanelComboBox(ArgumentsAnnotation annotation, AutomatorOptionsAttribute attribute) {
		JComboBox<String> combo;
		combo = new JComboBox<String>();
		combo.setEnabled(false);
		attribute.setComponent(combo);
		for (String v : annotation.values()) 
			combo.addItem(v);
		
		optionsPanelCenter.add(combo);
		return combo;
	}
	
	private JComboBox<String> editOptionPanelComboBox(ArgumentsAnnotation annotation, AutomatorOptionsAttribute attribute, String value) {
		JComboBox<String> combo;
		combo = new JComboBox<String>();
		combo.setEnabled(true);
		attribute.setComponent(combo);
		ArrayList<String> rest = new ArrayList<String>();
		for (String v : annotation.values()){
			if(v.equals(value))
				combo.addItem(v);
			else
				rest.add(v);
		}
		for (String r : rest){ 
			combo.addItem(r);
		}
		optionsPanelCenter.add(combo);
		return combo;
	}

	private void cleanOptionsPanel() {
		configurationPanelLeft.removeAll();
		optionsPanelCenter.removeAll();
		optionsPanelRight.removeAll();
		optionsButton.setVisible(false);
		helpArea.setVisible(false);
		repaint();
	}

	private ArrayList<ArgumentsAnnotation> addAnnotationsToList(Field[] fields){
		ArrayList<ArgumentsAnnotation> annotationsList = new ArrayList<ArgumentsAnnotation>();
		for (Field f : fields) {
			if(f.isAnnotationPresent(ArgumentsAnnotation.class))
				annotationsList.add(f.getAnnotation(ArgumentsAnnotation.class));
		}
		return annotationsList;
	}
	
	private Component createCheckBox(Component component, AutomatorOptionsAttribute attribute, boolean editedAttribute){
		JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(editedAttribute);
		checkBox.addActionListener(new CheckBoxListener(component));
		attribute.setCheckBox(checkBox);
		return checkBox;
	}
	
	private void editOptionPanelAttribute(){
		try {
			String attributeID = sensorsActuatorsList.getSelectedValue();
			
			if(attributeID != null){
				Arguments args = null;
				
				Class<?> c = null;
				
				c = attributeID.contains("Sensor") ? Sensor.class : Actuator.class;
				
				args = robotConfig.getSensorActuator(attributeID);
				
				editedAttributeName = attributeID;
				
				//Preencher com as opções existentes
				ArrayList<AutomatorOptionsAttribute> optionsAttribute = getAttributesFromArgumentsString(args);
				
				optionsAttributes.clear();
				populateOptionsPanel(ClassLoadHelper.findClassesContainingName(c), args.getArgumentAsString("classname"), optionsAttribute);
				
			}	
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private ArrayList<AutomatorOptionsAttribute> getAttributesFromArgumentsString(Arguments args) {
		
		ArrayList<AutomatorOptionsAttribute> optionsAttribute = new ArrayList<AutomatorOptionsAttribute>();
		
		for (String key : args.getArguments()) { 
			
			String val = args.getArgumentAsString(key);
			
			if(key.equals("classname")) {
				currentClassName = val;
				currentOptions = val.contains("Sensor") ? "sensors" : "actuators";
			} else {
				AutomatorOptionsAttribute oA = new AutomatorOptionsAttribute();
				oA.setName(key);
				oA.setDefaultValue(val);
				optionsAttribute.add(oA);
			}
		}
		return optionsAttribute;
	}
	
	class OptionsAtributesListener implements ActionListener{
		
		private JComboBox<String> comboBox;
		private ArrayList<Class<?>> classesList;
		private String selected;

		public OptionsAtributesListener(Class<?> targetClass, String selected) {
			classesList = ClassLoadHelper.findClassesContainingName(targetClass);
			this.selected = selected;
		}
		
		public void actionPerformed(ActionEvent event) {
			try {
				editedAttributeName="";
				cleanOptionsPanel();
				optionsAttributes.clear();
				currentOptions = selected;
				comboBox = (JComboBox<String>) event.getSource();
				currentComboBox = comboBox;
				
				if(!((String)comboBox.getSelectedItem()).isEmpty()){
					currentClassName = (String)comboBox.getSelectedItem();
					populateOptionsPanel(classesList, currentClassName, null);
				}else{
					
					if(selected.equals("network"))
						result.getArgument("controllers").removeArgument("network");
					else
						result.setArgument(selected, new Arguments(""));
					
					cleanOptionsPanel();
					updateConfigurationText();
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	class TextFieldListener implements DocumentListener{

		private JTextField textfield;
		private String option;
		
		public TextFieldListener(JTextField textfield, String option) {
			this.textfield = textfield;
			this.option = option;
			if(option == "random-seed"){
				textfield.setText("1");
				result.setArgument(option, new Arguments(textfield.getText()));
				result.setSelectedKey("");
				updateConfigurationText();
			}
		}
		
		public void insertUpdate(DocumentEvent e) {
			updateTextfieldInformation();
		}

		public void removeUpdate(DocumentEvent e) {
			updateTextfieldInformation();
		}

		public void changedUpdate(DocumentEvent e) { }
		
		public void updateTextfieldInformation(){
			result.setArgument(option, new Arguments(textfield.getText()));
			updateConfigurationText();
		}
		
	}
	
	class LabelMouseListener implements MouseListener {
		private String helpText;
		
		public LabelMouseListener(String helpText) {
			this.helpText = helpText;
		}
		
		public void mouseReleased(MouseEvent e) { }
		public void mousePressed(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { 
			helpArea.setText("");
		}
		public void mouseEntered(MouseEvent e) { 
			helpArea.setText(helpText);
		}
		public void mouseClicked(MouseEvent e) { }
	}
	
	class CheckBoxListener implements ActionListener{
		
		Component component;
		
		public CheckBoxListener(Component component) {
			this.component = component;
		}

		public void actionPerformed(ActionEvent e) {
			JCheckBox checkBox = (JCheckBox)e.getSource();
			if(checkBox.isSelected())
				component.setEnabled(true);
			else
				component.setEnabled(false);
		}
	}
	
	public synchronized void wakeUpWaitingThreads() {
		notifyAll();
	}
	
	public synchronized void waitUntilEvolutionLaunched() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {}

	public String getConfigurationFileName() {
		return result.getArgument("output").getCompleteArgumentString().trim();
	}
	
}	