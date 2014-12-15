package gui;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import gui.renderer.Renderer;
import gui.util.Editor;
import gui.util.GraphPlotter;
import gui.util.GraphViz;
import gui.util.NetworkViewer;
import gui.util.PostEvaluationData;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.Updatable;
import simulation.util.Arguments;
import updatables.BlenderExport;

public class ResultViewerGui extends Gui {
//	protected JFrame      frame;
	protected JPanel frame;
	protected JTextField  controlStepTextField;	
	protected JTextField  fitnessTextField;

	protected JTextField  controlStepTimeTextField;
	protected JTextField  rendererTimeTextField;
	
	protected NetworkViewer networkViewer = new NetworkViewer();

	protected int         sleepBetweenControlSteps = 10;
	
	JPanel treeWrapper;

	protected JButton     pauseButton;
	protected JButton		plotButton;
	protected JButton		newRandomSeedButton = new JButton("New Random Seed");

	protected JSlider playPosition = new JSlider(0,100);
	protected JSlider sleepSlider = new JSlider(0,100);

	protected JEditorPane extraArguments;

	protected FileTree fileTree;
	protected JTextField currentFileTextField = new JTextField(18);
	protected JButton	loadButton	= new JButton("Load");
	protected JButton editButton = new JButton("Edit");
	protected JButton plotFitnessButton = new JButton("Plot Fitness");
	protected JButton compareFitnessButton = new JButton("Compare Fitness");
	
	protected JLabel fitnessSummary = new JLabel("");

	static final int RUN         = 2;
	static final int PAUSED      = 3;
	static final int STOPPED     = 4;	
	static final int ENDED       = 5;
	
	protected int simulateUntil = 0;

	protected int simulationState = STOPPED;

	protected Renderer renderer;
	protected Simulator simulator;
	protected JBotEvolver jBotEvolver;
	protected EvaluationFunction evaluationFunction;
	
	protected boolean readyToSkip = true;
	
	protected Thread worker;
	
	protected GraphViz graphViz = null;
	protected boolean showNeuralNetwork = false;
	protected JCheckBox neuralNetworkCheckbox;
	protected JCheckBox neuralNetworkViewerCheckbox;
	protected JCheckBox exportToBlender;
	
	private boolean enableDebugOptions = false;
	
	protected EnvironmentKeyDispatcher dispatcher;

	public ResultViewerGui(JBotSim jBotEvolver, Arguments args) {
		super(jBotEvolver,args);
		this.jBotEvolver = (JBotEvolver)jBotEvolver;
		
		if(args.getArgumentIsDefined("renderer")) {
			createRenderer(new Arguments(args.getArgumentAsString("renderer")));
		}
		
		enableDebugOptions = args.getArgumentAsIntOrSetDefault("enabledebugoptions", 0) == 1;
		
		frame = new JPanel();
		frame.setLayout(new BorderLayout());
		super.setGuiPanel(frame);
	
		frame.add(initRightWrapperPanel(), BorderLayout.EAST);
		frame.add(initLeftWrapperPanel(), BorderLayout.WEST);

		initActions();
		initListeners();
		
		frame.setVisible(true);
	}

	protected JPanel initLeftWrapperPanel() {

		treeWrapper = new JPanel(new BorderLayout());

		fileTree = new FileTree(new File("."));

		JPanel argumentsPanel = new JPanel(new BorderLayout());
		extraArguments = new JEditorPane();
		argumentsPanel.add(new JLabel("Extra arguments"),BorderLayout.NORTH);
		argumentsPanel.add(new JScrollPane(extraArguments),BorderLayout.CENTER);
		
		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		topPanel.add(currentFileTextField);
		currentFileTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(fileTree);
		fileTree.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(fitnessSummary);
		fitnessSummary.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(editButton);
		
		JPanel editLoad = new JPanel();
		editLoad.add(editButton);
		editLoad.add(loadButton);
		
		editLoad.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(editLoad);
		
		topPanel.add(compareFitnessButton);
		compareFitnessButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		topPanel.add(plotFitnessButton);
		plotFitnessButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		treeWrapper.add(topPanel, BorderLayout.NORTH);
		treeWrapper.add(argumentsPanel, BorderLayout.CENTER);
		treeWrapper.add(newRandomSeedButton, BorderLayout.SOUTH);
		
		treeWrapper.setBorder(BorderFactory.createTitledBorder("Experiments"));

		return treeWrapper;
	}

