import java.awt.BorderLayout;

import javax.swing.JFrame;

import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import gui.evolution.EvolutionGui;

public class EMain {
	public static void main(String[] args) throws Exception {
		
		String configName = "neat_parameter_test.conf";
		
		try {
			args = new String[]{configName};
			JBotEvolver jBotEvolver = new JBotEvolver(args);
			EvolutionGui evo = new EvolutionGui(jBotEvolver,new Arguments(""));
			JFrame frame = new JFrame();
			frame.add(evo);
			frame.setSize(1000, 600);
			frame.setVisible(true);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			evo.init();
			evo.executeEvolution();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
