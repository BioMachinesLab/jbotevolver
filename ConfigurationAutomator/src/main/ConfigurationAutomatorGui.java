package main;

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
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import javax.swing.BorderFactory;
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
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import taskexecutor.TaskExecutor;
import utils.AutomatorOptionsAttribute;
import utils.ConfigurationResult;
import utils.RobotsResult;
import controllers.Controller;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.populations.Population;
import gui.renderer.Renderer;

public class ConfigurationAutomatorGui extends JFrame{
	
	private static final int OPTIONS_GRID_LAYOUT_SIZE = 15;
	
	private String[] keys =
		{"output","robots", "controllers", "population", "environment",
			"executor","evolution", "evaluation", "random-seed"};

	private JFrame previewFrame;
	
	private JPanel optionsPanelLeft;
	private JPanel optionsPanelCenter;
	private JPanel optionsPanelRight;
	
	private JPanel rendererPanel;
	
	private JTextArea configResult;
	private JTextArea helpArea;
	
	private JButton optionsButton;
	private JButton saveArgumentsFileButton;

	private ArrayList<AutomatorOptionsAttribute> optionsAttributes;
	
	private DefaultListModel<String> sensorsActuatorsListModel;
	private JList<String> sensorsActuatorsList;

	private String currentOptions;
	private String currentClassName;
	
	private String editedAttributeName;
	
	private RobotsResult robotConfig;
	private ConfigurationResult result;
	
	private Arguments rendererArgs;

	public ConfigurationAutomatorGui() {
		
		super("Configuration File Automator");
		
		optionsAttributes = new ArrayList<AutomatorOptionsAttribute>();
		
		robotConfig = new RobotsResult();
		result = new ConfigurationResult(keys);
		
		getContentPane().add(initLeftWrapperPanel(), BorderLayout.WEST);
		getContentPane().add(initCenterWrapperPanel(), BorderLayout.CENTER);
		getContentPane().add(initRightWrapperPanel(), BorderLayout.EAST);
		
		initListeners();

		previewFrame = new JFrame("Preview");
		previewFrame.setSize(800, 800);
		previewFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		previewFrame.setVisible(false);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1280, 860);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private Component initCenterWrapperPanel() {
		JPanel sidePanel = new JPanel(new BorderLayout());
		
		JPanel argumentsWrapper = new JPanel();
		argumentsWrapper.setLayout(new BorderLayout());
				
		JPanel north = new JPanel(new BorderLayout());
		JPanel subNorth = new JPanel(new BorderLayout());
		
		JPanel subPanelNorth = new JPanel(new GridLayout(1,1));
		subPanelNorth.add(initOutputWrapperPanel()); // 1. Output

		JPanel subPanelNorth2 = new JPanel(new GridLayout(1,1));
		subPanelNorth2.add(createJComboBoxPanel(new Class<?>[]{Robot.class, Sensor.class, Actuator.class})); // 2. Robots
		
		JPanel subPanelNorth3 = new JPanel(new GridLayout(1,1));
		subPanelNorth3.add(createJComboBoxPanel(new Class<?>[]{Controller.class, NeuralNetwork.class})); // 3. Controllers
		
		subNorth.add(subPanelNorth, BorderLayout.NORTH);
		subNorth.add(subPanelNorth2);
		subNorth.add(subPanelNorth3, BorderLayout.SOUTH);
		
		north.add(subNorth, BorderLayout.NORTH);
		north.add(createJComboBoxPanel(new Class<?>[]{Population.class, Environment.class, TaskExecutor.class, Evolution.class, EvaluationFunction.class}));
		
		argumentsWrapper.add(north, BorderLayout.NORTH);
		argumentsWrapper.setBorder(BorderFactory.createTitledBorder("Arguments"));

		sidePanel.add(argumentsWrapper, BorderLayout.NORTH);
		
		rendererPanel = new JPanel(new BorderLayout());
		rendererPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
		sidePanel.add(rendererPanel);
		
		return sidePanel;
	}
	