	protected JPanel initRightWrapperPanel() {
		
		int panelWidth = 150;

		JPanel sideTopPanel = new JPanel();
		sideTopPanel.setLayout(new BoxLayout(sideTopPanel, BoxLayout.Y_AXIS));
		
		pauseButton = new JButton("Start/Pause");
		plotButton = new JButton("Plot Neural Activations");
		
		pauseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideTopPanel.add(pauseButton);
		plotButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideTopPanel.add(plotButton);
		
		sideTopPanel.add(new JLabel(" "));
		
		JLabel sleep = new JLabel("Sleep between control steps (ms)");
		sideTopPanel.add(sleep);
		sleep.setAlignmentX(Component.CENTER_ALIGNMENT);

		sleepSlider.setMajorTickSpacing(20);
		sleepSlider.setMinorTickSpacing(5);
		sleepSlider.setPaintTicks(true);
		sleepSlider.setPaintLabels(true);
		sleepSlider.setValue(10);

		sleepSlider.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideTopPanel.add(sleepSlider);

		sideTopPanel.add(new JLabel("Play position (%)"));
		playPosition.setMajorTickSpacing(20);
		playPosition.setMinorTickSpacing(5);
		playPosition.setPaintTicks(true);
		playPosition.setPaintLabels(true);
		playPosition.setValue(0);
		playPosition.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideTopPanel.add(playPosition);
		
		sideTopPanel.add(new JLabel(" "));
		
		if(enableDebugOptions) {
		
			neuralNetworkCheckbox = new JCheckBox("Show Neural Network");
			neuralNetworkCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
			sideTopPanel.add(neuralNetworkCheckbox);
		/*	
			neuralNetworkViewerCheckbox = new JCheckBox("Show Neural Network #2");
			neuralNetworkViewerCheckbox.setAlignmentX(Component.CENTER_ALIGNMENT);
			sideTopPanel.add(neuralNetworkViewerCheckbox);
		*/
			exportToBlender = new JCheckBox("Export to Blender");
			exportToBlender.setAlignmentX(Component.CENTER_ALIGNMENT);
			sideTopPanel.add(exportToBlender);
			
			sideTopPanel.add(new JLabel(" "));
		}

		//Status panel
		JPanel statusPanel = new JPanel(new GridLayout(3,2));
		statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		statusPanel.add(new JLabel("Control step: "));
		controlStepTextField = new JTextField("N/A");
		controlStepTextField.setHorizontalAlignment(JTextField.CENTER);
		statusPanel.add(controlStepTextField);

		statusPanel.add(new JLabel("Fitness: "));
		fitnessTextField = new JTextField("N/A");
		fitnessTextField.setHorizontalAlignment(JTextField.CENTER);
		statusPanel.add(fitnessTextField);
		
		sideTopPanel.add(statusPanel);
		statusPanel.setPreferredSize(new Dimension(panelWidth,100));
		
		JPanel sideWrapperPanel = new JPanel(new BorderLayout());
		sideWrapperPanel.add(sideTopPanel,BorderLayout.NORTH);
		sideWrapperPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
		
		pauseButton.setPreferredSize(new Dimension(panelWidth,50));
		plotButton.setPreferredSize(new Dimension(panelWidth,50));

		return sideWrapperPanel;
	}

