package gui;

import gui.renderer.Renderer;

import java.awt.BorderLayout;
import java.awt.Component;
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
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class WithControlsGui extends Gui {
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

	public WithControlsGui(Arguments args) {
		super(args);
		this.renderer = Renderer.getRenderer(new Arguments(args.getArgumentAsString("renderer")));
		
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
				if (simulationState == RUN)
					simulationState = PAUSED;
				else
					simulationState = RUN;
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

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new EnvironmentKeyDispatcher(simulator));

		frame.setVisible(true);
	}

	//	@Override
	public void dispose() {
		frame.setVisible(false);
	}
	
	public void loadArguments(JBotSim jBotSim) {
		HashMap<String,Arguments> args = jBotSim.getArguments();
		this.renderer = Renderer.getRenderer(new Arguments(args.get("--gui").getArgumentAsString("renderer")));
		frame.getContentPane().add(renderer);
		renderer.enableInputMethods(true);
		frame.getContentPane().validate();
		long seed = args.get("--random-seed") != null ? Long.parseLong(args.get("--random-seed").getCompleteArgumentString()) : 0;
		Simulator simulator = jBotSim.createSimulator(new Random(seed));
		Environment env = jBotSim.getEnvironment(simulator);
		env.addRobots(jBotSim.createRobots(simulator));
		if (renderer != null) {
			frame.getContentPane().add(renderer);
			renderer.enableInputMethods(true);
			frame.getContentPane().validate();
		}
	}
	
	@Override
	public void update(Simulator simulator) {
		if(renderer != null) {
			renderer.update(simulator);
		}
		controlStepTextField.setText("" + simulator.getTime().intValue());
	}

	//	@Override
	public void run(Simulator simulator, Renderer rendererTo, int maxNumberOfSteps) {

		long lastFrameTime = 0;
		while (currentStep < maxNumberOfSteps) {						

			if (System.currentTimeMillis() - lastFrameTime > 1000.0 / maxFramesPerSecond) {
				long startTime = System.currentTimeMillis(); 
				renderer.drawFrame();
				long frameTime = System.currentTimeMillis() - startTime; 
				rendererTimeTextField.setText("" + frameTime + " ms");
				controlStepTextField.setText("" + currentStep);
				simulationTimeTextField.setText(String.format("%6.2fs", currentStep * simulator.getTimeDelta()));
				lastFrameTime = System.currentTimeMillis(); 
			}

			sleepBetweenControlStepsTextField.setText("" + sleepBetweenControlSteps + " ms");
			maxFramesPerSecondTextField.setText(String.format("%6.3f", maxFramesPerSecond));

			if (sleepBetweenControlSteps > 0) {
				try {	
					Thread.sleep(sleepBetweenControlSteps);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			long startTime = System.currentTimeMillis(); 
			if (simulationState == RUN) {
				simulator.performOneSimulationStep(new Double(0));
				long controlStepTime = System.currentTimeMillis() - startTime; 
				controlStepTimeTextField.setText("" + controlStepTime + " ms");


				currentStep++;
			}
		}
//		if (rendererComponent != null)
//			rendererComponent.removeKeyListener(experiment.getEnvironment());
	}
}
