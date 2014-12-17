package gui;

import gui.renderer.Renderer;
import gui.renderer.TwoDRenderer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import simulation.JBotSim;
import simulation.Simulator;
import simulation.Updatable;
import simulation.util.Arguments;

public class WithControlsGui extends Gui implements Updatable{
	JFrame      frame;
	JTextField  simulationTimeTextField;
	JTextField  controlStepTextField;	
	JTextField  fitnessTextField;

	JTextField  controlStepTimeTextField;
	JTextField  rendererTimeTextField;	

	double      maxFramesPerSecond       = 25; 	
	int         sleepBetweenControlSteps = 10;

	JButton     startButton = new JButton("Start");
	JButton     quitButton  = new JButton("Quit");
	JButton     pauseButton = new JButton("Pause");

	JButton     decreaseMaxFramesPerSecond   = new JButton("<");
	JTextField  maxFramesPerSecondTextField;	
	JButton     increaseMaxFramesPerSecond   = new JButton(">");

	JButton     decreaseSleepBetweenControlSteps   = new JButton("<");
	JTextField  sleepBetweenControlStepsTextField;
	JButton     increasesleepBetweenControlSteps   = new JButton(">");

	static final int NONE        = 1;
	static final int RUN         = 2;
	static final int PAUSED      = 3;
	static final int STOPPED     = 4;	
	static final int ENDED       = 5;	

	int simulationState = NONE; 

	Renderer renderer;
	protected Simulator simulator;

	public WithControlsGui(JBotSim jBotSim, Arguments args) {
		super(jBotSim, args);

		if(args.getArgumentIsDefined("renderer"))
			createRenderer(new Arguments(args.getArgumentAsString("renderer")));
		
		frame = new JFrame("WithControlsGui");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1000, 700);
		
		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('+'), "+");  
		((JComponent) frame.getContentPane()).getActionMap().put("+", new AbstractAction(){  
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {  
				if(renderer != null)
					WithControlsGui.this.renderer.zoomIn();  
			}
		});  

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('-'), "-");  
		((JComponent) frame.getContentPane()).getActionMap().put("-", new AbstractAction(){  
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) { 
				if(renderer != null)
					WithControlsGui.this.renderer.zoomOut();  
			}
		});  

		((JComponent) frame.getContentPane()).getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('*'), "*");  
		((JComponent) frame.getContentPane()).getActionMap().put("*", new AbstractAction(){  
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt) {  
				if(renderer != null)
					WithControlsGui.this.renderer.resetZoom();  
			}
		});  

		
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

		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);		

		JPanel sideTopPanel = new JPanel();
		sideTopPanel.setLayout(new GridLayout(7,1));
		sideTopPanel.add(startButton);
		sideTopPanel.add(pauseButton);
		sideTopPanel.add(quitButton);

		startButton.addActionListener(new ActionListener() {
			//			@Override
			public void actionPerformed(ActionEvent arg0) {
				simulationState = RUN;
				if(simulator != null || simulator.simulationFinished()) {
					simulator = loadSimulator();
					new Thread(new SimulationRunner(simulator)).start();
				}					
			} 		
		});

		quitButton.addActionListener(new ActionListener() {
			//			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			} 		
		});

		pauseButton.addActionListener(new ActionListener() {
			//			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (simulationState == RUN) {
					simulationState = PAUSED;
				}else {
					simulationState = RUN;
				}
			} 		
		});

		sideTopPanel.add(new JLabel("Max frames per second"));
		JPanel framesPanel = new JPanel();
		framesPanel.add(decreaseMaxFramesPerSecond);
		maxFramesPerSecondTextField = new JTextField("" + maxFramesPerSecond);
		maxFramesPerSecondTextField.setPreferredSize(new Dimension(60, 20));
		maxFramesPerSecondTextField.setHorizontalAlignment(JTextField.CENTER);
		framesPanel.add(maxFramesPerSecondTextField);
		framesPanel.add(increaseMaxFramesPerSecond);
		decreaseMaxFramesPerSecond.addActionListener(new ActionListener() {
			//			@Override
			public void actionPerformed(ActionEvent arg0) {
				maxFramesPerSecond *= 0.9;
			}

		});

		increaseMaxFramesPerSecond.addActionListener(new ActionListener() {
			//			@Override
			public void actionPerformed(ActionEvent arg0) {
				maxFramesPerSecond /=  0.9;
			}
		});		
		sideTopPanel.add(framesPanel);

		sideTopPanel.add(new JLabel("Sleep between control steps"));
		JPanel sleepPanel = new JPanel();
		sleepPanel.add(decreaseSleepBetweenControlSteps);
		sleepBetweenControlStepsTextField = new JTextField("" + sleepBetweenControlSteps + " ms");
		sleepBetweenControlStepsTextField.setPreferredSize(new Dimension(60, 20));
		sleepBetweenControlStepsTextField.setHorizontalAlignment(JTextField.CENTER);
		sleepPanel.add(sleepBetweenControlStepsTextField);
		sleepPanel.add(increasesleepBetweenControlSteps);
		decreaseSleepBetweenControlSteps.addActionListener(new ActionListener() {
			//			@Override
			public void actionPerformed(ActionEvent arg0) {
				sleepBetweenControlSteps -= 5;
				if (sleepBetweenControlSteps < 0)
					sleepBetweenControlSteps = 0;
			}
		});

		increasesleepBetweenControlSteps.addActionListener(new ActionListener() {
			//			@Override
			public void actionPerformed(ActionEvent arg0) {
				sleepBetweenControlSteps += 5;
			}
		});		
		sideTopPanel.add(sleepPanel);

		JPanel sideWrapperPanel = new JPanel();
		sideWrapperPanel.setLayout(new BorderLayout());
		sideWrapperPanel.add(sideTopPanel, BorderLayout.NORTH);
		frame.getContentPane().add(sideWrapperPanel, BorderLayout.EAST);
		
		simulator = loadSimulator();