	protected void initActions() {
		
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control ENTER"), "control ENTER");
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta ENTER"), "control ENTER");
		((JComponent) frame).getActionMap().put("control ENTER", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				loadCurrentFile();
			}
		});
		
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control P"), "control P");
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta P"), "control P");
		((JComponent) frame).getActionMap().put("control P", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				startPauseButton();
			}
		});
		
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt LEFT"), "alt LEFT");
		((JComponent) frame).getActionMap().put("alt LEFT", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveLeft();
			}
		});
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt RIGHT"), "alt RIGHT");
		((JComponent) frame).getActionMap().put("alt RIGHT", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveRight();
			}
		});
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt UP"), "alt UP");
		((JComponent) frame).getActionMap().put("alt UP", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveUp();
			}
		});
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt DOWN"), "alt DOWN");
		((JComponent) frame).getActionMap().put("alt DOWN", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.moveDown();
			}
		});
		
		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "+");  
		((JComponent) frame).getActionMap().put("+", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.zoomIn();
				renderer.drawFrame();
			}
		});  

		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "-");  
		((JComponent) frame).getActionMap().put("-", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.zoomOut();
				renderer.drawFrame();
			}
		});  

		((JComponent) frame).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('*'), "*");  
		((JComponent) frame).getActionMap().put("*", new AbstractAction(){  
			protected static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				renderer.resetZoom();
				renderer.drawFrame();
			}
		});  
	}

	protected void initListeners() {

		sleepSlider.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
				sleepBetweenControlSteps = sleepSlider.getValue();
			}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});

		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startPauseButton();
			} 		
		});

		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(worker != null)
					worker.interrupt();
				loadCurrentFile();
			} 		
		});

		playPosition.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
				shiftSimulationBy(playPosition.getValue(),true);
			}
			public void mousePressed(MouseEvent arg0) {
				simulationState = PAUSED;
			}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});

		currentFileTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				loadCurrentFile();
			}
		});

		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new Editor(currentFileTextField.getText());
			}
		});

		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(validFile(currentFileTextField.getText())) {
					if(simulator != null)
						simulator.stopSimulation();
					try{
						simulationState = PAUSED;
						jBotEvolver.loadFile(currentFileTextField.getText(), extraArguments.getText());
						new GraphPlotter(jBotEvolver,loadSimulator());
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		
		plotFitnessButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plotFitness();
			}
		});
		
		compareFitnessButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				compareFitness();
			}
		});
		
		newRandomSeedButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int newRandomSeed = new Random().nextInt(Integer.MAX_VALUE);
				
				if(extraArguments.getText().isEmpty()){
					extraArguments.setText("--random-seed " + newRandomSeed);
				}else{
					String finalText = "";
					boolean findSeedText = false;
					Scanner scanner = new Scanner(extraArguments.getText());
					while(scanner.hasNextLine()){
						String line = scanner.nextLine();
						if(line.startsWith("--random-seed")){
							finalText += "--random-seed " + newRandomSeed + "\n";
							findSeedText = true;
						}else{
							finalText += line + "\n";
						}
					}
					scanner.close();
					if(!findSeedText){
						finalText += "--random-seed " + newRandomSeed + "\n";
					}
					extraArguments.setText(finalText);
				}
				loadButton.doClick();
			}
		});
		
		if(enableDebugOptions) {
			neuralNetworkCheckbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JCheckBox check = (JCheckBox)arg0.getSource();
					showNeuralNetwork = check.isSelected();
				}
			});
		/*	
			neuralNetworkViewerCheckbox.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					JCheckBox check = (JCheckBox)arg0.getSource();
					networkViewer.setVisible(check.isSelected());
				}
			});
		*/
		}
	}
	
	protected void plotFitness() {
		File f = new File(currentFileTextField.getText().trim());
		final String mainFolder = f.isDirectory() ? f.getAbsolutePath() : f.getParent();

		Thread t = new Thread( new Runnable(){
			public void run(){
				new GraphPlotter(getFitnessFiles(mainFolder).split("###"));
			}
		});
		t.start();
	}
	
	protected String getFitnessFiles(String folder) {
		
		File f = new File(folder+"/_fitness.log");
		
		try {
			if(f.exists()){
				return f.getAbsolutePath();
			} else {
				String[] directories = (new File(folder)).list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return (new File(dir,name)).isDirectory();
					}
				});
				String result = "";
				if(directories != null){
					for(String dir : directories){
						String dirResult = getFitnessFiles(folder+"/"+dir);
						if(!dirResult.isEmpty())
							result+=dirResult+"###";
					}
				}
				return result;
			}
		}catch(Exception e ){
			e.printStackTrace();
		}
		return "";
	}

	protected void compareFitness(){
		TreePath[] selectedFiles = fileTree.getSelectedFilesPaths();
		String parentpath = "";
		ArrayList<PostEvaluationData> postsInformations = new ArrayList<PostEvaluationData>();
		
		for (TreePath treePath : selectedFiles) {
			String path = "";
			
			if(treePath.getParentPath() == null){
				path = treePath.getLastPathComponent().toString();
			}else{
				if(parentpath.equals("")){
					parentpath = treePath.getParentPath().toString().replace("[", "");
					parentpath = parentpath.replace("]", "");
				}
				
				path = parentpath + "/" + treePath.getLastPathComponent();
			}
			postsInformations.addAll(getInformationFromPostEvaluation(path));
		}
		
		JFrame comparisonFrame = new JFrame("Setups Comparison");
		PostEvaluationTableModel postsModel = new PostEvaluationTableModel(postsInformations);
		JTable comparisonTable = new JTable(postsModel);
		JScrollPane comparisonScrollPane = new JScrollPane(comparisonTable);
		comparisonFrame.add(comparisonScrollPane);
		comparisonFrame.pack();
		comparisonFrame.setLocationRelativeTo(frame);
		comparisonFrame.setVisible(true);
		
		comparisonTable.getColumn("Folder").setPreferredWidth(250);
		comparisonTable.getColumn("Best").setPreferredWidth(1);
		comparisonTable.getColumn("Fitness").setPreferredWidth(1);
		comparisonTable.getColumn("Average").setPreferredWidth(1);
		
		postsModel.fireTableDataChanged();
	}
	
	protected ArrayList<PostEvaluationData> getInformationFromPostEvaluation(String folder) {
		File f = new File(folder+"/post.txt");
		ArrayList<PostEvaluationData> postDataList = new ArrayList<PostEvaluationData>();
		
		if(f.exists()){
			try {
				postDataList.add(getDataFromPost(f));
				return postDataList;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			String[] directories = (new File(folder)).list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return (new File(dir,name)).isDirectory();
				}
			});
			
			if(directories != null){
				for(String dir : directories){
					postDataList.addAll(getInformationFromPostEvaluation(folder+"/"+dir));
				}
			}
			return postDataList;
		}
		
		return null;
	}

	private PostEvaluationData getDataFromPost(File f) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(f));
		String line = reader.readLine();
		String[] splitSetupName;
		
		if (System.getProperty("os.name").contains("Windows")) {
			splitSetupName = f.getAbsolutePath().replace("\\", "/").split("/");
		}else{
			splitSetupName = f.getAbsolutePath().split("/");
		}
		String setupName = splitSetupName[splitSetupName.length-2];
		
		String number = "";
		double chromosomeFitness = 0;
		double overall = 0;
		
		while (line != null) {
			if(line.contains("#")){
				number = line.substring(line.indexOf(" "), line.length()).trim();
			}else if(line.contains("Overall")){
				overall = Double.valueOf(line.split(" ")[1]);
			}else{
				String[] splitString = line.split(" ");
				if(splitString[0].equals(number)){
					chromosomeFitness = Double.valueOf(line.substring(line.indexOf("(")+1).split(" ")[0]);
				}
			}
			
			line = reader.readLine();
		}
		
		reader.close();
		
		return new PostEvaluationData(setupName, Integer.valueOf(number) ,chromosomeFitness, overall);
	}
	
	public void dispose() {
		frame.setVisible(false);
	}
	
