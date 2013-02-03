package gui;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.mains.ResultViewerMain;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import evolutionaryrobotics.neuralnetworks.inputs.SysoutNNInput;
import experiments.Experiment;
import gui.renderer.Renderer;
import gui.util.Editor;
import gui.util.GraphPlotter;
import gui.util.GraphViz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import simulation.Simulator;

public class ResultViewerGui implements Gui {
	private JFrame      frame;
	private JTextField  simulationTimeTextField;
	private JTextField  controlStepTextField;	
	private JTextField  fitnessTextField;
	private JTextField  shiftTextField;

	private JTextField  controlStepTimeTextField;
	private JTextField  rendererTimeTextField;	

	private double      maxFramesPerSecond       = 25; 	
	private int         sleepBetweenControlSteps = 10;
	
	JPanel treeWrapper;

	private JButton     startButton = new JButton("Start");
	private JButton     quitButton  = new JButton("Quit");
	private JButton     pauseButton = new JButton("Pause");
	private JButton		plotButton = new JButton("Plot Graph");
	private JButton		shiftButton = new JButton("Set");

	private JSlider playPosition = new JSlider(0,100);
	private JSlider sleepSlider = new JSlider(0,100);

	private JEditorPane extraArguments = new JEditorPane();

	private FileTree fileTree;
	private JTextField currentFileTextField = new JTextField(18);
	private JButton	loadButton	= new JButton("Load");
	private JButton editButton = new JButton("Edit");
	private JButton plotFitnessButton = new JButton("Plot Fitness");

	static final int NONE        = 1;
	static final int RUN         = 2;
	static final int PAUSED      = 3;
	static final int STOPPED     = 4;	
	static final int ENDED       = 5;
	
	private int position_shift = 10;
	private int simulateUntil = 0;
	private int currentStep = 0;

	private int simulationState = NONE;

	private Renderer renderer;
	protected Simulator simulator;
	private ResultViewerMain main;

	private Worker workerThread;
	
	private boolean readyToSkip = true;
	
	private GraphViz graphViz = null;
	private boolean showNeuralNetwork = false;
	private JCheckBox neuralNetworkCheckbox;
	
	private EnvironmentKeyDispatcher dispatcher;

