package main;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import evolutionaryrobotics.JBotEvolver;
import gui.evolution.EvolutionGui;
import simulation.util.Arguments;

public class EMain {
	private static final boolean START_GUI = false;
	private static final boolean CONFIRM_OVERRIDE = false;
	private static final String path = "./experiments/";
	private static final String[] configFiles = { "configFileAlternative.conf" };

	private HashMap<String, Evolver> evolverInstances = new HashMap<String, Evolver>();
	private long initTime;

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			ArrayList<String> files = new ArrayList<String>();
			for (String str : configFiles) {
				files.add(path + str);
			}

			new EMain(files.toArray(new String[files.size()]));
		} else {
			if (args[0].equals("all")) {
				String p = path;
				if (args.length > 1) {
					p = args[1];
				}

				ArrayList<String> files = new ArrayList<String>();
				File file = new File(p);

				for (File f : file.listFiles()) {
					if (!f.isDirectory() && f.getPath().endsWith(".conf")) {
						files.add(f.getPath());
					}
				}

				new EMain(files.toArray(new String[files.size()]));
			} else {
				new EMain(args);
			}
		}
	}

	@SuppressWarnings("unused")
	public EMain(String[] args) {
		for (String config : args) {
			File file = new File(config.substring(0, config.indexOf(".conf")));
			if (file.exists() && file.isDirectory() && CONFIRM_OVERRIDE) {
				int response = JOptionPane.showConfirmDialog(null, "Folder " + file.getName() + " exists. Override?",
						"Override experiments?", JOptionPane.YES_NO_OPTION);

				if (response == JOptionPane.NO_OPTION || response == JOptionPane.CLOSED_OPTION) {
					break;
				}
			}

			Evolver evolver = new Evolver(new String[] { config });
			System.out.println("Starting: " + config);
			evolver.start();
			evolverInstances.put(config, evolver);
		}

		if (!evolverInstances.isEmpty()) {
			initTime = System.currentTimeMillis();
			for (Evolver evolver : evolverInstances.values()) {
				try {
					evolver.join();
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis() - initTime);

			System.out.print("######## Finished evolution in " + (calendar.get(Calendar.DAY_OF_MONTH) - 1) + " days");
			System.out.print(", " + (calendar.get(Calendar.HOUR) - 1) + "h " + calendar.get(Calendar.MINUTE) + "m "
					+ calendar.get(Calendar.SECOND) + "s\n");

			System.exit(0);
		}
	}

	protected class Evolver extends Thread {
		private String[] args;

		public Evolver(String[] args) {
			this.args = args;
		}

		@Override
		public void run() {
			execute();
		}

		public void execute() {
			try {
				JBotEvolver jBotEvolver = new JBotEvolver(args);
				EvolutionGui evo = new EvolutionGui(jBotEvolver, new Arguments(""));

				if (START_GUI) {
					JFrame frame = new JFrame();
					frame.add(evo);
					frame.setSize(1000, 600);
					frame.setVisible(true);
					frame.setLocationRelativeTo(null);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				}

				evo.init();
				evo.executeEvolution();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
