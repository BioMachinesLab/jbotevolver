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

import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import taskexecutor.TaskExecutor;
import utils.AutomatorOptions;
import utils.AutomatorOptionsAttribute;
import utils.ConfigurationResult;
import utils.ControllersResult;
import utils.RobotsResult;
import controllers.Controller;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.populations.Population;

public class ConfigurationAutomatorGui {
	
	private static final int OPTIONS_GRID_LAYOUT_SIZE = 15;

	private JFrame frame;
	
	private JPanel optionsPanelLeft;
	private JPanel optionsPanelCenter;
	private JPanel optionsPanelRight;
	
	private JTextArea configResult;
	
	private JTextArea helpArea;
	
	private JTextField outputTextField;
	private JTextField randomSeedTextField;
	
	private JButton optionsButton;
	private JButton saveArgumentsFileButton;
	private JButton jListButton;
	
	private JComboBox<String> robotsClassNameComboBox;
	private JComboBox<String> sensorsComboBox;
	private JComboBox<String> actuatorsComboBox;
	private JComboBox<String> controllersClassNameComboBox;
	private JComboBox<String> networkComboBox;
	private JComboBox<String> populationClassNameComboBox;
	private JComboBox<String> environmentClassNameComboBox;
	private JComboBox<String> executorClassNameComboBox;
	private JComboBox<String> evolutionClassNameComboBox;
	private JComboBox<String> evaluationClassNameComboBox;
	
	private ArrayList<AutomatorOptionsAttribute> optionsAttributes;
	
	private DefaultListModel<String> jListModel;
	private JList<String> jList;
	
	private AutomatorOptions currentOptions;
	private String currentClassName;
	
	private int sensorId = 1;
	private int actuatorId = 1;
	
	private boolean editedAttribute;
	
	private RobotsResult robotsResult;
	private ControllersResult controllersResult;
	private ConfigurationResult result ;

