package gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import evolutionaryrobotics.neuralnetworks.NeuralNetworkController;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;

public class NetworkViewer extends JFrame implements Updatable{
	
	private int lastStep = 0;
	private JPanel inputsPanel;
	private JPanel outputsPanel;
	
	public NetworkViewer() {
		super("Network Viewer");
	}
	
	private void setup(Robot robot) {
		if(robot != null && robot.getController() instanceof NeuralNetworkController) {
			NeuralNetworkController controller = (NeuralNetworkController)robot.getController();
			
			getContentPane().removeAll();
			
			int inputs = controller.getNeuralNetwork().getNumberOfInputNeurons();
			int outputs = controller.getNeuralNetwork().getNumberOfOutputNeurons();
			
			JPanel panel = new JPanel(new GridLayout(4, 1));
			
			inputsPanel = new JPanel();
			outputsPanel = new JPanel();
			
			for(int i = 0 ; i < inputs ; i++)
				inputsPanel.add(new JProgressBar(0,100));
			for(int i = 0 ; i < outputs ; i++)
				outputsPanel.add(new JProgressBar(0,100));
			
			panel.add(new JLabel("Inputs"));
			panel.add(inputsPanel);
			panel.add(new JLabel("Outputs"));
			panel.add(outputsPanel);
			
			add(panel);
			
			validate();
			repaint();
			pack();
			
		}
	}
	
	@Override
	public void update(Simulator simulator) {
		
		if(simulator.getTime() != lastStep+1)
			setup(simulator.getRobots().get(0));
		
		lastStep = simulator.getTime().intValue();
		
		NeuralNetworkController controller = (NeuralNetworkController)simulator.getRobots().get(0).getController();
		
		int inputs = controller.getNeuralNetwork().getNumberOfInputNeurons();
		int outputs = controller.getNeuralNetwork().getNumberOfOutputNeurons();
		
		double[] inputValues = controller.getNeuralNetwork().getInputNeuronStates();
		double[] outputValues = controller.getNeuralNetwork().getOutputNeuronStates();
		
		for(int i = 0 ; i < inputs ; i++)
			((JProgressBar)inputsPanel.getComponent(i)).setValue((int)(inputValues[i]*100));
		for(int i = 0 ; i < outputs ; i++) {
			((JProgressBar)outputsPanel.getComponent(i)).setValue((int)(outputValues[i]*100));
		}
	}
}