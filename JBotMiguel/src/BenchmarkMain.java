import javax.swing.JFrame;

import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import gui.evolution.EvolutionGui;

public class BenchmarkMain {
	public static void main(String[] args) throws Exception {
		
		String configName[] = {/*"benchmark/hexamap.conf","benchmark/hexamap-single.conf",*/"benchmark/hexamap-double.conf"};
		
		for(String s : configName) {
			
			long time = System.currentTimeMillis();
		
			try {
				args = new String[]{s};
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
			
			System.out.println(s+" "+(System.currentTimeMillis()-time));
		}
	}
}