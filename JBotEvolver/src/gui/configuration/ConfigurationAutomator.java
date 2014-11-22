package gui.configuration;

import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;
import gui.evolution.EvolutionGui;

public class ConfigurationAutomator {

	public ConfigurationAutomator() {
		new ConfigurationAutomatorGui(this);
	}
	
	public void startEvolution(String configFileName){
		new EvolutionGuiThread(configFileName).start();
	}
	
	public static void main(String[] args) {
		new ConfigurationAutomator();
	}
	
	private class EvolutionGuiThread extends Thread{
		
		private String configFileName;

		public EvolutionGuiThread(String configFileName) {
			this.configFileName = configFileName;
		}
		
		@Override
		public void run() {
			try {
				String[] args = new String[]{configFileName + ".conf"};
				JBotEvolver jBotEvolver = new JBotEvolver(args);
				EvolutionGui evo = new EvolutionGui(jBotEvolver);
				evo.executeEvolution();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
