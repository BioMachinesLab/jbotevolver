package gui;

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
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
	
	
	protected void updateStatus() {
		controlStepTextField.setText("" + simulator.getTime().intValue());
		initBottomPanel();
		fitnessTextFieldA.setText(String.format("%12.6f", evaluationFunctionA.getFitness()));
		fitnessTextFieldB.setText(String.format("%12.6f", evaluationFunctionB.getFitness()));
		updatePlaySlider(simulator.getTime(), simulator.getEnvironment().getSteps());
		renderer.drawFrame();
	}
	
	protected JPanel initBottomPanel() {

		JPanel bottomPanel   = new JPanel();

		bottomPanel.add(new JLabel("Control step: "));
		controlStepTextField = new JTextField("N/A");
		controlStepTextField.setPreferredSize(new Dimension(50, 20));
		controlStepTextField.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(controlStepTextField);

		bottomPanel.add(new JLabel("Fitness A: "));
		fitnessTextFieldA = new JTextField("N/A");
		fitnessTextFieldA.setPreferredSize(new Dimension(100, 20));
		fitnessTextFieldA.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(fitnessTextFieldA);
		
		fitnessTextField = new JTextField("N/A");
		
		bottomPanel.add(new JLabel("Fitness B: "));
		fitnessTextFieldB = new JTextField("N/A");
		fitnessTextFieldB.setPreferredSize(new Dimension(100, 20));
		fitnessTextFieldB.setHorizontalAlignment(JTextField.RIGHT);
		bottomPanel.add(fitnessTextFieldB);

		bottomPanel.setBorder(BorderFactory.createTitledBorder("Status"));

		return bottomPanel;
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
			fitnessTextField.setText("0");
		}
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new EnvironmentKeyDispatcher(simulator));
		
		return simulator;
	}

}
