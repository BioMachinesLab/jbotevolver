package main.automator;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JOptionPane;

import src.Main;

public class AMain {
	private static final boolean CONFIRM_OVERRIDE = false;
	private static final String path = "./experiments_automator/";
	private static final String[] configFiles = { "targetFollowing_automator.conf" };
	private HashMap<String, Evolver> evolverInstances = new HashMap<String, Evolver>();
	private long initTime;

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			ArrayList<String> files = new ArrayList<String>();
			for (String str : configFiles) {
				files.add(path + str);
			}

			new AMain(files.toArray(new String[files.size()]));
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
						files.add(f.getPath().replace("\\", "/"));
					}
				}

				new AMain(files.toArray(new String[files.size()]));
			} else {
				new AMain(args);
			}
		}
	}

	@SuppressWarnings("unused")
	public AMain(String[] args) {
		int evolverCount = 0;
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
			System.out.printf("[%s] Starting evolver #%d with configuration file: %s%n", getClass().getSimpleName(),
					evolverCount, config);
			evolver.start();
			evolverInstances.put(config, evolver);

			evolverCount++;
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

			System.out.printf("[%s] Finished evolution in %d days, %dh %dm %ds%n", getClass().getSimpleName(),
					(calendar.get(Calendar.DAY_OF_MONTH) - 1), (calendar.get(Calendar.HOUR) - 1),
					calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

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
				Main main = new AutomatorMain(args);
				main.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
