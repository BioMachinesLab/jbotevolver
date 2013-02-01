package evolutionaryrobotics.parallel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import simulation.util.Arguments;
import evolutionaryrobotics.parallel.task.ChromosomeTask;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.Util;

public class MultiMaster {
	public static final int LOW_PRIORITY = 10;
	public static final int HIGH_PRIORITY = 0;
	public static final int STARTING_GUI = 1;
	public static final int STARTING_INFO = 0;
	public static final int TIMEOUT = 1200000;

	public static int DEFAULTCLIENTMASTERPORT = 10001;
	public static int DEFAULTMASTERSLAVEPORT = 10000;
	public static int DEFAULTMASTERINFOPORT = 9999;
	public static int DEFAULTGUIPORT = 9998;

	public int masterClientPort = DEFAULTCLIENTMASTERPORT;
	public int masterSlavePort = DEFAULTMASTERSLAVEPORT;
	public int masterInfoPort = DEFAULTMASTERINFOPORT;
	public int masterGuiPort = DEFAULTGUIPORT;


	private Hashtable<String, SlaveData> slaveDataVector = new Hashtable<String, SlaveData>();
	private LinkedList<ClientDescription> clients = new LinkedList<ClientDescription>();

	private int numberOfRunningSlaves = 0;
	private int numberOfRunningClients = 0;

	private LinkedList<ChromosomeTask> pendingChromosome = new LinkedList<ChromosomeTask>();
	private int numberOfChromosomesLeft = 0;
	private boolean evolutionDone;
	// private int numberOfChromosomesEvaluated = 0;
	private int totalNumberOfEvaluatiosDone = 0;
	public long startTime;
	public int totalNumberOfEvaluationsNecessary;

	// private Comparator<ClientDescription> sorter = new
	// ClientDescription.Sorter();
	private int totalNumberSlaves = 0;

	public MultiMaster() {
		startTime = System.currentTimeMillis();
	}

