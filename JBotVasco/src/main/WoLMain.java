package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import evolutionaryrobotics.JBotEvolver;
import simulation.util.Arguments;
import taskexecutor.ConillonTaskExecutor;
import taskexecutor.TaskExecutor;
import taskexecutor.tasks.WoLTask;

public class WoLMain {
	private final static String HOSTS_MACS_FILE = "../../SecTools/WoLTools/hostnamesMACs.txt";
	private final static String CONFIG_FILE = "./experiments_automator/teste.conf";
	private final int REPEAT = 30;

	public static void main(String[] args) {
		new WoLMain();
	}

	public WoLMain() {
		// Get MAC addresses
		Map<String, String> macAddresses = getMacAddresses();

		// Initialize task executor
		JBotEvolver jBotEvolver;
		try {
			jBotEvolver = new JBotEvolver(new String[] { CONFIG_FILE });

			TaskExecutor taskExecutor = new ConillonTaskExecutor(jBotEvolver,
					new Arguments("server=evolve.dcti.iscte.pt"));
			// TaskExecutor taskExecutor = new
			// SequentialTaskExecutor(jBotEvolver, null);
			taskExecutor.start();
			taskExecutor.setTotalNumberOfTasks(macAddresses.keySet().size());

			for (int i = 0; i < REPEAT; i++) {
				ArrayList<String> hwAddresses = new ArrayList<String>();
				hwAddresses
						.addAll(Arrays.asList(macAddresses.values().toArray(new String[macAddresses.values().size()])));

				taskExecutor.addTask(new WoLTask(hwAddresses));
				System.out.print(".");
				taskExecutor.setDescription("Social sending " + macAddresses.size() * (i + 1) + " out of "
						+ (macAddresses.size() * REPEAT));
			}
			System.out.println();

			for (int i = 0; i < REPEAT; i++) {
				taskExecutor.getResult();
				System.out.println("!");
			}

			if (taskExecutor != null) {
				taskExecutor.stopTasks();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> getMacAddresses() {
		FileReader hostnamesFileReader = null;
		BufferedReader hostamesBReader = null;
		HashMap<String, String> macAddresses = new HashMap<String, String>();

		try {
			hostnamesFileReader = new FileReader(new File(HOSTS_MACS_FILE));
			hostamesBReader = new BufferedReader(hostnamesFileReader);

			String line = hostamesBReader.readLine();

			// Discard CSV titles line
			if (line != null) {
				line = hostamesBReader.readLine();
			}

			int count = 0;
			while (line != null) {
				if (!line.isEmpty() && !line.startsWith("#")) {
					String[] elements = line.split(",");

					if (elements.length == 2) {
						macAddresses.put(elements[0], elements[1]);
						count++;
					}
				}

				line = hostamesBReader.readLine();
			}

			System.out.printf("[%s] Loaded %d hostnames and hostnames%n", getClass().getSimpleName(), count);
		} catch (FileNotFoundException e) {
			System.err.printf("[%s] File not found! %s%n", getClass().getSimpleName(), e.getMessage());
		} catch (IOException e) {
			System.err.printf("[%s] Error reading from file! %s%n", getClass().getSimpleName(), e.getMessage());
		} finally {
			if (hostnamesFileReader != null) {
				try {
					hostnamesFileReader.close();
				} catch (IOException e) {
				}
			}

			if (hostamesBReader != null) {
				try {
					hostamesBReader.close();
				} catch (IOException e) {
				}
			}
		}

		return macAddresses;
	}
}
