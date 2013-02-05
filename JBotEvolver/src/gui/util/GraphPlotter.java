//package gui.util;
//
//import java.awt.GridLayout;
//import java.awt.Insets;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.PrintWriter;
//import java.text.DecimalFormat;
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.Vector;
//import javax.swing.BorderFactory;
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JEditorPane;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTextArea;
//import javax.swing.JTextField;
//import simulation.Simulator;
//import simulation.robot.Robot;
//import simulation.robot.actuators.Actuator;
//import simulation.robot.actuators.BehaviorActuator;
//import simulation.robot.behaviors.Behavior;
//import com.panayotis.gnuplot.JavaPlot;
//import com.panayotis.gnuplot.dataset.FileDataSet;
//import com.panayotis.gnuplot.plot.DataSetPlot;
//import com.panayotis.gnuplot.style.FillStyle;
//import com.panayotis.gnuplot.style.FillStyle.Fill;
//import com.panayotis.gnuplot.style.NamedPlotColor;
//import com.panayotis.gnuplot.style.PlotStyle;
//import com.panayotis.gnuplot.style.Smooth;
//import com.panayotis.gnuplot.style.Style;
//
//import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
//import evolutionaryrobotics.mains.ResultViewerMain;
//import evolutionaryrobotics.neuralnetworks.CTRNNMultilayer;
//import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
//import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
//import evolutionaryrobotics.neuralnetworks.inputs.BehaviorNNInput;
//import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
//import evolutionaryrobotics.neuralnetworks.outputs.BehaviorNNOutput;
//import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
//import experiments.Experiment;
//
//public class GraphPlotter extends JFrame {
//
//	private static final long serialVersionUID = 1L;
//	
//	private LinkedList<JCheckBox> robotCheckboxes = new LinkedList<JCheckBox>();
//	private LinkedList<JCheckBox> inputCheckboxes = new LinkedList<JCheckBox>();
//	private LinkedList<JCheckBox> hiddenCheckboxes = new LinkedList<JCheckBox>();
//	private LinkedList<JCheckBox> outputCheckboxes = new LinkedList<JCheckBox>();
//	
//	private LinkedList<JTextField> inputTextFields = new LinkedList<JTextField>();
//	private LinkedList<JTextField> hiddenTextFields = new LinkedList<JTextField>();
//	private LinkedList<JTextField> outputTextFields = new LinkedList<JTextField>();
//	
//	private JEditorPane console = new JEditorPane();
//	
//	private LinkedList<Robot> robots;
//	private Vector<NNInput> inputs;
//	private double[] hidden;
//	private Vector<NNOutput> outputs;
//	
//	private NeuralNetwork network;
//	
//	private LinkedList<double[][]> valuesList = new LinkedList<double[][]>();
//	private LinkedList<String> titlesList = new LinkedList<String>();
//	
//	private JCheckBox smoothCheckBox = new JCheckBox("Smooth lines");
//	
//	private int currentStep = 0;
//	private int currentIndex = 0;
//	
//	private ResultViewerMain main;
//	private Simulator simulator;
//	
//	private boolean saveToFile = false;
//
//	/**
//	 * Currently this only works for robots that use a NeuralNetwork as a controller.
//	 * This constructor creates a JFrame that allows you to choose which neural network
//	 * intpu/output activations to plot 
//	 * 
//	 * @param simulator The simulator should be initialized, not in the middle of a simulation.
//	 * @param evaluationFunction If this parameter is null, the graph will be plotted
//	 * up to the maximum number of steps.
//	 */
//	public GraphPlotter(ResultViewerMain main) {
//		super("Graph Plotter");
//		
//		this.main = main;
//		try{
//			this.simulator = main.setupSimulator();
//			
//			JPanel mainPanel = new JPanel();
//			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
//	
//			robots = simulator.getExperiment().getRobots();
//	
//			NeuralNetworkController controller = (NeuralNetworkController)robots.get(0).getEvolvingController();
//			network = controller.getNeuralNetwork();
//			inputs = network.getInputs();
//			outputs = network.getOutputs();
//	
//			mainPanel.add(initRobotPanel());
//			mainPanel.add(initInputsPanel());
//			
//			JPanel hiddenPanel = initHiddenPanel();
//			if(hiddenPanel != null)
//				mainPanel.add(hiddenPanel);
//			
//			mainPanel.add(initOutputsPanel());
//	
//			JPanel buttonsPanel = new JPanel(new GridLayout(1,4));
//			
//			smoothCheckBox.setSelected(true);
//			buttonsPanel.add(smoothCheckBox);
//			
//			JButton checkAllButton = new JButton("Check all");
//			buttonsPanel.add(checkAllButton);
//			
//			JButton uncheckAllButton = new JButton("Uncheck all");
//			buttonsPanel.add(uncheckAllButton);
//			
//			JButton saveToFileButton = new JButton("Save to file");
//			buttonsPanel.add(saveToFileButton);
//	
//			JButton plotButton = new JButton("Plot!");
//			buttonsPanel.add(plotButton);
//	
//			checkAllButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					changeAllCheckboxes(true);
//				}
//			});
//			
//			uncheckAllButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					changeAllCheckboxes(false);
//				}
//			});
//			
//			plotButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					plotGraph();
//				}
//			});
//			
//			saveToFileButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					saveToFile = true;
//					plotGraph();
//					saveToFile = false;
//				}
//			});
//	
//			mainPanel.add(buttonsPanel);
//			
//			mainPanel.add(new JScrollPane(console));
//			
//			add(mainPanel);
//	
//			setLocationRelativeTo(null);
//			setResizable(false);
//			pack();
//			setVisible(true);
//		}catch(Exception e) {e.printStackTrace();}
//	}
//	
//	/**
//	 * This constructor receives a list of fitness.log files
//	 * @param files list of file names of the fitness.log files to plot
//	 */
//	public GraphPlotter(String[] files) {
//		JavaPlot plot = new JavaPlot();
//		FileDataSet fileDataSet;
//		
//		int subStringIndex = 0;
//		int amountOfGoodPlots = 0;
//		
//		for(String s : files)
//			if(!s.isEmpty())
//				amountOfGoodPlots++;
//		
//		if(files.length > 0) {
//			String s = files[0];
//			boolean stop = false;
//			int i = 0;
//			for(i = 1 ; i < s.length() && !stop; i++) {
//				String subString = s.substring(0, i);
//				for(String other : files)
//					if(!other.isEmpty() && !other.startsWith(subString)) stop = true;
//			}
//			subStringIndex = i-2;
//		}
//		
//		if(amountOfGoodPlots == 1)
//			subStringIndex=0;
//		
//		NamedPlotColor[] colors = {NamedPlotColor.RED, NamedPlotColor.BLUE, NamedPlotColor.GREEN, NamedPlotColor.ORANGE, NamedPlotColor.YELLOW,
//				NamedPlotColor.VIOLET, NamedPlotColor.PINK, NamedPlotColor.GOLDENROD, NamedPlotColor.TURQUOISE, NamedPlotColor.SALMON};
//		int colorIndex = 0;
//		
//		for(String s : files) {
//			if(!s.isEmpty()) {
//				try {
//					File f = new File(s);
//					boolean coEvolution = (new File(f.getParent()+"/_Ashowbest_current.conf").exists());
//					
//					fileDataSet = new FileDataSet(new File(s));
//					DataSetPlot dataSet = new DataSetPlot(fileDataSet);
//					PlotStyle style = getNewPlotStyle();
//					dataSet.setPlotStyle(style);
//					int currentColor = (colorIndex++)%colors.length;
//					style.setLineType(colors[currentColor]);
//					dataSet.setTitle(s.substring(subStringIndex));
//					
//					plot.addPlot(dataSet);
//					
//					//In co-evolution, the fitness is plotted in pairs
//					if(coEvolution) {
//						DataSetPlot dataSetB = new DataSetPlot(fileDataSet);
//						dataSetB.put("using", "1:5");
//						PlotStyle styleB = getNewPlotStyle();
//						styleB.setStyle(Style.LINESPOINTS);
//						dataSetB.setPlotStyle(styleB);
//						styleB.setLineType(colors[currentColor]);
//						dataSetB.setTitle(s.substring(subStringIndex)+" B");
//						dataSet.setTitle(s.substring(subStringIndex)+" A");
//						plot.addPlot(dataSetB);
//					}
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		(new Plotter(plot)).start();
//		
//		dispose();
//	}
//	
//	private class Plotter extends Thread {
//		
//		private JavaPlot plot;
//		
//		public Plotter(JavaPlot plot) {
//			this.plot = plot;
//		}
//		
//		@Override
//		public void run() {
//			plot.plot();
//		}
//	
//	}
//	
//	private PlotStyle getNewPlotStyle() {
//		PlotStyle myPlotStyle = new PlotStyle();
//		myPlotStyle.setStyle(Style.LINES);
//		myPlotStyle.setLineWidth(1);
//		return myPlotStyle;
//	}
//	
//	private void changeAllCheckboxes(boolean value) {
//		
//		for(JCheckBox c : robotCheckboxes)
//			c.setSelected(value);
//		
//		for(JCheckBox c : inputCheckboxes)
//			c.setSelected(value);
//		
//		for(JCheckBox c : hiddenCheckboxes)
//			c.setSelected(value);
//		
//		for(JCheckBox c : outputCheckboxes)
//			c.setSelected(value);
//	}
//	
//	private void plotGraph() {
//		
//		try{
//			valuesList = new LinkedList<double[][]>();
//			titlesList = new LinkedList<String>();
//			simulator = main.setupSimulator();
//			EvaluationFunction evaluationFunction = main.getEvaluationFunction();
//			Experiment experiment = simulator.getExperiment();		
//			int maxNumberOfSteps = experiment.getNumberOfStepsPerRun();
//	
//			robots = simulator.getExperiment().getRobots();
//			
//			NeuralNetworkController controller = (NeuralNetworkController)robots.get(0).getEvolvingController();
//			network = controller.getNeuralNetwork();
//			inputs = network.getInputs();
//			outputs = network.getOutputs();
//	
//			//Instantiate the needed arrays
//			for(int i = 0 ; i < robotCheckboxes.size() ; i++) {
//				if(robotCheckboxes.get(i).isSelected()) {
//					
//					for(int j = 0 ; j < inputCheckboxes.size() ; j++){
//						if(inputCheckboxes.get(j).isSelected()) {
//							
//							String[] chosenInputs = inputTextFields.get(j).getText().trim().split(",");
//							int[] chosenInts = new int[chosenInputs.length];
//							
//							for(int z = 0 ; z < chosenInputs.length ; z++)
//								chosenInts[z] = Integer.parseInt(chosenInputs[z]);
//							Arrays.sort(chosenInts);
//							
//							for(int z = 0 ; z < inputs.get(j).getNumberOfInputValues() ; z++) {
//								if(Arrays.binarySearch(chosenInts, z) >= 0) {
//									titlesList.add(getInputTitle(robots.get(i),inputs.get(j).getClass(), z));
//									valuesList.add(new double[maxNumberOfSteps][2]);
//								}
//							}
//						}
//					}
//					if(hiddenCheckboxes.get(0).isSelected()){
//						
//						String[] chosenHidden = hiddenTextFields.get(0).getText().trim().split(",");
//						int[] chosenInts = new int[chosenHidden.length];
//						
//						for(int z = 0 ; z < chosenHidden.length ; z++)
//							chosenInts[z] = Integer.parseInt(chosenHidden[z]);
//						Arrays.sort(chosenInts);
//						
//						for(int z = 0 ; z < hidden.length ; z++) {
//							if(Arrays.binarySearch(chosenInts, z) >= 0) {
//								titlesList.add("Hidden State "+ z);
//								valuesList.add(new double[maxNumberOfSteps][2]);
//							}
//						}
//					}
//					for(int j = 0 ; j < outputCheckboxes.size() ; j++){
//						if(outputCheckboxes.get(j).isSelected()){
//							
//							String[] chosenOutputs = outputTextFields.get(j).getText().trim().split(",");
//							int[] chosenInts = new int[chosenOutputs.length];
//						
//							for(int z = 0 ; z < chosenOutputs.length ; z++)
//								chosenInts[z] = Integer.parseInt(chosenOutputs[z]);
//							Arrays.sort(chosenInts);
//							
//							for(int z = 0 ; z < outputs.get(j).getNumberOfOutputValues() ; z++) {
//								if(Arrays.binarySearch(chosenInts, z) >= 0) {
//									titlesList.add(getOutputTitle(robots.get(i),outputs.get(j).getClass(), z));
//									valuesList.add(new double[maxNumberOfSteps][2]);
//								}
//							}
//						}
//					}
//				}
//			}
//			
//			NeuralNetwork network = controller.getNeuralNetwork();
//			
//			inputs = network.getInputs();
//			outputs = network.getOutputs();
//			
//			int[] numberOfInputNeurons = new int[inputs.size()];
//			for(int j = 0 ; j < inputs.size() ; j++)
//				numberOfInputNeurons[j] = inputs.get(j).getNumberOfInputValues();
//			
//			int[] numberOfHiddenInputs = new int[hidden.length > 0 ? hidden.length : 1];
//			
//			try{
//				CTRNNMultilayer multilayer = (CTRNNMultilayer)network;
//				hidden = multilayer.getHiddenStates();
//				for(int j = 0 ; j < hidden.length ; j++)
//					numberOfHiddenInputs[j] = hidden.length;
//			} catch(Exception e) {}
//			
//			int[] numberOfOutputNeurons = new int[outputs.size()];
//			for(int j = 0 ; j < outputs.size() ; j++)
//				numberOfOutputNeurons[j] = outputs.get(j).getNumberOfOutputValues();
//			
//			currentStep = 0;
//			
//			//Simulate and get the values for the inputs/outputs of the controller
//			while (!experiment.hasEnded() && currentStep < maxNumberOfSteps) {
//				
//				for(int i = 0 ; i < robotCheckboxes.size() ; i++) {
//					if(robotCheckboxes.get(i).isSelected()) {
//						
//						currentIndex = 0;
//						
//						controller = (NeuralNetworkController)robots.get(i).getEvolvingController();
//						network = controller.getNeuralNetwork();
//						
//						inputs = network.getInputs();
//						outputs = network.getOutputs();
//						
//						processLayer(inputCheckboxes, inputTextFields, network.getInputNeuronStates(), numberOfInputNeurons);
//						processLayer(outputCheckboxes, outputTextFields, network.getOutputNeuronStates(), numberOfOutputNeurons);
//						processLayer(hiddenCheckboxes, hiddenTextFields, hidden, numberOfHiddenInputs);
//					}
//				}
//	
//				simulator.performOneSimulationStep(currentStep++);
//				if (evaluationFunction != null)
//					evaluationFunction.step();
//			}
//			
////			DecimalFormat twoDForm = new DecimalFormat("#.#####");
//			
//			for(int i = 0 ; i < valuesList.size() ; i++) {
//				double[][] currentValues = valuesList.get(i);
//				double[][] newValues = new double[currentStep][2];
//				for(int j = 0 ; j < currentStep ; j++) {
//					newValues[j][0] = currentValues[j][0];
//					newValues[j][1] = currentValues[j][1];
////					String s = ""+Double.valueOf(twoDForm.format(newValues[j][1]));
////					if(s.contains("E"))
////						System.out.println(0);
////					else
////						System.out.println(s);
//				}
////				System.out.println();
//				valuesList.set(i, newValues);
//			}
//			
//			if(saveToFile) {
//				
//				String[] array = new String[valuesList.get(0).length];
//				
//				for(int i = 0 ; i < valuesList.size() ; i++) {
//					double[][] current = valuesList.get(i);
//					for(int j = 0 ; j < array.length ; j++) {
//						if(array[j] == null)
//							array[j]=""+j;
//						array[j]+=" "+current[j][1];
//					}
//				}
//				
//				File f = new File("data.csv");
//				PrintWriter pw = new PrintWriter(new FileOutputStream(f));
//				for(String s : array)
//					pw.write(s+"\n");
//				pw.close();
//				
//			} else {
//	
//				JavaPlot p = new JavaPlot();
//		
//				PlotStyle myPlotStyle = new PlotStyle();
//				myPlotStyle.setStyle(Style.LINES);
//				myPlotStyle.setLineWidth(1);
//				
//				boolean smooth = smoothCheckBox.isSelected();
//		
//				for(int i = 0 ; i < valuesList.size() ; i++) { 
//					DataSetPlot s = new DataSetPlot(valuesList.get(i));
//					s.setPlotStyle(myPlotStyle);
//					if(smooth)
//						s.setSmooth(Smooth.CSPLINES);
//					s.setTitle(titlesList.get(i));
//					p.addPlot(s);
//				}
//		
//				p.set("xrange", "[0:"+(currentStep-1)+"]");
//				(new Plotter(p)).start();
//			}
//		}catch(Exception e){e.printStackTrace();}
//	}
//	
//	private void processLayer(LinkedList<JCheckBox> checkboxes,
//			LinkedList<JTextField> textFields,
//			double[] states,
//			int[] numberOfNeurons) {
//		
//		int currentNeuron = 0;
//		
//		for(int j = 0 ; j < checkboxes.size() ; j++){
//			
//			if(checkboxes.get(j).isSelected()){
//				
//				String[] chosenNeurons = textFields.get(j).getText().trim().split(",");
//				int[] chosenInts = new int[chosenNeurons.length];
//				
//				try{
//					for(int z = 0 ; z < chosenNeurons.length ; z++)
//						chosenInts[z] = Integer.parseInt(chosenNeurons[z]);
//					Arrays.sort(chosenInts);
//					
//					for(int z = 0 ; z < numberOfNeurons[j] ; z++, currentNeuron++) {
//						if(Arrays.binarySearch(chosenInts, z) >= 0) {
//							valuesList.get(currentIndex)[currentStep][0] = currentStep;
//							valuesList.get(currentIndex++)[currentStep][1] = states[currentNeuron];
//						}
//					}
//				}catch(NumberFormatException e) {
//					e.printStackTrace();
//				}
//			}else
//				currentNeuron+=numberOfNeurons[j];
//		}
//	}
//	
//	private String getInputTitle(Robot r, Class c, int index) {
//		
//		if(c.equals(BehaviorNNInput.class)) {
//			for(Actuator a : r.getActuators()) {
//				if(a.getClass().equals(BehaviorActuator.class)) {
//					BehaviorActuator ba = (BehaviorActuator)a;
//					return ba.getBehaviors().get(index).getClass().getSimpleName();
//				}
//			}
//		}
//		return getGenericTitle(c,index);
//	}
//	
//	private String getOutputTitle(Robot r, Class c, int index) {
//		
//		if(c.equals(BehaviorNNOutput.class)) {
//			for(Actuator a : r.getActuators()) {
//				if(a.getClass().equals(BehaviorActuator.class)) {
//					BehaviorActuator ba = (BehaviorActuator)a;
//					
//					int i,b;
//					Behavior behavior = null;
//					
//					for(i = 0, b = 0 ; i < ba.getNumberOfOutputs() ; ) {
//						behavior = ba.getBehaviors().get(b);
//						
//						if(i + behavior.getNumberOfOutputs() <= index) {
//							i+= behavior.getNumberOfOutputs();
//							b++;
//						}else
//							break;
//					}
//					
//					index-= i;
//					
//					String result = behavior.getClass().getSimpleName();
//					
//					if(index > 0)
//						result+=" "+index;
//					return result;	
//				}
//			}
//		}
//		return getGenericTitle(c,index);
//	}
//	
//	private String getGenericTitle(Class c,int index) {
//		return c.getSimpleName()+" "+index;
//	}
//
//	private JPanel initRobotPanel() {
//		JPanel robotPanel = new JPanel();
//		int number = (int)Math.ceil(simulator.getExperiment().numberOfRobots/2);
//		robotPanel.setLayout(new GridLayout(number,2));
//		robotPanel.setBorder(BorderFactory.createTitledBorder("Robots"));
//
//		for(int i = 0 ; i < robots.size() ; i++) {
//			JCheckBox checkbox = new JCheckBox("Robot "+i);
//			checkbox.setSelected(true);
//			robotCheckboxes.add(checkbox);
//			robotPanel.add(checkbox);
//		}
//		return robotPanel;
//	}
//
//	private JPanel initInputsPanel() {
//		JPanel inputPanel = new JPanel();
//		inputPanel.setLayout(new GridLayout(inputs.size(),2));
//		inputPanel.setBorder(BorderFactory.createTitledBorder("Inputs"));
//
//		for(int i = 0 ; i < inputs.size() ; i++) {
//			NNInput input = inputs.get(i);
//			
//			String name = input.getClass().getSimpleName() +" ("+input.getNumberOfInputValues()+")";
//			JCheckBox checkbox = new JCheckBox(name);
//			checkbox.setSelected(true);
//			
//			String text = "";
//			for(int j = 0 ; j < input.getNumberOfInputValues() ; j++)
//				text+= j == input.getNumberOfInputValues()-1 ? j : j+","; 
//			
//			inputCheckboxes.add(checkbox);
//			inputTextFields.add(new JTextField(text));
//			
//			inputPanel.add(checkbox);
//			inputPanel.add(inputTextFields.peekLast());
//		}
//		return inputPanel;
//	}
//	
//	private void printHiddenTaus() {
//		
//		String text = "";
//		
//		for(int i = 0 ; i < robotCheckboxes.size() ; i++) {
//			if(robotCheckboxes.get(i).isSelected()) {
//				text+="Hidden Taus for Robot #"+i+"\n";
//				Robot r = robots.get(i);
//				CTRNNMultilayer network = (CTRNNMultilayer)((NeuralNetworkController)r.getController()).getNeuralNetwork();
//				double[] hiddenTaus = network.getHiddenTaus();
//				
//				for(int j = 0 ; j < hiddenTaus.length ; j++) {
//					text+=hiddenTaus[j];
//					text+="\n";
//				}
//				text+="\n";
//			}
//		}
//		new ConsoleFrame(text);
//	}
//	
//	private JPanel initHiddenPanel() {
//		try{
//			JPanel hiddenPanel = new JPanel();
//			hiddenPanel.setLayout(new GridLayout(2,2));
//			hiddenPanel.setBorder(BorderFactory.createTitledBorder("Hidden"));
//			
//			CTRNNMultilayer multilayer = (CTRNNMultilayer)network;
//			
//			hidden = multilayer.getHiddenStates();
//			
//			JCheckBox checkbox = new JCheckBox("Hidden States");
//			checkbox.setSelected(true);
//			
//			String text = "";
//			
//			for(int i = 0 ; i < hidden.length ; i++)
//				text+= i == hidden.length-1 ? i : i+","; 
//			
//			hiddenCheckboxes.add(checkbox);
//			hiddenTextFields.add(new JTextField(text));
//			
//			JButton hiddenTausButton = new JButton("Print Hidden Taus");
//			hiddenTausButton.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent arg0) {
//					printHiddenTaus();					
//				}
//			});
//			
//			hiddenPanel.add(checkbox);
//			hiddenPanel.add(hiddenTextFields.peekLast());
//			
//			hiddenPanel.add(hiddenTausButton);
//			
//			return hiddenPanel;
//			
//		} catch(Exception e) {}
//		
//		return null;
//	}
//
//	private JPanel initOutputsPanel() {
//		JPanel outputPanel = new JPanel();
//		outputPanel.setLayout(new GridLayout(outputs.size(),2));
//		outputPanel.setBorder(BorderFactory.createTitledBorder("Outputs"));
//
//		for(int i = 0 ; i < outputs.size() ; i++) {
//			NNOutput a = outputs.get(i);
//			String name = a.getClass().getSimpleName() +" ("+a.getNumberOfOutputValues()+")";
//			JCheckBox checkbox = new JCheckBox(name);
//			checkbox.setSelected(true);
//			
//			String text = "";
//			for(int j = 0 ; j < outputs.get(i).getNumberOfOutputValues() ; j++)
//				text+= j == outputs.get(i).getNumberOfOutputValues()-1 ? j : j+","; 
//			
//			outputCheckboxes.add(checkbox);
//			outputTextFields.add(new JTextField(text));
//			
//			outputPanel.add(checkbox);
//			outputPanel.add(outputTextFields.peekLast());
//		}
//		return outputPanel;
//	}
//	
//	private class ConsoleFrame extends JFrame {
//		
//		public ConsoleFrame(String s) {
//			JTextArea a = new JTextArea(s);
//			a.setMargin(new Insets(5,5,5,5));
//			add(a);
//			pack();
//			setVisible(true);
//		}
//		
//	}
//}
