package gui.configuration;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;
import gui.evolution.EvolutionGui;

public class ConfigurationAutomator {
	
	private EvolutionGui evo;

	public ConfigurationAutomator(EvolutionGui evolution) {
		new ConfigurationAutomatorGui(this);
		this.evo = evolution;
	}
	
	public void startEvolution(String configFileName){
		new EvolutionGuiThread(configFileName).start();
	}
	
	private class EvolutionGuiThread extends Thread{
		
		private String configFileName;

		public EvolutionGuiThread(String configFileName) {
			this.configFileName = configFileName;
		}
		
		@Override
		public void run() {
			try {
				
				evo.stopEvolution();
				
				String[] args = new String[]{configFileName + ".conf"};
				JBotEvolver jBotEvolver = new JBotEvolver(args);
				evo.init(jBotEvolver);
				evo.executeEvolution();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
