import javax.swing.JFrame;

import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import gui.evolution.EvolutionGui;

public class EMain {
	public static void main(String[] args) throws Exception {
		
//		String configName = "hexamap.conf";
//		String configName = "hexa_neat_big.conf";
//		String configName = "hexa_neat.conf";
//		String configName = "hexamap_big.conf";
		String configName = "repertoire_obstacle.conf";
//		String configName = "hexamap_debug.conf";
//		String configName = "hexamap_big/_restartevolution.conf";
//		String configName = "hexamap/_restartevolution.conf";
//		String configName = "repertoire_nav.conf";
//		String configName = "nsga2_novelty.conf";
//		String configName = "nsga2_fitness.conf";
//		String configName = "nsga2_nf.conf";
		
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
