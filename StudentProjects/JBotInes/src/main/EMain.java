package main;

import evolutionaryrobotics.JBotEvolver;
import gui.evolution.EvolutionGui;

import javax.swing.JFrame;

import simulation.util.Arguments;

public class EMain {

	public static void main(String[] args) {
//		String configName="experiments/forage_environment.conf";
		String configName="experiments/coevolutionpatrolling_tests151127/1/_restartevolution.conf";
//		String configName="experiments/joined2.conf";
//		String configName="experiments/chain_tests_4xSR3_2LR7/_restartevolution.conf";
		try {
			args = new String[]{configName};
			JBotEvolver jBotEvolver = new JBotEvolver(args);
			EvolutionGui evo = new EvolutionGui(jBotEvolver, new Arguments(""));
			JFrame frame = new JFrame();
			frame.add(evo);
			frame.setSize(1000, 500);
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			evo.init();
			evo.executeEvolution();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