	private Component initLeftWrapperPanel() {
		JPanel fileContentWrapper = new JPanel();
		fileContentWrapper.setPreferredSize(new Dimension(370,370));
		fileContentWrapper.setLayout(new BorderLayout());
		
		JPanel resultContentWrapper = new JPanel(new BorderLayout());
		configResult = new JTextArea();
		configResult.setEditable(false);
		updateConfigurationText();
		resultContentWrapper.add(new JScrollPane(configResult));
		resultContentWrapper.setBorder(BorderFactory.createTitledBorder("Result"));
		
		resultContentWrapper.add(createResultContentButtons(), BorderLayout.SOUTH);
		fileContentWrapper.add(resultContentWrapper, BorderLayout.CENTER);
		
		JPanel jListContentWrapper = new JPanel(new BorderLayout());
		sensorsActuatorsListModel = new DefaultListModel<String>();
		sensorsActuatorsList = new JList<String>(sensorsActuatorsListModel);
		
		JPanel buttonsPanel = new JPanel();
		
		JButton jListRemoveButton = new JButton("Remove");
		
		jListRemoveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeElementFromSensorsActuatorsList();
			}
		});
		
		buttonsPanel.add(jListRemoveButton);
		
		jListContentWrapper.add(new JScrollPane(sensorsActuatorsList));
		jListContentWrapper.add(buttonsPanel, BorderLayout.SOUTH);
		jListContentWrapper.setBorder(BorderFactory.createTitledBorder("Attributes List"));
		fileContentWrapper.add(jListContentWrapper, BorderLayout.SOUTH);
	
		return fileContentWrapper;
	}
	
	private Component initRightWrapperPanel() {
		JPanel rightWrapper = new JPanel();
		rightWrapper.setPreferredSize(new Dimension(410,getHeight()));
		rightWrapper.setLayout(new BorderLayout());
		
		optionsPanelLeft = new JPanel();
		optionsPanelCenter = new JPanel();
		optionsPanelRight = new JPanel();
		
		optionsButton = new JButton("Add to File");
		optionsButton.setVisible(false);
		
		JPanel borderLayout = new JPanel(new BorderLayout());
		
		JPanel splitLayout = new JPanel(new BorderLayout());
		splitLayout.add(optionsPanelLeft, BorderLayout.WEST);
		splitLayout.add(optionsPanelCenter, BorderLayout.CENTER);
		splitLayout.add(optionsPanelRight, BorderLayout.EAST);
		borderLayout.add(new JScrollPane(splitLayout,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		
		helpArea = new JTextArea();
		Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
		helpArea.setWrapStyleWord(true);
		helpArea.setLineWrap(true);
		helpArea.setEnabled(false);
		helpArea.setBorder(border);
		helpArea.setVisible(false);
		helpArea.setPreferredSize(new Dimension(borderLayout.getWidth(), 45));
		borderLayout.add(helpArea,BorderLayout.SOUTH);
		
		rightWrapper.add(borderLayout);
		rightWrapper.add(optionsButton, BorderLayout.SOUTH);
		rightWrapper.setBorder(BorderFactory.createTitledBorder("Options"));
		return rightWrapper;
	}

	private Component initOutputWrapperPanel() {
		JPanel outputWrapper = new JPanel();
		outputWrapper.setLayout(new GridLayout(2,1));
		
		JTextField outputTextField = new JTextField(20);
		JTextField randomSeedTextField = new JTextField(10);
		
		outputTextField.getDocument().addDocumentListener(new TextFieldListener(outputTextField, "output"));
		randomSeedTextField.getDocument().addDocumentListener(new TextFieldListener(randomSeedTextField, "random-seed"));
		
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
			configName.replace("neuralnetwork", "network");
			configName = nameExceptions.contains(configName) ? configName+"s" : configName;
			
			currentBox.addActionListener(new OptionsAtributesListener(c, configName));
			panel.add(new JLabel(c.getSimpleName()+": "));
			panel.add(currentBox);
		}
		
		panel.setBorder(BorderFactory.createTitledBorder(""));
		return panel;
	}
	
	private void initListeners() {

		optionsButton.addActionListener(new ActionListener() {
			@Override
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
			@Override
			public void actionPerformed(ActionEvent event) {
				createArgumentFile();
			}
		});
	}

	private void updateConfigurationText() {
		configResult.setText(result.toString());
	}
	
	private JPanel createResultContentButtons() {
		JPanel panel = new JPanel();
		saveArgumentsFileButton = new JButton("Save to File");
		panel.add(saveArgumentsFileButton);
		return panel;
	}
	
	private void amplifyPreview(Renderer renderer) {
		if(renderer != null){
			previewFrame.getContentPane().removeAll();
			previewFrame.getContentPane().add(renderer);
			previewFrame.setVisible(true);
			previewFrame.invalidate();
			previewFrame.validate();
			previewFrame.repaint();
		}
	}
	
	private void seeSensors() {
		
		if(robotConfig.getCompleteArguments().getNumberOfArguments() > 0 && result.getArgument("environment").getNumberOfArguments() > 0){
			try {
				String extraRendererArguments = "conesensorid=";
				
				String selectedID = sensorsActuatorsList.getSelectedValue();
						
				if(selectedID != null && selectedID.contains("Sensor")){
					String id = robotConfig.getSensorActuator(selectedID).getArgumentAsString("id");
					extraRendererArguments += id;
				} else
					extraRendererArguments += -1;
				
				showPreview(extraRendererArguments);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void showPreview(String rendererExtraArgs) throws Exception{
		String arguments = "classname=TwoDRendererDebug," +rendererExtraArgs;
		rendererArgs = new Arguments(arguments,true);
		
		Renderer renderer = setupRenderer(rendererArgs);
		
		renderer.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {
				amplifyPreview(setupRenderer(rendererArgs));
			}
		});
		
		rendererPanel.removeAll();
		
		if (renderer != null) {
			
			rendererPanel.add(renderer);
			rendererPanel.revalidate();
			rendererPanel.repaint();
			
			revalidate();
			repaint();
		}
	}
	
	private Renderer setupRenderer(Arguments rArgs) {
		
		try {
			Renderer renderer = Renderer.getRenderer(rArgs);
			
			String[] args = Arguments.readOptionsFromString(
					"--robots " + result.getArgument("robots") + "\n" +
					"--environment " + result.getArgument("environment"));
			
			HashMap<String, Arguments> arguments = Arguments.parseArgs(args);
			
			Simulator simulator = new Simulator(new Random(), arguments);
			
			ArrayList<Robot> robots = Robot.getRobots(simulator, arguments.get("--robots"));
			simulator.addRobots(robots);
			
			simulator.setupEnvironment();
			
			renderer.enableInputMethods(true);
			renderer.setSimulator(simulator);
			renderer.drawFrame();
			
			return renderer;
		
		} catch(Exception e) {
			//Gotta catch 'em all! Silence the exceptions that might occur because of problems in the
			//initialization of Environments
		}
		
		return null;
	}

	private void createArgumentFile() {
		
		String outputText = result.getArgument("--output").getCompleteArgumentString().trim();
		
		if(outputText.length() > 0){
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Save File");
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				PrintWriter printWriter = null;
				try {
					String filePath = fileChooser.getSelectedFile().getAbsolutePath() + "/" + outputText + ".conf";
					
					printWriter = new PrintWriter(new File(filePath));
			    	printWriter.println(result.toString());
			    	JOptionPane.showMessageDialog(this, "File " + outputText + ".conf, created!");
			    } catch (FileNotFoundException e) {
			    	e.printStackTrace();
			    } finally {
			    	printWriter.close ();
			    }
			}
		}else{
			JOptionPane.showMessageDialog(this, "No Setup Name defined.");
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
		
		for (Class<?> cl : aux)
			comboBox.addItem(cl.getSimpleName());
		
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
				result.setArgument("robots",robotConfig.getCompleteArguments());
				break;
			case "network":
				Arguments controllerArgs = result.getArgument("controllers");
				controllerArgs.setArgument("network", arguments + ",inputs=auto,outputs=auto");
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

	private void fullFillOptionsPanel(ArrayList<Class<?>> classesList, String className, ArrayList<AutomatorOptionsAttribute> optAttributes) throws ClassNotFoundException {
		cleanOptionsPanel();
		
		Class<?> cls = null;
		for (Class<?> c : classesList) {
			if(c.getName().contains(className)){
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
		
		int spaceToFillGrid = 0;
		
		if(annotations.size() >= OPTIONS_GRID_LAYOUT_SIZE){
			optionsPanelLeft.setLayout(new GridLayout(annotations.size()+1,1));
			optionsPanelCenter.setLayout(new GridLayout(annotations.size()+1,1));
			optionsPanelRight.setLayout(new GridLayout(annotations.size()+1,1));
		}else{
			spaceToFillGrid = OPTIONS_GRID_LAYOUT_SIZE - (annotations.size()+1);
			
			optionsPanelLeft.setLayout(new GridLayout(OPTIONS_GRID_LAYOUT_SIZE,1));
			optionsPanelCenter.setLayout(new GridLayout(OPTIONS_GRID_LAYOUT_SIZE,1));
			optionsPanelRight.setLayout(new GridLayout(OPTIONS_GRID_LAYOUT_SIZE,1));	
		}
		
		JLabel jl = new JLabel("classname");
		JTextField jtf = new JTextField(currentClassName);
		jtf.setEnabled(false);
		JCheckBox jcb = new JCheckBox();
		jcb.setEnabled(false);
		jcb.setVisible(false);
		optionsPanelLeft.add(jl);
		optionsPanelCenter.add(jtf);
		optionsPanelRight.add(jcb);
		
		for (ArgumentsAnnotation annotation : annotations) {
			JLabel label = new JLabel(annotation.name());
				
			if(!annotation.help().isEmpty())
				label.addMouseListener(new LabelMouseListener(annotation.help()));
			
			optionsPanelLeft.add(label);
				
			AutomatorOptionsAttribute attribute = new AutomatorOptionsAttribute();
			attribute.setName(annotation.name());
			String defaultValue = annotation.defaultValue();
			
			JTextField textField = null;
			JComboBox<String> combo = null;
			AutomatorOptionsAttribute attributeToEdit = null;
			
			if(optAttributes != null){
				for (AutomatorOptionsAttribute optAttribute : optAttributes) {
					if(optAttribute.getName().equals(annotation.name()))
						attributeToEdit = optAttribute;
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
			optionsPanelLeft.add(left);
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
		optionsPanelLeft.removeAll();
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
				
				fullFillOptionsPanel(ClassLoadHelper.findClassesContainingName(c), args.getArgumentAsString("classname"), optionsAttribute);
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
		
		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent event) {
			editedAttributeName="";
			try {
				currentOptions = selected;
				comboBox = (JComboBox<String>) event.getSource();
				if(!((String)comboBox.getSelectedItem()).isEmpty()){
					currentClassName = (String)comboBox.getSelectedItem();
					fullFillOptionsPanel(classesList, currentClassName, null);
				}else{
					
					if(selected.equals("network"))
						result.getArgument("controllers").removeArgument("network");
					else
						result.setArgument(selected, new Arguments(""));
					
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
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateTextfieldInformation();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateTextfieldInformation();
		}

		@Override
		public void changedUpdate(DocumentEvent e) { }
		
		public void updateTextfieldInformation(){
			result.setArgument(option, new Arguments(textfield.getText(),false));
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

		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox checkBox = (JCheckBox)e.getSource();
			if(checkBox.isSelected())
				component.setEnabled(true);
			else
				component.setEnabled(false);
		}
		
	}
	
	public static void main(String[] args) {
		new ConfigurationAutomatorGui();
	}
	
}	