	public ConfigurationAutomatorGui() {
		
		optionsAttributes = new ArrayList<AutomatorOptionsAttribute>();
		
		robotsResult = new RobotsResult();
		controllersResult = new ControllersResult();
		result = new ConfigurationResult(robotsResult,controllersResult);
		
		frame = new JFrame("Configuration File Automator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1280, 760);
	
		frame.getContentPane().add(initLeftWrapperPanel(), BorderLayout.WEST);
		frame.getContentPane().add(initCenterWrapperPanel(), BorderLayout.CENTER);
		frame.getContentPane().add(initRightWrapperPanel(), BorderLayout.EAST);

		initListeners();
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
	}

	private Component initLeftWrapperPanel() {
		JPanel argumentsWrapper = new JPanel();
		argumentsWrapper.setPreferredSize(new Dimension(450,frame.getHeight()));
		argumentsWrapper.setLayout(new GridLayout(8, 1));
		
		argumentsWrapper.add(initOutputWrapperPanel()); // 1. Output
		argumentsWrapper.add(initRobotsWrapperPanel()); // 2. Robots
		argumentsWrapper.add(initControllersWrapperPanel()); // 3. Controllers
		argumentsWrapper.add(initPopulationWrapperPanel()); // 4. Population
		argumentsWrapper.add(initEnvironmentWrapperPanel()); // 5. Environment
		argumentsWrapper.add(initExecutorWrapperPanel()); // 6. Executor
		argumentsWrapper.add(initEvolutionWrapperPanel()); // 7. Evolution
		argumentsWrapper.add(initEvaluationWrapperPanel()); // 8. Evaluation
		
		argumentsWrapper.setBorder(BorderFactory.createTitledBorder("Arguments"));

		return argumentsWrapper;
	}
	
	private Component initCenterWrapperPanel() {
		JPanel fileContentWrapper = new JPanel();
		fileContentWrapper.setLayout(new BorderLayout());
		
		JPanel resultContentWrapper = new JPanel(new BorderLayout());
		configResult = new JTextArea();
		configResult.setEditable(false);
		configResult.setText(result.getResult());
		resultContentWrapper.add(new JScrollPane(configResult));
		resultContentWrapper.setBorder(BorderFactory.createTitledBorder("Result"));
		saveArgumentsFileButton = new JButton("Save to File");
		resultContentWrapper.add(saveArgumentsFileButton, BorderLayout.SOUTH);
		fileContentWrapper.add(resultContentWrapper, BorderLayout.CENTER);
		
		JPanel jListContentWrapper = new JPanel(new BorderLayout());
		jListModel = new DefaultListModel<String>();
		jList = new JList<String>(jListModel);
		jListButton = new JButton("Remove");
		jListContentWrapper.add(new JScrollPane(jList));
		jListContentWrapper.add(jListButton, BorderLayout.SOUTH);
		jListContentWrapper.setBorder(BorderFactory.createTitledBorder("Attributes List"));
		fileContentWrapper.add(jListContentWrapper, BorderLayout.SOUTH);
	
		return fileContentWrapper;
	}
	
	private Component initRightWrapperPanel() {
		JPanel rightWrapper = new JPanel();
		rightWrapper.setPreferredSize(new Dimension(410,frame.getHeight()));
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
		
		JLabel outputLabel = new JLabel("Setup Name: ");
		outputTextField = new JTextField(20);
		JLabel randomSeedLabel = new JLabel("Random Seed: ");
		randomSeedTextField = new JTextField(10);
		
		outputWrapper.add(outputLabel);
		outputWrapper.add(outputTextField);
		outputWrapper.add(randomSeedLabel);
		outputWrapper.add(randomSeedTextField);

		outputWrapper.setBorder(BorderFactory.createTitledBorder("Output"));
		return outputWrapper;
	}

	private Component initRobotsWrapperPanel() {
		JPanel robotsWrapper = new JPanel();
		robotsWrapper.setLayout(new GridLayout(3,1));
		
		JLabel classNameLabel = new JLabel("Class name: ");
		robotsClassNameComboBox = loadRobotsClassNamesToComboBox(Robot.class);
		
		JLabel sensorsLabel = new JLabel("Sensors: ");
		sensorsComboBox = loadRobotsClassNamesToComboBox(Sensor.class);
		
		JLabel actuatorsLabel = new JLabel("Actuators: ");
		actuatorsComboBox = loadRobotsClassNamesToComboBox(Actuator.class);
		
		robotsWrapper.add(classNameLabel);
		robotsWrapper.add(robotsClassNameComboBox);
		
		robotsWrapper.add(sensorsLabel);
		robotsWrapper.add(sensorsComboBox);
		
		robotsWrapper.add(actuatorsLabel);
		robotsWrapper.add(actuatorsComboBox);

		robotsWrapper.setBorder(BorderFactory.createTitledBorder("Robots"));
		return robotsWrapper;
	}
	
	private Component initControllersWrapperPanel() {
		JPanel controllersWrapper = new JPanel();
		controllersWrapper.setLayout(new GridLayout(2,1));
		
		JLabel classNameLabel = new JLabel("Class name: ");
		controllersClassNameComboBox = loadRobotsClassNamesToComboBox(Controller.class);
		
		JLabel networkLabel = new JLabel("Network: ");
		networkComboBox = loadRobotsClassNamesToComboBox(NeuralNetwork.class);
		
		controllersWrapper.add(classNameLabel);
		controllersWrapper.add(controllersClassNameComboBox);
		
		controllersWrapper.add(networkLabel);
		controllersWrapper.add(networkComboBox);
		
		controllersWrapper.setBorder(BorderFactory.createTitledBorder("Controllers"));
		return controllersWrapper;
	}

	private Component initPopulationWrapperPanel() {
		JPanel populationWrapper = new JPanel();
		populationWrapper.setLayout(new GridLayout(1,1));
		
		JLabel classNameLabel = new JLabel("Class name: ");
		populationClassNameComboBox = loadRobotsClassNamesToComboBox(Population.class);
		
		populationWrapper.add(classNameLabel);
		populationWrapper.add(populationClassNameComboBox);
		
		populationWrapper.setBorder(BorderFactory.createTitledBorder("Population"));
		return populationWrapper;
	}

	private Component initEnvironmentWrapperPanel() {
		JPanel environmentWrapper = new JPanel();
		environmentWrapper.setLayout(new GridLayout(1,1));
		
		JLabel classNameLabel = new JLabel("Class name: ");
		environmentClassNameComboBox = loadRobotsClassNamesToComboBox(Environment.class);
		
		environmentWrapper.add(classNameLabel);
		environmentWrapper.add(environmentClassNameComboBox);
		
		environmentWrapper.setBorder(BorderFactory.createTitledBorder("Environment"));
		return environmentWrapper;
	}

	private Component initExecutorWrapperPanel() {
		JPanel executorWrapper = new JPanel();
		executorWrapper.setLayout(new GridLayout(1,1));
		
		JLabel classNameLabel = new JLabel("Class name: ");
		executorClassNameComboBox = loadRobotsClassNamesToComboBox(TaskExecutor.class);
		
		executorWrapper.add(classNameLabel);
		executorWrapper.add(executorClassNameComboBox);
		
		executorWrapper.setBorder(BorderFactory.createTitledBorder("Executor"));
		return executorWrapper;
	}

	private Component initEvolutionWrapperPanel() {
		JPanel evolutionWrapper = new JPanel();
		evolutionWrapper.setLayout(new GridLayout(1,1));
		
		JLabel classNameLabel = new JLabel("Class name: ");
		evolutionClassNameComboBox = loadRobotsClassNamesToComboBox(Evolution.class);
		
		evolutionWrapper.add(classNameLabel);
		evolutionWrapper.add(evolutionClassNameComboBox);
		
		evolutionWrapper.setBorder(BorderFactory.createTitledBorder("Evolution"));
		return evolutionWrapper;
	}

	private Component initEvaluationWrapperPanel() {
		JPanel evaluationWrapper = new JPanel();
		evaluationWrapper.setLayout(new GridLayout(1,1));
		
		JLabel classNameLabel = new JLabel("Class name: ");
		evaluationClassNameComboBox = loadRobotsClassNamesToComboBox(EvaluationFunction.class);
		
		evaluationWrapper.add(classNameLabel);
		evaluationWrapper.add(evaluationClassNameComboBox);
		
		evaluationWrapper.setBorder(BorderFactory.createTitledBorder("Evaluation"));
		return evaluationWrapper;
	}
	
	private void initListeners() {
		outputTextField.getDocument().addDocumentListener(new TextFieldListener(outputTextField, AutomatorOptions.OUTPUT));
		randomSeedTextField.getDocument().addDocumentListener(new TextFieldListener(randomSeedTextField, AutomatorOptions.RANDOM_SEED));
		
		robotsClassNameComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(Robot.class), AutomatorOptions.ROBOTS));
		sensorsComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(Sensor.class), AutomatorOptions.SENSORS));
		actuatorsComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(Actuator.class), AutomatorOptions.ACTUATORS));
		controllersClassNameComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(Controller.class), AutomatorOptions.CONTROLLERS));
		networkComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(NeuralNetwork.class), AutomatorOptions.NETWORK));
		populationClassNameComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(Population.class), AutomatorOptions.POPULATION));
		environmentClassNameComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(Environment.class), AutomatorOptions.ENVIRONMENT));
		executorClassNameComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(TaskExecutor.class), AutomatorOptions.EXECUTOR));
		evolutionClassNameComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(Evolution.class), AutomatorOptions.EVOLUTION));
		evaluationClassNameComboBox.addActionListener(new OptionsAtributesListener(findClassesContainingName(EvaluationFunction.class), AutomatorOptions.EVALUATION));
		
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(editedAttribute){
					jListButton.doClick();
				}
				writeAttributes();
				jListModel.clear();
				for (String id : result.getRobotsResult().getSensorIds()) {
					jListModel.addElement(id);
				}
				for (String id : result.getRobotsResult().getActuatorsIds()) {
					jListModel.addElement(id);
				}
				configResult.setText(result.getResult());
				optionsAttributes.clear();
				cleanOptionsPanel();
			}
		});
		
		jList.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent event) { }
			@Override
			public void mousePressed(MouseEvent event) { }
			@Override
			public void mouseExited(MouseEvent event) { }
			@Override
			public void mouseEntered(MouseEvent event) { }
			@Override
			public void mouseClicked(MouseEvent event) {
				try {
					editOptionPanelAttribute();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
		
		jListButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedID = jList.getSelectedValue();
				if(selectedID.contains("Sensor"))
					result.getRobotsResult().removeSensorInformation(selectedID);
				else if(selectedID.contains("Actuator"))
					result.getRobotsResult().removeActuatorInformation(selectedID);
				else if(selectedID.contains("network"))
					result.getRobotsResult().removeActuatorInformation(selectedID);
				else if(selectedID.contains("robot"))
					result.getRobotsResult().removeActuatorInformation(selectedID);
				else
					System.err.println("Selected Id doesn't contain Sensor, Actuator, Robot or Network.");
				
				jListModel.removeElement(selectedID);
				configResult.setText(result.getResult());
			}
		});
		
		saveArgumentsFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(outputTextField.getText().trim().length() > 0){
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setDialogTitle("Save File");
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
						PrintWriter printWriter = null;
						try {
							String pathname = fileChooser.getSelectedFile().getAbsolutePath() + "/" + outputTextField.getText() + ".conf";
							
							printWriter = new PrintWriter(new File(pathname));
					    	printWriter.println(result.getResult());
					    	JOptionPane.showMessageDialog(frame, "File " + outputTextField.getText() + ".conf, created!");
					    } catch (FileNotFoundException e) {
					    	e.printStackTrace();
					    } finally {
					    	printWriter.close ();
					    }
					}
				}else{
					JOptionPane.showMessageDialog(frame, "No Setup Name defined.");
				}
				
			}
		});
	}

	private JComboBox<String> loadRobotsClassNamesToComboBox(Class<?> className) {
		
		JComboBox<String> comboBox = new JComboBox<String>();
		ArrayList<Class<?>> aux = findClassesContainingName(className);
		
		comboBox.addItem("");
		
		for (Class<?> cl : aux)
			comboBox.addItem(cl.getSimpleName());
		
		return comboBox;
	}

	public ArrayList<Class<?>> findClassesContainingName(Class<?> objectClass) {
    	ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
    	
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(System.getProperty("path.separator"));

        for (String path : paths) {
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
            	findClasses(file, objectClass,classes);
            }
        }
        
        return classes;
    }

	private void findClasses(File file, Class<?> objectClass, ArrayList<Class<?>> cls) {
		try {
			for (File f : file.listFiles()) {
	    		if(f.isDirectory()){
	    			findClasses(f, objectClass,cls);
	    		}else{
	    			if(f.getName().endsWith(".class")){
	    				String path = f.getAbsolutePath().split("/bin/")[1].replaceAll(".class", "").replaceAll("/", ".");
		    			Class<?> cl = Class.forName(path);

		    			if(isIntanceOf(cl, objectClass)){
		    				cls.add(cl);
		    			}
		    			
	    			}
	    		}
	      	}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private boolean isIntanceOf(Class<?> cls, Class<?> objectClass) {
		Class<?> superClass = cls.getSuperclass();
		
		if(superClass != null){			
			if(superClass.equals(Object.class)){
				return false;
			}else if(superClass.equals(objectClass)){
				return true;
			}else{
				return isIntanceOf(superClass, objectClass);
			}
		}
		
		return false;
	}
	
	private void writeAttributes() {
		switch (currentOptions) {
			case ROBOTS:
				result.getRobotsResult().appendTextToResult("classname=" + currentClassName);
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					String s = createAttributeString(attribute, false);
					if(!s.isEmpty())
						result.getRobotsResult().appendTextToResult(s);
				}
				break;
			case SENSORS:
				String sensorInformation = currentClassName + "=(classname=" + currentClassName + ",id=" + sensorId++;
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					sensorInformation += createAttributeString(attribute, true);
				}
				sensorInformation += ")";
				result.getRobotsResult().addSensorInformation(currentClassName, sensorInformation);
				break;
			case ACTUATORS:
				String actuatorInformation = currentClassName + "=(classname=" + currentClassName + ",id=" + actuatorId++;
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					actuatorInformation += createAttributeString(attribute, true);
				}
				actuatorInformation += ")";
				result.getRobotsResult().addActuatorInformation(currentClassName, actuatorInformation);
				break;
			case CONTROLLERS:
				result.getControllersResult().addClassname("classname=" + currentClassName);
				break;
			case NETWORK:
				result.getControllersResult().addNetworkClassname("classname=" + currentClassName);
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					String s = createAttributeString(attribute, false);
					if(!s.isEmpty())
					result.getControllersResult().addToNetworkString(s);
				}
				break;
			case POPULATION:
				result.appendTextToPopulation("classname=" + currentClassName);
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					String s = createAttributeString(attribute, false);
					if(!s.isEmpty())
						result.appendTextToPopulation(s);
				}
				break;
			case ENVIRONMENT:
				result.appendTextToEnvironment("classname=" + currentClassName);
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					String s = createAttributeString(attribute, false);
					if(!s.isEmpty())
						result.appendTextToEnvironment(s);
				}
				break;
			case EXECUTOR:
				result.appendTextToExecutor("classname=" + currentClassName);
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					String s = createAttributeString(attribute, false);
					if(!s.isEmpty())
						result.appendTextToExecutor(s);
				}
				break;
			case EVOLUTION:
				result.appendTextToEvolution("classname=" + currentClassName);
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					String s = createAttributeString(attribute, false);
					if(!s.isEmpty())
						result.appendTextToEvolution(s);
				}
				break;
			case EVALUATION:
				result.appendTextToEvaluation("classname=" + currentClassName);
				for (AutomatorOptionsAttribute attribute : optionsAttributes) {
					String s = createAttributeString(attribute, false);
					if(!s.isEmpty())
						result.appendTextToEvaluation(s);
				}
				break;
		default:
			System.err.println("The currentOption variable is not available for writeAttributes()!");
			break;
		}
	}

	private String createAttributeString(AutomatorOptionsAttribute attribute, boolean insertComma) {
		String result = "";
		if(attribute.getComponent() instanceof JTextField){
			JTextField textField = (JTextField)attribute.getComponent();
			if(attribute.getCheckBox().isSelected() && !textField.getText().isEmpty()){
				if(insertComma)
					result = "," + attribute.getName() + "=" + textField.getText();
				else
					result = attribute.getName() + "=" + textField.getText();
			}
		}else if(attribute.getComponent() instanceof JComboBox<?>){
			JComboBox<?> comboBox = (JComboBox<?>) attribute.getComponent();
			String selection = (String)comboBox.getSelectedItem();
			if(attribute.getCheckBox().isSelected() && !selection.isEmpty()){
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
			
			optionsPanelLeft.setLayout(new GridLayout(15,1));
			optionsPanelCenter.setLayout(new GridLayout(15,1));
			optionsPanelRight.setLayout(new GridLayout(15,1));	
		}
		
		editedAttribute = false;
		
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
				
			JTextField textField = null;
			JComboBox<String> combo = null;
			String defaultValue = annotation.defaultValue();
			
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
					editedAttribute = true; 
					isEdited = true;
				}
			}else{
				if(attributeToEdit == null)
					combo = createOptionPanelComboBox(annotation, attribute);
				else{
					combo = editOptionPanelComboBox(annotation, attribute,attributeToEdit.getDefaultValue());
					editedAttribute = true;
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
		frame.invalidate();
		frame.validate();
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
		frame.repaint();
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
	
	private void editOptionPanelAttribute() throws ClassNotFoundException {
		String attributeID = jList.getSelectedValue();
		if(attributeID != null){
			Arguments args = null;
			
			Class<?> c = null;
			if(attributeID.contains("Sensor")){
				c = Sensor.class;
				args = result.getRobotsResult().getArgumentsForSensorId(attributeID);
			}else if(attributeID.contains("Actuator")){
				c = Actuator.class;
				args = result.getRobotsResult().getArgumentsForActuatorId(attributeID);
			}else{
				System.err.println("Error on the editOptionPanelAttribute(), attributeID don't contain the word Sensor or Actuator");
			}
			
			//Preencher com as opções existentes
			ArrayList<AutomatorOptionsAttribute> optionsAttribute = getAttributesFromArgumentsString(args);
			
			fullFillOptionsPanel(findClassesContainingName(c), attributeID.split(" ")[0], optionsAttribute);
		}
	}
	
	private ArrayList<AutomatorOptionsAttribute> getAttributesFromArgumentsString(Arguments args) {
		ArrayList<AutomatorOptionsAttribute> optionsAttribute = new ArrayList<AutomatorOptionsAttribute>();
		
		for (String v : args.getValues()) {
			String[] argsAttributes = v.split(",");
			for (String a : argsAttributes) {
				String[] attributeComplete = a.split("=");
				if(!attributeComplete[0].equals("classname")){
					AutomatorOptionsAttribute oA = new AutomatorOptionsAttribute();
					oA.setName(attributeComplete[0]);
					oA.setDefaultValue(attributeComplete[1]);
					optionsAttribute.add(oA);
				}else{
					currentClassName = attributeComplete[1];
					if(attributeComplete[1].contains("Sensor")){
						currentOptions = AutomatorOptions.SENSORS;
					}else if(attributeComplete[1].contains("Actuator")){
						currentOptions = AutomatorOptions.ACTUATORS;
					}else{
						System.err.println("Error on the getAttributesFromArgumentsString(), attributeComplete[1] don't contain the word Sensor or Actuator");
					}
				}
			}
		}
		return optionsAttribute;
	}

	public static void main(String[] args) {
		new ConfigurationAutomatorGui();
	}
	
	class OptionsAtributesListener implements ActionListener{
		
		private JComboBox<String> comboBox;
		private ArrayList<Class<?>> classesList;
		private AutomatorOptions selected;

		public OptionsAtributesListener(ArrayList<Class<?>> classesList, AutomatorOptions selected) {
			this.classesList = classesList;
			this.selected = selected;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent event) {
			try {
				currentOptions = selected;
				comboBox = (JComboBox<String>) event.getSource();
				if(!comboBox.getSelectedItem().equals("")){
					currentClassName = (String)comboBox.getSelectedItem();
					fullFillOptionsPanel(classesList, currentClassName, null);
				}else if(selected == AutomatorOptions.NETWORK){
					result.getControllersResult().clearNetwork();
					configResult.setText(result.getResult());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	class TextFieldListener implements DocumentListener{

		private JTextField textfield;
		private AutomatorOptions option;
		
		public TextFieldListener(JTextField textfield, AutomatorOptions option) {
			this.textfield = textfield;
			this.option = option;
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			switch (option) {
				case OUTPUT:
					result.setOutput("--output " + textfield.getText());
					configResult.setText(result.getResult());
					break;
				case RANDOM_SEED:
					result.setRandomSeed("--random_seed " + textfield.getText());
					configResult.setText(result.getResult());
					break;
			default:
				System.err.println("option variable received on the listener TextFieldListener is not supported by the method insertUpdate!");
				break;
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			switch (option) {
				case OUTPUT:
					result.setOutput("--output " + textfield.getText());
					configResult.setText(result.getResult());
					break;
				case RANDOM_SEED:
					result.setRandomSeed("--random_seed " + textfield.getText());
					configResult.setText(result.getResult());
					break;
			default:
				System.err.println("option variable received on the listener TextFieldListener is not supported by the method removeUpdate!");
				break;
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) { }
		
	}
	
	class LabelMouseListener implements MouseListener {
		private String helpText;
		
		public LabelMouseListener(String helpText) {
			this.helpText = helpText;
		}
		
		@Override
		public void mouseReleased(MouseEvent e) { }
		@Override
		public void mousePressed(MouseEvent e) { }
		@Override
		public void mouseExited(MouseEvent e) { 
			helpArea.setText("");
		}
		@Override
		public void mouseEntered(MouseEvent e) { 
			helpArea.setText(helpText);
		}
		@Override
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
	
}	