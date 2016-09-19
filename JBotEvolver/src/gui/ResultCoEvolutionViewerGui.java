package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.util.Arguments;

public class ResultCoEvolutionViewerGui extends ResultViewerGui {
	
	private EvaluationFunction evaluationFunctionA;
	private EvaluationFunction evaluationFunctionB;
	
	private JTextField fitnessTextFieldA;
	private JTextField fitnessTextFieldB;

	public ResultCoEvolutionViewerGui(JBotSim jBot, Arguments args) {
		super(jBot, args);
		plotButton.removeActionListener(plotButton.getActionListeners()[0]);
		plotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				plot();
			}
		});
	}
	
	private void plot() {
		if(validFile(currentFileTextField.getText())) {
			if(simulator != null)
				simulator.stopSimulation();
			try{
				simulationState = PAUSED;
				jBotEvolver.loadFile(currentFileTextField.getText(), extraArguments.getText());
				JOptionPane.showMessageDialog(this, "This feature is not implemented for CoEvolution! Please implement it :)");
//				new GraphPlotterCoEvolution(jBotEvolver,loadSimulator(),"--robots");
//				new GraphPlotterCoEvolution(jBotEvolver,loadSimulator(),"--robots2");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void updateStatus() {
		controlStepTextField.setText("" + simulator.getTime().intValue());
		fitnessTextFieldA.setText(String.format("%12.6f", evaluationFunctionA.getFitness()));
		fitnessTextFieldB.setText(String.format("%12.6f", evaluationFunctionB.getFitness()));
		updatePlaySlider(simulator.getTime(), simulator.getEnvironment().getSteps());
		renderer.drawFrame();
	}
	
	@Override
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

		statusPanel.add(new JLabel("Fitness A: "));
		
		fitnessTextFieldA = new JTextField("N/A");
		fitnessTextFieldA.setPreferredSize(new Dimension(100, 20));
		fitnessTextFieldA.setHorizontalAlignment(JTextField.RIGHT);
		statusPanel.add(fitnessTextFieldA);
		
		statusPanel.add(new JLabel("Fitness B: "));
		
		fitnessTextFieldB = new JTextField("N/A");
		fitnessTextFieldB.setPreferredSize(new Dimension(100, 20));
		fitnessTextFieldB.setHorizontalAlignment(JTextField.RIGHT);
		statusPanel.add(fitnessTextFieldB);
		
		sideTopPanel.add(statusPanel);
		statusPanel.setPreferredSize(new Dimension(panelWidth,100));
		
		JPanel sideWrapperPanel = new JPanel(new BorderLayout());
		sideWrapperPanel.add(sideTopPanel,BorderLayout.NORTH);
		sideWrapperPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
		
		pauseButton.setPreferredSize(new Dimension(panelWidth,50));
		plotButton.setPreferredSize(new Dimension(panelWidth,50));

		return sideWrapperPanel;
	}
	
	@Override
	public Simulator loadSimulator() {
		if(simulator != null)
			simulator.stopSimulation();
		
		HashMap<String,Arguments> args = jBotEvolver.getArguments();
		
		if(renderer != null)
			this.remove(renderer);
		
		if(args.get("--gui") != null && args.get("--gui").getArgumentIsDefined("renderer"))
			createRenderer(new Arguments(args.get("--gui").getArgumentAsString("renderer")));
		
		//Cria o Simulador
		Simulator simulator = jBotEvolver.createSimulator();
		
		// Obtem a evolução para a populaçãoA e adiciona ao callBack
		evaluationFunctionA = jBotEvolver.getSpecificEvaluationFunction("a");
		simulator.addCallback(evaluationFunctionA);
		
		
//		 Obtem a evolução para a populaçãoB e adiciona ao callBack
		evaluationFunctionB = jBotEvolver.getSpecificEvaluationFunction("b");
		simulator.addCallback(evaluationFunctionB);
		
		//Cria os melhores de A e B
		jBotEvolver.setupBestCoIndividual(simulator);
		
		simulator.addCallback(this);
		simulator.setupEnvironment();
		
//		for(Updatable up : simulator.getCallbacks()) {
//			if(up instanceof EvaluationFunction) {
//				this.evaluationFunction = (EvaluationFunction)up;
//				break;
//			}
//		}
		
		if (renderer != null) {
			renderer.enableInputMethods(true);
			renderer.setSimulator(simulator);
			this.add(renderer);
			if(simulateUntil == 0)
				renderer.drawFrame();
			this.validate();
		}
		
		if(simulateUntil == 0) {
			controlStepTextField.setText("0");
			fitnessTextFieldA.setText("0");
			fitnessTextFieldB.setText("0");
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new EnvironmentKeyDispatcher(simulator));
		
		return simulator;
	}

}
