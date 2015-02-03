package gui;

import evolutionaryrobotics.JBotEvolver;
import gui.configuration.ConfigurationGui;
import gui.evolution.EvolutionGui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import simulation.util.Arguments;

public class CombinedGui extends JFrame {
	
	private JTabbedPane tabbedPane;
	private EvolutionGui evo;
	
	public CombinedGui(String[] args) {
		
		super("JBotEvolver");
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		
		tabbedPane = new JTabbedPane();
		
		JBotEvolver jbot = null;
		try {
			jbot = new JBotEvolver(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		evo = new EvolutionGui(jbot, new Arguments(""));
		ConfigurationGui configAutomatorGui = new ConfigurationGui(jbot, new Arguments(""));
		
		tabbedPane.addTab("Configuration", configAutomatorGui);
		tabbedPane.addTab("Evolution", evo);
		tabbedPane.addTab("Results", Gui.getGui(jbot, jbot.getArguments().get("--gui")));
		
		add(tabbedPane);

		setSize(1100,680);
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
	
	protected class WaitForEvolutionThread extends Thread {
		
		private ConfigurationGui configAutomatorGui;
		
		public WaitForEvolutionThread(ConfigurationGui configAutomatorGui) {
			this.configAutomatorGui = configAutomatorGui;
		}
		
		@Override
		public void run() {
			while(true) {
				configAutomatorGui.waitUntilEvolutionLaunched();
				new EvolutionGuiThread(configAutomatorGui.getConfigurationFileName()).start();
				tabbedPane.setSelectedIndex(1);
			}
		}
	}
	
	protected class EvolutionGuiThread extends Thread{
		
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