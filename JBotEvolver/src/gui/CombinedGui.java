package gui;

import evolutionaryrobotics.JBotEvolver;
import gui.configuration.ConfigurationAutomator;
import gui.configuration.ConfigurationAutomatorGui;
import gui.evolution.EvolutionGui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class CombinedGui extends JFrame {
	
	private JTabbedPane tabbedPane;
	
	public CombinedGui(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		tabbedPane = new JTabbedPane();
		
		EvolutionGui evolution = new EvolutionGui();
		
		ConfigurationAutomator configAutomator = new ConfigurationAutomator(evolution);
		ConfigurationAutomatorGui configAutomatorGui = new ConfigurationAutomatorGui(configAutomator);
		
		JBotEvolver jbot = null;
		try {
			jbot = new JBotEvolver(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		tabbedPane.addTab("Configuration", configAutomatorGui);
		
		tabbedPane.addTab("Evolution", evolution);
		tabbedPane.addTab("Results", jbot.getGui().getGuiPanel());
		
		add(tabbedPane);

		setSize(1100,900);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		
		new WaitForEvolutionThread(configAutomatorGui).start();
	}
	
	public CombinedGui() {
		this(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer))"});
	}
	
	public static void main(String[] args) {
		new CombinedGui();
	}
	
	class WaitForEvolutionThread extends Thread {
		
		private ConfigurationAutomatorGui configAutomatorGui;
		
		public WaitForEvolutionThread(ConfigurationAutomatorGui configAutomatorGui) {
			this.configAutomatorGui = configAutomatorGui;
		}
		
		@Override
		public void run() {
			while(true) {
				configAutomatorGui.waitUntilEvolutionLaunched();
				tabbedPane.setSelectedIndex(1);
			}
		}
	}

}