//	protected void startButton() {
//		simulationState = RUN;
//		simulateUntil = 0;
//		readyToSkip = true;
//		if(simulator != null && simulator.simulationFinished())
//			loadCurrentFile();
//	}
	
	protected void startPauseButton() {
		
		if(simulator != null && simulator.simulationFinished()) {
			simulationState = RUN;
			loadButton.doClick();
		} else	if (simulationState == RUN)
			simulationState = PAUSED;
		else
			simulationState = RUN;
	}

	protected void shiftSimulationBy(int value, boolean percentage) {
		
		try {
			if(readyToSkip || (simulator != null && simulator.simulationFinished())) {
				int maxSteps = simulator.getEnvironment().getSteps();
				
				if(!percentage) {
					double realStep = simulator.getTime()-1;
					value+=realStep;
					value = value > maxSteps ? maxSteps : (value < 1 ? 1 : value);
					simulateUntil = value;
				}else {
					value = value > 100 ? 100 : (value < 1 ? 1 : value);
					double percent = value/100.0;
					int targetStep = (int)(percent*maxSteps);
					simulateUntil = targetStep;
				}
				simulationState = PAUSED;
				loadCurrentFile();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public synchronized void update(Simulator simulator) {
		
		if(simulateUntil == 0) {
			
			readyToSkip = true;
		
			if(dispatcher != null)
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
			dispatcher = new EnvironmentKeyDispatcher(simulator);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
			
			if(showNeuralNetwork && graphViz == null) {
				NeuralNetworkController nn = (NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getController();
				graphViz = new GraphViz(nn.getNeuralNetwork());
			}
			if(showNeuralNetwork)
				graphViz.show();
	
			updateStatus();
			
			try {
				if (sleepBetweenControlSteps > 0)
					Thread.sleep(sleepBetweenControlSteps);
				
				while(simulationState != RUN)
					Thread.sleep(10);
			} catch(Exception e) {}
			
			if (simulationState == RUN) {
				if(showNeuralNetwork) {
					if(graphViz != null)
						graphViz.changeNeuralNetwork(((NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getController()).getNeuralNetwork());
					else
						graphViz = new GraphViz(((NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getController()).getNeuralNetwork());
						
				}
			}
		}else if(simulateUntil <= simulator.getTime()){
			simulateUntil = 0;
			updateStatus();
			simulationState = PAUSED;
			readyToSkip = true;
		}else
			readyToSkip = false;
		
	}
	
	protected void updateStatus() {
		controlStepTextField.setText("" + simulator.getTime().intValue());
		fitnessTextField.setText(String.format("%12.6f", evaluationFunction.getFitness()));
		updatePlaySlider(simulator.getTime(), simulator.getEnvironment().getSteps());
		renderer.drawFrame();
	}

	protected void updatePlaySlider(double step, double maxSteps) {
		int value = (int)(step/maxSteps*100);
		playPosition.setValue(value);
	}

	protected boolean validFile(String filename) {
		File f = new File(filename);
		return f.exists() && !f.isDirectory();
	}

	protected void loadCurrentFile() {
		String filename = currentFileTextField.getText();

		try{
			if(validFile(filename)) {
				
				String extra = "";
				if (System.getProperty("os.name").contains("Windows")) {
					extra = extraArguments.getText();
					extra = extra.replaceAll("\r", "");
				}else{
					extra = extraArguments.getText();
				}
				
				jBotEvolver.loadFile(filename,extra);
				simulator = loadSimulator();
				if(exportToBlender != null && exportToBlender.isSelected())
					simulator.addCallback(new BlenderExport());
				if(simulateUntil == 0)
					playPosition.setValue(0);

				launchSimulation();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public class FileTree extends JPanel {

		protected static final long serialVersionUID = 1L;
		protected String currentFilename = "";
		protected JTree tree;
		protected DefaultTreeModel model;

		public FileTree(File dir) {
			setLayout(new BorderLayout());
			
			DefaultMutableTreeNode nodes = addNodes(null, dir);

			model = new DefaultTreeModel(nodes);
			tree = new JTree(model);
			
			addTree();
		}
		
		protected void addTree() {
			
			tree.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
			tree.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");
			tree.getInputMap().put(KeyStroke.getKeyStroke("meta LEFT"), "none");
			tree.getInputMap().put(KeyStroke.getKeyStroke("meta RIGHT"), "none");
			
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					TreePath tp = e.getPath();

					String filename = "";

					for(int i = 0 ; i < tp.getPathCount() ; i++) {
						filename+= tp.getPathComponent(i);
						if(i != tp.getPathCount()-1)
							filename+="/";
					}

					File f = new File(filename);
					
					if(f.exists()){
						currentFilename = filename;
						if(!currentFileTextField.getText().equals(filename))
							currentFileTextField.setText(filename);
					}
				}
			});
			
			tree.addMouseListener(new MouseListener() {
				public void mousePressed(MouseEvent arg0) {
					if(arg0.getClickCount() == 2) {
						File f = (new File(currentFileTextField.getText()));
						if(f.isDirectory())
							changeDirectory(f.getAbsolutePath());
						else
							loadCurrentFile();
					}						
				}
				public void mouseReleased(MouseEvent arg0) {}
				public void mouseExited(MouseEvent arg0) {}
				public void mouseEntered(MouseEvent arg0) {}
				public void mouseClicked(MouseEvent arg0) {}
			});

			JScrollPane scroll = new JScrollPane(tree);
			scroll.setPreferredSize(new Dimension(230,300));

			removeAll();
			add(BorderLayout.CENTER, scroll);
		}
		
		public void changeDirectory(String dir) {
			File f = new File(dir);
			try {
				String filename = f.getCanonicalPath();
				DefaultMutableTreeNode nodes = addNodes(null, new File(filename));
				tree = new JTree(nodes);
				addTree();
				treeWrapper.revalidate();
				frame.invalidate();
			}catch(Exception e){e.printStackTrace();}
			
			updateFitnessSummary(dir);
		}
		
		public void updateFitnessSummary(String dir) {
			
			try {
				File f = new File(dir+"/post.txt");
				
				if(f.exists()) {
					PostEvaluationData d = getDataFromPost(f);
					fitnessSummary.setText("Run: "+d.getBestFitnessNumber()+"; Best: "+d.getBestFitness()+"; Average: "+d.getAverageFitness());
				} else {
					fitnessSummary.setText("");
				}
			
			}catch(IOException e) {
				e.printStackTrace();
			}
		}

		public String getCurrentFilename() {
			return currentFilename;
		}
		
		public TreePath[] getSelectedFilesPaths(){
			return tree.getSelectionPaths();
		}

		protected DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
			
			String curPath = dir.getAbsolutePath();
			DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir.getAbsolutePath());
			
			if(curTop != null)
				curDir = curTop;
			
			Vector<String> ol = new Vector<String>();
			String[] tmp = dir.list();
			for (int i = 0; i < tmp.length; i++)
				ol.addElement(tmp[i]);
			Collections.sort(ol, new Comparator<String>() {
			    @Override
			    public int compare(String o1, String o2) {
			       if(!o1.startsWith("showbest"))
			    	   return o1.compareTo(o2);
			       String oo1 = o1.substring(8, o1.indexOf("."));
			       String oo2 = o2.substring(8, o2.indexOf("."));
			       return Integer.parseInt(oo1)-Integer.parseInt(oo2);
			    }
			});
			
			curDir.add(new DefaultMutableTreeNode(".."));
			
			for (int i = 0; i < ol.size(); i++) {
				String thisObject = (String) ol.elementAt(i);
				String newPath = curPath + File.separator + thisObject;
				File f = new File(newPath);
				
				if(!f.getName().startsWith(".")) {
					DefaultMutableTreeNode DefaultMutableTreeNode = new DefaultMutableTreeNode(f.getName());
					if(f.isDirectory())
						DefaultMutableTreeNode.add(new DefaultMutableTreeNode());
					curDir.add(DefaultMutableTreeNode);
				}
			}
			return curDir;
		}
	}
	
	protected void launchSimulation() {
		
		if(worker != null)
			worker.interrupt();
		
		worker = new Thread(new SimulationRunner(simulator));
		worker.start();
	}
	
	protected void createRenderer(Arguments args) {
		if(args.getArgumentIsDefined("classname")) {
			this.renderer = Renderer.getRenderer(args);
		}
	}
	
	public Simulator loadSimulator() {
		
		if(simulator != null)
			simulator.stopSimulation();
		
		HashMap<String,Arguments> args = jBotEvolver.getArguments();
		
		if(renderer != null)
			frame.remove(renderer);
		
		if(args.get("--gui") != null && args.get("--gui").getArgumentIsDefined("renderer"))
			createRenderer(new Arguments(args.get("--gui").getArgumentAsString("renderer")));
		
		Simulator simulator = jBotEvolver.createSimulator();

		evaluationFunction = jBotEvolver.getEvaluationFunction();

		simulator.addCallback(evaluationFunction);
		
		if(networkViewer.isVisible())
			simulator.addCallback(networkViewer);
		
		jBotEvolver.setupBestIndividual(simulator);
		
		simulator.addCallback(this);
		simulator.setupEnvironment();
		
		for(Updatable up : simulator.getCallbacks()) {
			if(up instanceof EvaluationFunction) {
				this.evaluationFunction = (EvaluationFunction)up;
				break;
			}
		}
		 
		if (renderer != null) {
			renderer.enableInputMethods(true);
			renderer.setSimulator(simulator);
			frame.add(renderer);
			if(simulateUntil == 0)
				renderer.drawFrame();
			frame.validate();
		}
		
		if(simulateUntil == 0) {
			controlStepTextField.setText("0");
			fitnessTextField.setText("0");
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new EnvironmentKeyDispatcher(simulator));
		
		return simulator;
	}
	
	public class SimulationRunner implements Runnable {
		protected Simulator sim;
		public SimulationRunner(Simulator sim) {
			this.sim = sim;
		}
		@Override
		public void run() {
			sim.simulate();
		}
	}
	
	class PostEvaluationTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		private ArrayList<PostEvaluationData> postDataList;

		public PostEvaluationTableModel(ArrayList<PostEvaluationData> postDataList) {
			this.postDataList = postDataList;
		}
		
		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public int getRowCount() {
			return postDataList.size();
		}

		public String getColumnName(int x) {
			switch (x) {
			case 0:
				return "Folder";
			case 1:
				return "Best";
			case 2:
				return "Fitness";
			case 3:
				return "Average";
			}
			return "Unknown";
		}

		@Override
		public Object getValueAt(int y, int x) {
			PostEvaluationData postData = postDataList.get(y);
			
			switch (x) {
			case 0:
				return postData.getFolder();
			case 1:
				return postData.getBestFitnessNumber();
			case 2:
				return postData.getBestFitness();
			case 3:
				return postData.getAverageFitness();
			}
			return "Unknown";
		}

	}
	
}