//		new Thread(new SimulationRunner(simulator)).start();
		frame.setVisible(true);
	}

	//	@Override
	public void dispose() {
		frame.setVisible(false);
	}
	
	private void createRenderer(Arguments args) {
		if(args.getArgumentIsDefined("classname"))
			this.renderer = Renderer.getRenderer(args);
	}
	
	private Simulator loadSimulator() {
		HashMap<String,Arguments> args = jBotSim.getArguments();
		
		if(renderer != null)
			frame.getContentPane().remove(renderer);
		
		if(args.get("--gui").getArgumentIsDefined("renderer"))
			createRenderer(new Arguments(args.get("--gui").getArgumentAsString("renderer")));
		
		Simulator simulator = jBotSim.createSimulator(new Random(jBotSim.getRandomSeed()));
		simulator.addRobots(jBotSim.createRobots(simulator));
		simulator.addCallback(this);
		simulator.setupEnvironment();
		
		if (renderer != null) {
			renderer.enableInputMethods(true);
			renderer.setSimulator(simulator);
			frame.getContentPane().add(renderer);
			frame.validate();
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new EnvironmentKeyDispatcher(simulator));
		
		return simulator;
	}
	
	@Override
	public void update(Simulator simulator) {
		try {
			while(simulationState == PAUSED)
				Thread.sleep(10);
		}catch(Exception e){}
		
		controlStepTextField.setText("" + simulator.getTime().intValue());
		if(renderer != null)
			renderer.drawFrame();
		
		if (sleepBetweenControlSteps > 0) {
			try {	
				Thread.sleep(sleepBetweenControlSteps);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	class SimulationRunner implements Runnable {
		private Simulator sim;
		public SimulationRunner(Simulator sim) {
			this.sim = sim;
		}
		@Override
		public void run() {
			sim.simulate();
		}
	}
}