	public ResultViewerGui(Simulator simulator, Renderer renderer, ResultViewerMain main) {
		this.simulator = simulator;
		this.renderer = renderer;
		this.main = main;
		frame = new JFrame("Result Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1200, 700);
	
		frame.getContentPane().add(initBottomPanel(), BorderLayout.SOUTH);	
		frame.getContentPane().add(initRightWrapperPanel(), BorderLayout.EAST);
		frame.getContentPane().add(initLeftWrapperPanel(), BorderLayout.WEST);

		initActions();
		initListeners();
		
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private JPanel initLeftWrapperPanel() {

		treeWrapper = new JPanel();
		treeWrapper.setPreferredSize(new Dimension(250,100));

		fileTree = new FileTree(new File("."));

		JPanel argumentsPanel = new JPanel(new BorderLayout());
		argumentsPanel.setPreferredSize(new Dimension(230,100));
		argumentsPanel.add(new JLabel("Extra arguments"),BorderLayout.NORTH);
		argumentsPanel.add(new JScrollPane(extraArguments),BorderLayout.CENTER);
		
		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
		extraArguments.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");

		treeWrapper.add(currentFileTextField);
		treeWrapper.add(fileTree);
		treeWrapper.add(editButton);
		treeWrapper.add(loadButton);
		treeWrapper.add(argumentsPanel);
		treeWrapper.add(plotFitnessButton);

		treeWrapper.setBorder(BorderFactory.createTitledBorder("Experiments"));

		return treeWrapper;
	}

	private JPanel initRightWrapperPanel() {

		JPanel sideTopPanel = new JPanel();
		sideTopPanel.setLayout(new GridLayout(11,1));
		sideTopPanel.add(startButton);
		sideTopPanel.add(pauseButton);
		sideTopPanel.add(quitButton);
		sideTopPanel.add(plotButton);

		sideTopPanel.add(new JLabel(" Sleep between control steps (ms)"));

		sleepSlider.setMajorTickSpacing(20);
		sleepSlider.setMinorTickSpacing(5);
		sleepSlider.setPaintTicks(true);
		sleepSlider.setPaintLabels(true);
		sleepSlider.setValue(10);

		sideTopPanel.add(sleepSlider);

		sideTopPanel.add(new JLabel(" Play position (%)"));
		playPosition.setMajorTickSpacing(20);
		playPosition.setMinorTickSpacing(5);
		playPosition.setPaintTicks(true);
		playPosition.setPaintLabels(true);
		playPosition.setValue(0);
		sideTopPanel.add(playPosition);
		
		sideTopPanel.add(new JLabel(" Number of steps to shift (ctrl+arrows)"));
		JPanel buttonPanel = new JPanel();
		shiftTextField = new JTextField(5);
		shiftTextField.setText(""+position_shift);
		buttonPanel.add(shiftTextField);
		buttonPanel.add(shiftButton);
		sideTopPanel.add(buttonPanel);
		
		neuralNetworkCheckbox = new JCheckBox("Show Neural Network");
		sideTopPanel.add(neuralNetworkCheckbox);

		JPanel sideWrapperPanel = new JPanel();
		sideWrapperPanel.setLayout(new BorderLayout());
		sideWrapperPanel.add(sideTopPanel, BorderLayout.NORTH);

		sideWrapperPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

		return sideWrapperPanel;
	}

	private JPanel initBottomPanel() {

		JPanel bottomPanel   = new JPanel();

		bottomPanel.add(new JLabel("Simulation time: "));
		simulationTimeTextField = new JTextField("N/A");
		simulationTimeTextField.setPreferredSize(new Dimension(50, 20));
		simulationTimeTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(simulationTimeTextField);

		bottomPanel.add(new JLabel("Control step: "));
		controlStepTextField = new JTextField("N/A");
		controlStepTextField.setPreferredSize(new Dimension(50, 20));
		controlStepTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(controlStepTextField);

		bottomPanel.add(new JLabel("Fitness: "));
		fitnessTextField = new JTextField("N/A");
		fitnessTextField.setPreferredSize(new Dimension(100, 20));
		fitnessTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(fitnessTextField);

		bottomPanel.add(new JLabel("CPU time/control step: "));
		controlStepTimeTextField = new JTextField("N/A");
		controlStepTimeTextField.setPreferredSize(new Dimension(50, 20));
		controlStepTimeTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(controlStepTimeTextField);

		bottomPanel.add(new JLabel("CPU time/renderer frame: "));
		rendererTimeTextField = new JTextField("N/A");
		rendererTimeTextField.setPreferredSize(new Dimension(50, 20));
		rendererTimeTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(rendererTimeTextField);

		bottomPanel.setBorder(BorderFactory.createTitledBorder("Status"));

		return bottomPanel;
	}

	private void initActions() {
		
		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("control LEFT"), "none");
		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("control RIGHT"), "none");
		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("meta LEFT"), "none");;
		shiftTextField.getInputMap().put(KeyStroke.getKeyStroke("meta RIGHT"), "none");
		
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control ENTER"), "control ENTER");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta ENTER"), "control ENTER");
		((JComponent) frame.getContentPane()).getActionMap().put("control ENTER", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.loadCurrentFile();
			}
		});
		
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control P"), "control P");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta P"), "control P");
		((JComponent) frame.getContentPane()).getActionMap().put("control P", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.pauseButton();
			}
		});
		
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "control S");  
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta S"), "control S");
		((JComponent) frame.getContentPane()).getActionMap().put("control S", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.startButton();
			}
		});
		
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control LEFT"), "control LEFT");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta LEFT"), "control LEFT");
		((JComponent) frame.getContentPane()).getActionMap().put("control LEFT", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.shiftSimulationBy(-position_shift,false);
			}
		});
		
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control RIGHT"), "control RIGHT");
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("meta RIGHT"), "control RIGHT");
		((JComponent) frame.getContentPane()).getActionMap().put("control RIGHT", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.shiftSimulationBy(position_shift,false);
			}
		});
		
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "+");  
		((JComponent) frame.getContentPane()).getActionMap().put("+", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.renderer.zoomIn();  
			}
		});  

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "-");  
		((JComponent) frame.getContentPane()).getActionMap().put("-", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.renderer.zoomOut();  
			}
		});  

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('*'), "*");  
		((JComponent) frame.getContentPane()).getActionMap().put("*", new AbstractAction(){  
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent evt) {  
				ResultViewerGui.this.renderer.resetZoom();  
			}
		});  
	}

	private void initListeners() {

		sleepSlider.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {
				sleepBetweenControlSteps = sleepSlider.getValue();
			}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseClicked(MouseEvent arg0) {}
		});

		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startButton();
			} 		
		});

		quitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			} 		
		});

		pauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				pauseButton();
			} 		
		});

		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
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
					if(simulator != null && simulator.getExperiment() != null)
						simulator.getExperiment().endExperiment();
					try{
						simulationState = NONE;
						main.loadFile(currentFileTextField.getText(), extraArguments.getText());
						simulator = main.setupSimulator();
						new GraphPlotter(main);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		});
		
		shiftButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					int value = Integer.parseInt(shiftTextField.getText());
					position_shift = value;
				}catch(Exception e) {}
			}
		});
		
		shiftTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					int value = Integer.parseInt(shiftTextField.getText());
					position_shift = value;
				}catch(Exception e) {}				
			}
		});
		
		plotFitnessButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plotFitness();
			}
		});
		
		neuralNetworkCheckbox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				JCheckBox check = (JCheckBox)arg0.getSource();
				showNeuralNetwork = check.isSelected();
			}
		});
	}
	
	private void plotFitness() {
		File f = new File(currentFileTextField.getText().trim());
		final String mainFolder = f.isDirectory() ? f.getAbsolutePath() : f.getParent();

		Thread t = new Thread( new Runnable(){
			public void run(){
				new GraphPlotter(getFitnessFiles(mainFolder).split("###"));
			}
		});
		t.start();
	}
	
	private String getFitnessFiles(String folder) {
		
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

	public void dispose() {
		frame.setVisible(false);
	}
	
	private void startButton() {
		simulationState = RUN;
		if(simulator != null && 
				simulator.getExperiment() != null &&
				simulator.getExperiment().hasEnded())
			loadCurrentFile();
	}
	
	private void pauseButton() {
		if (simulationState == RUN)
			simulationState = PAUSED;
		else
			simulationState = RUN;
	}

	private void shiftSimulationBy(int value, boolean percentage) {
		
		try {
			simulator = main.setupSimulator();
		
			if(readyToSkip) {
				int maxSteps = simulator.getExperiment().getNumberOfStepsPerRun();
				
				if(!percentage) {
					value+=currentStep;
					value = value > maxSteps ? maxSteps : (value < 1 ? 1 : value);
					simulateUntil = value;
				}else {
					value = value > 100 ? 100 : (value < 1 ? 1 : value);
					double percent = value/100.0;
					int targetStep = (int)(percent*maxSteps);
					simulateUntil = targetStep;
				}
				launchSimulation();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public synchronized void run(Simulator simulator, Renderer rendererTo,
			Experiment experiment, EvaluationFunction evaluationFunction,
			int maxNumberOfSteps) {
		
		this.simulator = simulator;
		if(dispatcher != null)
			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
		dispatcher = new EnvironmentKeyDispatcher(simulator);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
		
		if(graphViz == null) {
			NeuralNetworkController nn = (NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getEvolvingController();
			graphViz = new GraphViz(nn.getNeuralNetwork());
		}
		if(showNeuralNetwork)
			graphViz.show();

		Component rendererComponent = null;
		readyToSkip = false;

		try{
			currentStep = 0;
			if(renderer != null){
				renderer.setSimulator(simulator);
				rendererComponent = renderer.getComponent();
				if (rendererComponent != null) {
					frame.add(rendererComponent,BorderLayout.CENTER);
					rendererComponent.addKeyListener(experiment.getEnvironment());
					rendererComponent.enableInputMethods(true);
					rendererComponent.setFocusable(true);
					frame.getContentPane().validate();
					frame.getContentPane().setFocusable(true);
				}
			}

			if(simulateUntil > 0) {
				while(currentStep < simulateUntil && !experiment.hasEnded()) {
					
					simulator.performOneSimulationStep(currentStep);
					currentStep++;
					if (evaluationFunction != null) {
						evaluationFunction.step();
					}
					Thread.sleep(0);
				}
				fitnessTextField.setText(String.format("%12.6f", evaluationFunction.getFitness()));
				updatePlaySlider(currentStep,maxNumberOfSteps);
			}
			
			readyToSkip = true;

			long lastFrameTime = 0;
			boolean pause = false;
			while (!experiment.hasEnded() && currentStep < maxNumberOfSteps) {						

				if (System.currentTimeMillis() - lastFrameTime > 1000.0 / maxFramesPerSecond) {
					long startTime = System.currentTimeMillis(); 
					renderer.drawFrame();
					long frameTime = System.currentTimeMillis() - startTime; 
					rendererTimeTextField.setText("" + frameTime + " ms");
					controlStepTextField.setText("" + currentStep);
					simulationTimeTextField.setText(String.format("%6.2fs", currentStep * simulator.getTimeDelta()));
					lastFrameTime = System.currentTimeMillis(); 
				}

				if (sleepBetweenControlSteps > 0)
					Thread.sleep(sleepBetweenControlSteps);

				long startTime = System.currentTimeMillis(); 
				if (simulationState == RUN) {
					pause = false;
					simulator.performOneSimulationStep(currentStep);
					long controlStepTime = System.currentTimeMillis() - startTime; 
					controlStepTimeTextField.setText("" + controlStepTime + " ms");

					if (evaluationFunction != null) {
						evaluationFunction.step();
						fitnessTextField.setText(String.format("%12.6f", evaluationFunction.getFitness()));
					}
					currentStep++;
					updatePlaySlider(currentStep,maxNumberOfSteps);
					if(showNeuralNetwork)
						graphViz.changeNeuralNetwork(((NeuralNetworkController)simulator.getEnvironment().getRobots().get(0).getEvolvingController()).getNeuralNetwork());
				}else if(simulationState == PAUSED){
					if(pause == false)
						simulator.pause();
					pause = true;
				}
			}

		}catch(InterruptedException e){
		}catch(Exception e) {
			e.printStackTrace();
		}finally{
			if (rendererComponent != null)
				rendererComponent.removeKeyListener(experiment.getEnvironment());
		}
	}

	private void updatePlaySlider(double step, double maxSteps) {
		int value = (int)(step/maxSteps*100);
		playPosition.setValue(value);
	}

	private void launchSimulation() throws Exception{

		boolean skip = false;
		
		if(workerThread != null) {
			workerThread.interrupt();
			workerThread.join();
		}
		
		if(!skip) {
			workerThread = new Worker();
			workerThread.start();
		}
	}

	private boolean validFile(String filename) {
		File f = new File(filename);
		return f.exists() && !f.isDirectory();
	}

	private void loadCurrentFile() {
		String filename = currentFileTextField.getText();

		try{
			if(validFile(filename)) {
				main.loadFile(filename,extraArguments.getText());

				playPosition.setValue(0);
				simulateUntil = 0;

				launchSimulation();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public class Worker extends Thread {
		@Override
		public void run() {
			try {
				main.execute();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public class FileTree extends JPanel {

		private static final long serialVersionUID = 1L;
		private String currentFilename = "";
		private JTree tree;
		private DefaultTreeModel model;

		public FileTree(File dir) {
			setLayout(new BorderLayout());
			
			DefaultMutableTreeNode nodes = addNodes(null, dir);

			model = new DefaultTreeModel(nodes);
			tree = new JTree(model);
			
			addTree();
		}
		
		private void addTree() {
			
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
		}

		public String getCurrentFilename() {
			return currentFilename;
		}

		private DefaultMutableTreeNode addNodes(DefaultMutableTreeNode curTop, File dir) {
			
			String curPath = dir.getAbsolutePath();
			DefaultMutableTreeNode curDir = new DefaultMutableTreeNode(dir.getAbsolutePath());
			
			if(curTop != null)
				curDir = curTop;
			
			Vector<String> ol = new Vector<String>();
			String[] tmp = dir.list();
			for (int i = 0; i < tmp.length; i++)
				ol.addElement(tmp[i]);
			Collections.sort(ol, String.CASE_INSENSITIVE_ORDER);
			
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
}