	private synchronized ChromosomeTask getChromosome(SlaveData slaveData) {
		ChromosomeTask task = null;

		// System.out.println(getIdAndTime(slaveData) + " -> there are "
		// + numberOfChromosomesLeft + " chromosomes to calculate");
		try {
			// if (slaveData.numberOfChromosomesProcessed > 1) {
			// // while (getAverageLastChromosomeTimeOfRunningSlaves() * (1
			// + numberOfChromosomesLeft / numberOfRunningSlaves) <
			// slaveData.lastChromosomeTime||
			// // (numberOfChromosomesLeft <= numberOfRunningSlaves &&
			// // getAverageLastChromosomeTimeOfRunningSlaves() <
			// slaveData.lastChromosomeTime / 1.5)) {
			// //// System.out.println("avg: " +
			// getAverageLastChromosomeTimeOfRunningSlaves() + "slave: " +
			// slaveData.lastChromosomeTime);
			// // slaveData.slaveStatus = "Waiting because slow...";
			// // notifyAll();
			// // wait();
			// // }
			// }

			while (numberOfChromosomesLeft == 0 && !evolutionDone) {
				System.out.println(getIdAndTime(slaveData)
						+ " :nothing to evaluate!  " + numberOfChromosomesLeft);
				wait();
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println(getIdAndTime(slaveData)
					+ " ->Was interrupted while waiting!");
			return null;
		}

		if (evolutionDone) {
			System.out.println(getIdAndTime(slaveData) + " -> Evolution done!");
			return null;
		} else {
			// System.out.println(getIdAndTime(slaveData) + " -> there are "
			// + numberOfChromosomesLeft +
			// " chromosomes to calculate + pending = " + pendingChromosome);

			if (pendingChromosome.size() > 0) {
				// System.out.println(getIdAndTime(slaveData) +
				// " ->  pending = " + pendingChromosome);

				task = pendingChromosome.pollFirst();
				if (task == null) {
					System.out
					.println("XXX: get null chromosome from pending chromosome list!");
				}

			} else {
				// System.out.println(getIdAndTime(slaveData) +
				// " ->  sorting ");

				Collections.sort(clients);
				// System.out.print(getIdAndTime(slaveData) + " ->  WORK: ");
				//				
				// for(ClientDescription client : clients){
				// System.out.print(client + ", ");
				// }
				// System.out.print("");

				task = clients.getLast().getNextTask();

				// System.out.println(getIdAndTime(slaveData) + " -> task = " +
				// task);

				// while (task == null) {
				// try {
				// wait();
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// Collections.sort(clients);
				// task = clients.getLast().getNextTask();
				// }
			}
			numberOfChromosomesLeft--;
			// System.out
			// .println(getIdAndTime(slaveData) + " :got chromosome. "
			// + numberOfChromosomesLeft
			// + " chromosomes left to calculate");

			return task;
		}
	}

	private synchronized void addChromosome(ChromosomeTask task) {
		pendingChromosome.add(task);
		numberOfChromosomesLeft++;
		notifyAll();
	}

	private synchronized void addChromosomesFromNewPopulation(int populationSize) {
		numberOfChromosomesLeft += populationSize;
		notifyAll();
	}

	private synchronized void setResult(ChromosomeTask task, double fitness) {
		task.setEvaluationResult(fitness, totalNumberOfEvaluatiosDone++);
	}

	public synchronized void addClient(ClientDescription clientInfo) {
		clients.add(clientInfo);
	}

	public synchronized void removeClient(ClientDescription clientInfo) {
		clients.remove(clientInfo);
		numberOfRunningClients--;

		// TODO: remove the number of genomes to calculate
	}

	public void execute() {

		RemoteInfoThread remoteInfoThread = new RemoteInfoThread();
		remoteInfoThread.start();

		ReceiveSlaveConnectionsThread receiveSlaveConnectionsThread = new ReceiveSlaveConnectionsThread();
		receiveSlaveConnectionsThread.start();

		ReceiveClientConnectionsThread receiveClientConnectionsThread = new ReceiveClientConnectionsThread();
		receiveClientConnectionsThread.start();

		RemoteGuiThread remoteIGuiThread = new RemoteGuiThread();
		remoteIGuiThread.start();

	}

	private String getIdAndTime(SlaveData slaveData) {
		return "(" + slaveData.id + ") ["
		+ (System.currentTimeMillis() - startTime) + "]";
	}

	public static void main(String[] args) {
		new MultiMaster().execute();
	}

	class ClientThread extends Thread {
		private static final int NUMBER_ARGUMENTS = 5;
		Socket socket;
		ObjectOutputStream out;
		ObjectInputStream in;
		private Integer numberChromossomesToEvaluate;

		public ClientThread(Socket socket) throws IOException {
			this.socket = socket;
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			System.out.println("New client " + socket);

		}

		public void run() {
			ClientDescription clientInfo = null;
			try {
				Integer slaveVersion = (Integer) in.readObject();
				if (slaveVersion.intValue() != Master.MASTERVERSION.intValue()) {
					out.writeObject(new Boolean(false));
					return;
				} else {
					out.writeObject(new Boolean(true));
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			Arguments[] arguments = new Arguments[NUMBER_ARGUMENTS];

			Integer priority;
			try {
				numberChromossomesToEvaluate = (Integer) in.readObject();

				synchronized (MultiMaster.this) {
					totalNumberOfEvaluationsNecessary += numberChromossomesToEvaluate;
				}
				priority = (Integer) in.readObject();
				for (int i = 0; i < arguments.length; i++) {
					arguments[i] = (Arguments) in.readObject();
				}
				clientInfo = new ClientDescription(arguments, priority);
				addClient(clientInfo);

				// Enable keep alive:
				if (socket.getKeepAlive())
					socket.setKeepAlive(true);

				// Set a of 1 minute:
				// socket.setSoTimeout(60000);

				synchronized (MultiMaster.this) {
					numberOfRunningClients++;
				}
				boolean keepRunning = true;
				while (keepRunning) {
					Population population = (Population) in.readObject();
					//long seed = (Long)in.readObject();
					if (population == null) {
						keepRunning = false;
					} else {
						System.out.println("Received new generation "
								+ population.getNumberOfCurrentGeneration()
								+ "(" + numberChromossomesToEvaluate
								+ ") from " + socket);

						clientInfo.setPopulation(population);
						addChromosomesFromNewPopulation(population
								.getPopulationSize()
								- population.getNumberOfChromosomesEvaluated());

						numberChromossomesToEvaluate -= population
						.getPopulationSize()
						- population.getNumberOfChromosomesEvaluated();

						clientInfo.waitResults();
						out.reset();
						out.writeObject(clientInfo.getPopulation());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				removeClient(clientInfo);
				synchronized (MultiMaster.this) {
					totalNumberOfEvaluationsNecessary -= numberChromossomesToEvaluate;
				}

			}

		}

	}

	class SlaveThread extends Thread {
		Socket socket;
		ObjectOutputStream out;
		ObjectInputStream in;
		SlaveData slaveData;
		private long thisChromosomeStartTime;

		public SlaveThread(Socket socket) throws IOException {
			this.socket = socket;
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			System.out.println("New slave " + socket);
		}

		public void run() {
			try {
				Integer slaveVersion = (Integer) in.readObject();
				if (slaveVersion.intValue() != Master.MASTERVERSION.intValue()) {
					out.writeObject(new Boolean(false));
					return;
				} else {
					out.writeObject(new Boolean(true));
				}

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			String address = socket.getInetAddress().getHostName().toString()
			+ "-" + socket.getPort();
			slaveData = slaveDataVector.get(address);

			if (slaveData == null) {
				slaveData = new SlaveData();
				slaveData.id = totalNumberSlaves++;
				slaveDataVector.put(address, slaveData);
				slaveData.startTime = new Date().toString();
				slaveData.slaveAddress = address;
				slaveData.slavePort = socket.getPort();
			}

			slaveData.connectionCount++;

			long startTime = System.currentTimeMillis();

			try {
				// Enable keep alive:
				if (socket.getKeepAlive())
					socket.setKeepAlive(true);
			} catch (SocketException se) {
				se.printStackTrace();
			}

			try {
				// Set a timeout of 1 minute:
				socket.setSoTimeout(TIMEOUT);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

			synchronized (MultiMaster.this) {
				numberOfRunningSlaves++;
			}

			boolean keepRunning = true;
			ChromosomeTask task = null;
			try {
				while (keepRunning) {
					slaveData.slaveStatus = "Waiting...";
					// System.out.println(getIdAndTime(slaveData)
					// + " waiting for chromossome");
					task = getChromosome(slaveData);
					// System.out.println(getIdAndTime(slaveData)
					// + " got chromossome "
					// + task.getChromosome());

					slaveData.running = true;
					if (task == null) {
						slaveData.slaveStatus = "Done.";
						out.writeObject(new Boolean(false));
						out.flush();
						keepRunning = false;
						System.out.println(getIdAndTime(slaveData)
								+ " Done, closing connection to " + socket);

					} else {

						slaveData.slaveStatus = "Computing...";
						// System.out.println(getIdAndTime(slaveData)
						// + " sending chromosome");

						thisChromosomeStartTime = System.currentTimeMillis();

						out.reset();

						out.writeObject(new Boolean(true));

						Arguments[] args = task.getArgumets();
						for (int i = 0; i < args.length; i++)
							out.writeObject(args[i]);

						out.writeObject(task.getRandom());
						out.writeObject(task.getChromosome());
						out.writeObject(new Integer(task
								.getNumberOfSamplesPerChromosome()));
						out.writeObject(new Integer(task
								.getNumberOfStepsPerSample()));

						// System.out.println(getIdAndTime(slaveData)
						// + " waiting for fitness");
						double fitness = in.readDouble();
						// System.out
						// .println(getIdAndTime(slaveData)
						// + " received fitness: "
						// + fitness);

						setResult(task, fitness);
						slaveData.numberOfChromosomesProcessed++;
						slaveData.averageTimePerChromosome = (double) ((System
								.currentTimeMillis() - startTime) + 1)
								/ (1000.0 * slaveData.numberOfChromosomesProcessed);
						long thisTime = System.currentTimeMillis()
						- thisChromosomeStartTime;
						slaveData.lastChromosomeTime = thisTime;
						// try {
						// socket.setSoTimeout((int) (thisTime * 5));
						// } catch (SocketException e1) {
						// e1.printStackTrace();
						// }

					}
				}
				slaveData.slaveStatus = "Ended normally.";
			} catch (Exception e) {
				slaveData.slaveStatus = "Ended due to exception.";
				System.out.println(getIdAndTime(slaveData)
						+ " : ended due to exception:"  + socket.getRemoteSocketAddress().toString() 
						+ " error msg: " + e.getClass().getName());
				//e.printStackTrace(System.out);
				addChromosome(task);
				try {
					socket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			slaveData.endTime = new Date().toString();
			slaveData.running = false;

			synchronized (MultiMaster.this) {
				numberOfRunningSlaves--;
			}

		}

	}

	class ReceiveSlaveConnectionsThread extends Thread {
		public ReceiveSlaveConnectionsThread() {

		}

		public void run() {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(masterSlavePort);
				System.out.println("Waiting for slave connections on socket: "
						+ serverSocket);
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						new SlaveThread(socket).start();
					} catch (IOException e) {

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (serverSocket != null)
						serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ReceiveClientConnectionsThread extends Thread {
		public ReceiveClientConnectionsThread() {

		}

		public void run() {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(masterClientPort);
				System.out.println("Waiting for client connections on socket: "
						+ serverSocket);
				while (true) {
					try {
						Socket socket = serverSocket.accept();
						new ClientThread(socket).start();
					} catch (IOException e) {

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (serverSocket != null)
						serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class RemoteInfoThread extends Thread {
		public RemoteInfoThread() {
		}

		public void run() {
			ServerSocket serverSocket = null;
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(masterInfoPort);
				System.out.println("RemoteInfo opened server socket on port: "
						+ masterInfoPort);
				while (true) {
					socket = serverSocket.accept();
					String address = socket.getInetAddress().toString();
					address = address.substring(1, address.length());
					System.out.println("Connection accepted from: " + address);
					new serveRemoteInfoThread(socket).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {

				serverSocket.close();
			} catch (Exception e) {

			}
		}


		class serveRemoteInfoThread extends Thread {
			Socket socket;

			public serveRemoteInfoThread(Socket socket) {
				this.socket = socket;
			}

			public void run() {
				PrintWriter out = null;
				BufferedReader in = null;
				try {
					out = new PrintWriter(socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(socket
							.getInputStream()));

					out.println("Magic");
					String magic = in.readLine();
					if (magic == null) {
						socket.close();
						return;
					}

					if (!magic.equals("robot")) {
						socket.close();
						return;
					}
					long totalNumberOfEvaluatiosDoneStep = totalNumberOfEvaluatiosDone;

					while (!evolutionDone) {
						long time = (System.currentTimeMillis() - startTime) / 1000;
						out.println(new Date().toString());
						out
						.println("\tNumber of Chromosomes to be evaluated: "
								+ (totalNumberOfEvaluationsNecessary - totalNumberOfEvaluatiosDone));
						out.println("\tNumber of Chromosomes evaluated: "
								+ totalNumberOfEvaluatiosDone);
						out
						.println("\tCurrent calculating power: "
								+ (totalNumberOfEvaluatiosDone - totalNumberOfEvaluatiosDoneStep)
								/ 10.0 + " Chromosomes/sec");
						// out.println("\tOutput dir:       " +
						// diskStorage.getOutputDirectory());
						// out.println("\tGeneration:       " +
						// population.getNumberOfChromosomesEvaluated() + " / "
						// + population.getPopulationSize() + " [" +
						// population.getNumberOfCurrentGeneration() + " / " +
						// population.getNumberOfGenerations() + "]");
						out.println("\tNumber of clients: "
								+ numberOfRunningClients);
						out.println("\tNumber of slaves: "
								+ numberOfRunningSlaves);
						out.println("\tTime spent:       "
								+ Util.formatIntoHHMMSS(time));
						out.print("\tTime left:        ");

						if (totalNumberOfEvaluatiosDone > 0) {
							out
							.println(Util
									.formatIntoHHMMSS((System
											.currentTimeMillis() - startTime)
											/ totalNumberOfEvaluatiosDone
											* (totalNumberOfEvaluationsNecessary - totalNumberOfEvaluatiosDone)
											/ 1000));
						} else {
							out.println("N/A");
						}

						// Enumeration<SlaveData> iterator = slaveDataVector
						// .elements();
						// while (iterator.hasMoreElements()) {
						// SlaveData slaveData = iterator.nextElement();
						// out
						// .print(slaveData.slaveAddress
						// + " ("
						// + slaveData.numberOfChromosomesProcessed
						// + ", "
						// + (slaveData.lastChromosomeTime / 1000.0)
						// + ", " + slaveData.connectionCount
						// + ")   ");
						// }
						// out.println();

						// out.printf("\tFitness:          max: %10.6f, avg: %10.6f, min: %10.6f\n",
						// maxFitness, averageFitness, minFitness);
						totalNumberOfEvaluatiosDoneStep = totalNumberOfEvaluatiosDone;
						sleep(10000);
						if (in.ready()) {
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if (evolutionDone) {
					System.out.println("Evolution done!");
				}

				System.out.println("Closing connection");

				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public class RemoteGuiThread extends Thread {

		public RemoteGuiThread() {
		}

		public void run() {
			ServerSocket serverSocket = null;
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(masterGuiPort);
				System.out.println("RemoteInfo opened server socket on port: "
						+ masterGuiPort);
				while (true) {
					socket = serverSocket.accept();
					String address = socket.getInetAddress().toString();
					address = address.substring(1, address.length());
					System.out.println("Connection accepted from: " + address);
					new serveRemoteGuiThread(socket).start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {

				serverSocket.close();
			} catch (Exception e) {

			}
		}


		class serveRemoteGuiThread extends Thread {
			Socket socket;

			public serveRemoteGuiThread(Socket socket) {
				this.socket = socket;
			}

			public void run() {
				try {
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

					long totalNumberOfEvaluatiosDoneStep = totalNumberOfEvaluatiosDone;

					while (!evolutionDone) {
						long time = (System.currentTimeMillis() - startTime) / 1000;
						out.reset();
						out.writeObject(slaveDataVector);



						out.writeObject(new Date().toString());
						//						out
						//						.println("\tNumber of Chromosomes to be evaluated: "
						out.writeInt(totalNumberOfEvaluationsNecessary - totalNumberOfEvaluatiosDone);

						//						out.println("\tNumber of Chromosomes evaluated: "
						out.writeInt(totalNumberOfEvaluatiosDone);
						//						out
						//						.println("\tCurrent calculating power: "
						out.writeDouble((totalNumberOfEvaluatiosDone - totalNumberOfEvaluatiosDoneStep)/ 2.0 );
						//+ " Chromosomes/sec");
						//						// out.println("\tOutput dir:       " +
						//						// diskStorage.getOutputDirectory());
						//						// out.println("\tGeneration:       " +
						//						// population.getNumberOfChromosomesEvaluated() + " / "
						//						// + population.getPopulationSize() + " [" +
						//						// population.getNumberOfCurrentGeneration() + " / " +
						//						// population.getNumberOfGenerations() + "]");
						//						out.println("\tNumber of clients: "
						out.writeInt(numberOfRunningClients);
						//						out.println("\tNumber of slaves: "
						out.writeInt( numberOfRunningSlaves);
						//						out.println("\tTime spent:       "
						out.writeObject( Util.formatIntoHHMMSS(time));
						//						out.print("\tTime left:        ");
						//
						if (totalNumberOfEvaluatiosDone > 0) {
							//							out
							out.writeObject(Util
									.formatIntoHHMMSS((System
											.currentTimeMillis() - startTime)
											/ totalNumberOfEvaluatiosDone
											* (totalNumberOfEvaluationsNecessary - totalNumberOfEvaluatiosDone)
											/ 2000));
						} else {
							out.writeObject("N/A");
						}
						//
						//						// Enumeration<SlaveData> iterator = slaveDataVector
						//						// .elements();
						//						// while (iterator.hasMoreElements()) {
						//						// SlaveData slaveData = iterator.nextElement();
						//						// out
						//						// .print(slaveData.slaveAddress
						//						// + " ("
						//						// + slaveData.numberOfChromosomesProcessed
						//						// + ", "
						//						// + (slaveData.lastChromosomeTime / 1000.0)
						//						// + ", " + slaveData.connectionCount
						//						// + ")   ");
						//						// }
						//						// out.println();
						//
						//						// out.printf("\tFitness:          max: %10.6f, avg: %10.6f, min: %10.6f\n",
						//						// maxFitness, averageFitness, minFitness);
						totalNumberOfEvaluatiosDoneStep = totalNumberOfEvaluatiosDone;
						//						sleep(10000);
						//						if (in.ready()) {
						//							break;
						//						}



						sleep(1000);
					}
				} catch (IOException e) {
					
				} catch (InterruptedException e) {
					
				}

				if (evolutionDone) {
					System.out.println("Evolution done!");
				}

				System.out.println("Closing connection to remote Gui");

				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
