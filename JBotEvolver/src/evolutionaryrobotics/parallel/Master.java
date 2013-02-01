package evolutionaryrobotics.parallel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;
import evolutionaryrobotics.util.Util;

public class Master {
	public static final  int   DEFAULTMASTERSLAVEPORT = 10000;
	public static final int   DEFAULTMASTERINFOPORT  = 9999;

	public int   masterSlavePort = DEFAULTMASTERSLAVEPORT;
	public int   masterInfoPort  = DEFAULTMASTERINFOPORT;
	
	Vector<SlaveData> slaveDataVector = new Vector<SlaveData>();

	public static final Integer MASTERVERSION = new Integer(81);

	
	Population 		   population;
	int                numberOfChromosomesLeft      = 0;
	int                numberOfChromosomesEvaluated = 0;
	boolean            evolutionDone = false;
	DiskStorage        diskStorage;
	long               randomSeed;
	String             slaveFilename;
	Arguments          experimentArguments;
	Arguments          environmentArguments;
	Arguments          robotArguments;
	Arguments          controllerArguments;
	Arguments          evaluationArguments;
	LinkedList<Chromosome> pendingChromosome= new LinkedList<Chromosome>();

	double maxFitness     = 0;
	double averageFitness = 0;
	double minFitness     = 0; 
	
	int                    numberOfRunningSlaves = 0;
	int 				   totalNumberOfEvaluationsNecessary;
	int                    totalNumberOfEvaluatiosDone = 0;
	long                   startTime;

	boolean                withGui;
	
	protected Simulator simulator;

	public Master(Simulator simulator, String slaveFilename, Arguments experimentArguments, Arguments environmentArguments, Arguments robotArguments, Arguments controllerArguments, Population population, Arguments evaluationArguments, DiskStorage diskStorage, boolean withGui) {
		this.simulator = simulator;
		numberOfChromosomesLeft = population.getPopulationSize();
		this.withGui            = withGui;
		this.population         = population;
		this.diskStorage        = diskStorage;
		this.slaveFilename      = slaveFilename;
		this.experimentArguments  = experimentArguments;
		this.environmentArguments = environmentArguments;
		this.robotArguments       = robotArguments;
		this.controllerArguments  = controllerArguments;
		this.evaluationArguments  = evaluationArguments;

		totalNumberOfEvaluationsNecessary = (population.getNumberOfGenerations() - population.getNumberOfCurrentGeneration()) * population.getPopulationSize();
		startTime = System.currentTimeMillis();

		randomSeed = simulator.getRandom().nextLong();

		if (population.getPopulationSize() - population.getNumberOfChromosomesEvaluated() == 0) {
			population.createNextGeneration();
		}
	}

	class SlaveThread extends Thread {
		Socket socket;
		ObjectOutputStream out;
		ObjectInputStream in;

		public SlaveThread(String host, int port) throws IOException {	
			InetAddress address = InetAddress.getByName(host);
			socket = new Socket(address, port);
			System.out.println("--- connection established to " + host + ":" + port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in  = new ObjectInputStream(socket.getInputStream());

		}

		public SlaveThread(Socket socket) throws IOException {	
			this.socket = socket;
			out = new ObjectOutputStream(socket.getOutputStream());
			in  = new ObjectInputStream(socket.getInputStream());

		}

		public void run() {
			try {
				Integer slaveVersion = (Integer) in.readObject();
				if (slaveVersion.intValue() != MASTERVERSION.intValue()) {
					out.writeObject(new Boolean(false));
					return;
				} else {					
					out.writeObject(new Boolean(true));
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			} 
			
			SlaveData slaveData = new SlaveData();

			synchronized(slaveDataVector) {
				slaveDataVector.add(slaveData);
			}
			slaveData.startTime = new Date().toString();			
			slaveData.slaveAddress = socket.getInetAddress().getHostName().toString();
			slaveData.slavePort    = socket.getPort();
			
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
				socket.setSoTimeout(60000);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

			synchronized(Master.this) {
				numberOfRunningSlaves++;
			}
			boolean keepRunning = true;
			Chromosome chromosome=null;
			try {
				while (keepRunning) {				
					slaveData.slaveStatus = "Waiting...";
					chromosome = getChromosome(slaveData);
					slaveData.running      = true;
					if (chromosome == null) {	
						slaveData.slaveStatus = "Done.";
						out.writeObject(new Boolean(false));
						out.flush();
						keepRunning = false;
						System.out.println(this.toString() + ": Done, closing connection to " + socket);

					} else {
						
						slaveData.slaveStatus = "Computing...";
						System.out.println(this.toString() + ": Sending chromosome to " + socket);
					
						out.reset();
						out.writeObject(new Boolean(true));

						
						out.writeObject(experimentArguments);
						out.writeObject(environmentArguments);
						out.writeObject(robotArguments);
						out.writeObject(controllerArguments);
						out.writeObject(evaluationArguments);
						
						long thisChromosomeStartTime = System.currentTimeMillis();
						out.writeObject(new Long(randomSeed));
						out.writeObject(chromosome);
						out.writeObject(new Integer(population.getNumberOfSamplesPerChromosome()));
						out.writeObject(new Integer(population.getNumberOfStepsPerSample()));	
						
						double fitness = in.readDouble();
						
						System.out.println(this.toString() + ": " + experimentArguments.getArgumentAsString("output") + ", received fitness: " + fitness);
						setResult(chromosome, fitness);
						slaveData.numberOfChromosomesProcessed++;
						slaveData.averageTimePerChromosome = (double) ((System.currentTimeMillis() - startTime) + 1) / (1000.0 * slaveData.numberOfChromosomesProcessed);
						long thisTime = System.currentTimeMillis() - thisChromosomeStartTime;
						slaveData.lastChromosomeTime	= thisTime;

						try {
							socket.setSoTimeout((int) (thisTime*5));
						} catch (SocketException e1) {
							e1.printStackTrace();
						}

					}
				}
				slaveData.slaveStatus  = "Ended normally.";
			} catch (Exception e) {
				slaveData.slaveStatus  = "Ended due to exception.";
				addChromosome(chromosome);
				try {
					e.printStackTrace();
					socket.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
			slaveData.endTime      = new Date().toString();
			slaveData.running      = false;

			synchronized(Master.this) {
				numberOfRunningSlaves--;
			}

		}
	}

	private double getAverageTimePerChromosomeOfRunningSlaves() {
		double sum        = 0;
		int runningSlaves = 0;
		synchronized(slaveDataVector) {
			for (SlaveData sd : slaveDataVector) {
				if (sd.running) {
					sum           += sd.averageTimePerChromosome;
					runningSlaves++;
				}
			}
		}
		if (runningSlaves == 0)
			return 1e10;
		else
			return sum / runningSlaves;
	}
	
	private double getAverageLastChromosomeTimeOfRunningSlaves() {
		double sum        = 0;
		int runningSlaves = 0;
		synchronized(slaveDataVector) {
			for (SlaveData sd : slaveDataVector) {
				if (sd.running) {
					sum += sd.lastChromosomeTime;
					runningSlaves++;
				}
			}
		}
		if (runningSlaves == 0)
			return 1e10;
		else
			return sum / runningSlaves;
	}

	
	private synchronized Chromosome getChromosome(SlaveData slaveData) {
		Chromosome chromosome;
		try {			
			if (slaveData.numberOfChromosomesProcessed > 1) {
				while (getAverageLastChromosomeTimeOfRunningSlaves() * (1 + numberOfChromosomesLeft / numberOfRunningSlaves) < slaveData.lastChromosomeTime|| 
						(numberOfChromosomesLeft <=  numberOfRunningSlaves && 
					   getAverageLastChromosomeTimeOfRunningSlaves() < slaveData.lastChromosomeTime / 1.5)) {
//					System.out.println("avg: " + getAverageLastChromosomeTimeOfRunningSlaves() + "slave: " + slaveData.lastChromosomeTime);
					slaveData.slaveStatus = "Waiting because slow...";
					notifyAll();
					wait();
				} 	
			}

			while (numberOfChromosomesLeft == 0 && !evolutionDone) {
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Was interrupted while waiting!");
			return null;
		}

		if (evolutionDone) {
			System.out.println("Evolution done!");
			return null;
		} else {
			if (pendingChromosome.size() > 0){
				chromosome = pendingChromosome.pollFirst();
				if (chromosome == null) {
					System.out.println("XXX: get null chromosome from pending chromosome list!");
				}

			} else {
				chromosome = population.getNextChromosomeToEvaluate();
				if (chromosome == null) {
					System.out.println("XXX: get null chromosome from population!");				
				}
			}
			numberOfChromosomesLeft--;
			
			return chromosome;
		} 
	}

	private synchronized void addChromosome(Chromosome chromosome){
		pendingChromosome.add(chromosome);
		numberOfChromosomesLeft++;
		notifyAll();
	}

	private synchronized void setResult(Chromosome chromosome, double fitness) {
		population.setEvaluationResult(chromosome, fitness);
		numberOfChromosomesEvaluated++;
		totalNumberOfEvaluatiosDone++;
		if (numberOfChromosomesEvaluated == population.getPopulationSize()) {
			try {
				diskStorage.savePopulation(population,simulator.getRandom());
				maxFitness     = population.getHighestFitness();
				averageFitness = population.getAverageFitness();
				minFitness     = population.getLowestFitness();

			} catch (IOException e) {
				e.printStackTrace();
			}

			population.createNextGeneration();
			if (population.evolutionDone()) {
				evolutionDone = true;
			} else {
				numberOfChromosomesEvaluated = 0;
				numberOfChromosomesLeft      = population.getPopulationSize();
				randomSeed                   = simulator.getRandom().nextLong();
			}
			notifyAll();
		}
	}

	class ReceiveSlaveConnectionsThread extends Thread {
		public ReceiveSlaveConnectionsThread() {

		}
		public void run() {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(masterSlavePort);
				System.out.println("Waiting for slave connections on socket: " + serverSocket);
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

	class MasterGui extends JFrame implements ActionListener {
		JButton addSlave                   = new JButton("Add slave");
		JButton quit                       = new JButton("Quit");
		JTextField slaveAdress             = new JTextField("localhost 8000");
		JTextField numberOfSlavesTextField = new JTextField("");
		JTextField currentGeneration       = new JTextField("");
		JTextField timeSpent               = new JTextField("");
		JTextField timeETA                 = new JTextField("N/A");
		JTextField timeAverageTimePerChromosome = new JTextField("N/A");
		JTextField maxFitnessTextField          = new JTextField("0");
		JTextField avgFitnessTextField          = new JTextField("0");
		SlaveTableModel slaveTableModel;

		public MasterGui() {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setTitle("RobotSimulator -- Master");
			setExtendedState(JFrame.MAXIMIZED_BOTH);
			JPanel sideWrapperPanel = new JPanel();
			sideWrapperPanel.setLayout(new BorderLayout());
			JPanel sidePanel        = new JPanel();
			sideWrapperPanel.add(sidePanel, BorderLayout.NORTH);
			
			sidePanel.setLayout(new GridLayout(12, 2));			
			sidePanel.add(new JLabel("Master version: "));
			sidePanel.add(new JLabel(Master.MASTERVERSION.toString()));
			sidePanel.add(new JLabel("Output directory: "));
			sidePanel.add(new JLabel(diskStorage.getOutputDirectory()));
			sidePanel.add(new JLabel("New slave ip port: "));
			sidePanel.add(slaveAdress);
			sidePanel.add(new JLabel(""));
			sidePanel.add(addSlave);
			sidePanel.add(new JLabel("# slaves: "));
			sidePanel.add(numberOfSlavesTextField);
			sidePanel.add(new JLabel("evolution: "));
			sidePanel.add(currentGeneration);
			sidePanel.add(new JLabel("best fitness: "));
			sidePanel.add(maxFitnessTextField);
			sidePanel.add(new JLabel("avg fitness: "));
			sidePanel.add(avgFitnessTextField);
			sidePanel.add(new JLabel("time spent: "));
			sidePanel.add(timeSpent);
			sidePanel.add(new JLabel("time left: "));
			sidePanel.add(timeETA);
			sidePanel.add(new JLabel("average t per c: "));
			sidePanel.add(timeAverageTimePerChromosome);
			sidePanel.add(new JLabel(""));
			sidePanel.add(quit);
			addSlave.addActionListener(this);
			slaveAdress.addActionListener(this);
			quit.addActionListener(this);
			add(sideWrapperPanel, BorderLayout.WEST);

			slaveTableModel = new SlaveTableModel();
			getContentPane().add(new JScrollPane(new JTable(slaveTableModel)), BorderLayout.CENTER);
			setVisible(true);

			new Thread() {
				int oldNumberOfSlaves = 0;
				public void run() {
					int currentRefresh = 0;
					while(true) {
						currentGeneration.setText(population.getNumberOfChromosomesEvaluated() + " / " + population.getPopulationSize() + " [" + population.getNumberOfCurrentGeneration() + " / " + population.getNumberOfGenerations() + "]");
						numberOfSlavesTextField.setText("" + numberOfRunningSlaves);
						timeSpent.setText(Util.formatIntoHHMMSS((System.currentTimeMillis() - startTime) / 1000));

						if (totalNumberOfEvaluatiosDone > 0 && currentRefresh % 50 == 0) {
							timeETA.setText(Util.formatIntoHHMMSS((System.currentTimeMillis() - startTime) / totalNumberOfEvaluatiosDone * (totalNumberOfEvaluationsNecessary - totalNumberOfEvaluatiosDone) / 1000));							
						}

						if (totalNumberOfEvaluatiosDone > 0 && currentRefresh % 50 == 0) {
							timeAverageTimePerChromosome.setText(getAverageTimePerChromosomeOfRunningSlaves() + " s");							
						}
						
						maxFitnessTextField.setText(String.format("%12.6f", maxFitness));
						avgFitnessTextField.setText(String.format("%12.6f", averageFitness));
						
						if (currentRefresh % 10 == 0) {
							slaveTableModel.fireTableDataChanged();
						}

						if (oldNumberOfSlaves != slaveDataVector.size()) {
							oldNumberOfSlaves = slaveDataVector.size();
							slaveTableModel.fireTableStructureChanged();
						}

						repaint();
						currentRefresh++;

						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}


//		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == addSlave || e.getSource() == slaveAdress) {
				String   input = slaveAdress.getText();
				String[] split = input.split(" ");

				if (split.length != 2) {
					JOptionPane.showMessageDialog(null, "Error: cannot interpret line: " + input + ", got the following array: " + split);
				} else {
					try {
						SlaveThread newSlave = new SlaveThread(split[0], Integer.parseInt(split[1]));
						newSlave.start();
					} catch (NumberFormatException e1) {
						JOptionPane.showMessageDialog(null, "Error: cannot read port number from '" + split[1] + "'");					
					}  catch(IOException e1) {
						JOptionPane.showMessageDialog(null, "Error: couldn't connect to: " + input);
						e1.printStackTrace();
					}
				}
			} else if (e.getSource() == quit) {
				int quit = JOptionPane.showConfirmDialog(null, "Are you sure you want to terminate the evolution?", "Quit?", JOptionPane.YES_NO_OPTION);
				if (quit == JOptionPane.YES_OPTION) {
					System.exit(0);
				}
			}
		}

		class SlaveTableModel extends AbstractTableModel {
//			@Override
			public int getColumnCount() {

				return 8;
			}

//			@Override
			public int getRowCount() {
				return slaveDataVector.size();
			}

			public String getColumnName(int x) {
				switch (x) {
				case 0: return "Number"; 
				case 1: return "IP"; 
				case 2: return "Port"; 
				case 3: return "Start time"; 
				case 4: return "End time"; 
				case 5: return "Chromosomes processed"; 
				case 6: return "Average time per chromosome"; 
				case 7: return "Status";

				}
				return "Unknown";
			}


//			@Override
			public Object getValueAt(int y, int x) {
				SlaveData slaveData = slaveDataVector.elementAt(slaveDataVector.size() - y - 1);
				switch (x) {
				case 0: return new Integer(slaveDataVector.size() - y - 1); 
				case 1: return slaveData.slaveAddress; 
				case 2: return new Integer(slaveData.slavePort); 
				case 3: return slaveData.startTime; 
				case 4: return slaveData.endTime; 
				case 5: return new Integer(slaveData.numberOfChromosomesProcessed); 
				case 6: return String.format("%4.2fs", slaveData.averageTimePerChromosome); 		
				case 7: return slaveData.slaveStatus;
				}
				return "Unknown";
			}
		}				

	}

	public class RemoteInfoThread extends Thread {
		public RemoteInfoThread() {
		}

		public void run() {
			ServerSocket serverSocket = null;
			Socket       socket = null;
			try {
				serverSocket = new ServerSocket(masterInfoPort);
				System.out.println("RemoteInfo opened server socket on port: " + masterInfoPort);
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
					in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

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

					String ipaddress = "10.10.35.123";				
					while (!evolutionDone) {
						out.println(new Date().toString());
						out.println("\tOutput dir:       " + diskStorage.getOutputDirectory());
						out.println("\tGeneration:       " + population.getNumberOfChromosomesEvaluated() + " / " + population.getPopulationSize() + " [" + population.getNumberOfCurrentGeneration() + " / " + population.getNumberOfGenerations() + "]");
						out.println("\tNumber of slaves: " + numberOfRunningSlaves);
						out.println("\tTime spent:       " + Util.formatIntoHHMMSS((System.currentTimeMillis() - startTime) / 1000));
						out.print("\tTime left:        ");
						
						if (totalNumberOfEvaluatiosDone > 0) {
							out.println(Util.formatIntoHHMMSS((System.currentTimeMillis() - startTime) / totalNumberOfEvaluatiosDone * (totalNumberOfEvaluationsNecessary - totalNumberOfEvaluatiosDone) / 1000));							
						} else {
							out.println("N/A");											
						}

						out.printf("\tFitness:          max: %10.6f, avg: %10.6f, min: %10.6f\n", maxFitness, averageFitness, minFitness);

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

	public void execute() {
		if (withGui) {
			MasterGui dialog = new MasterGui();
		} 

		RemoteInfoThread remoteInfoThread = new RemoteInfoThread();
		remoteInfoThread.start();

		ReceiveSlaveConnectionsThread receiveSlaveConnectionsThread = new ReceiveSlaveConnectionsThread();
		receiveSlaveConnectionsThread.start();

		if (slaveFilename != null) {
			BufferedReader bufferedReader;
			String nextLine;
			int lineNumber = 1;
	
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(slaveFilename))));
	
				while ((nextLine = bufferedReader.readLine()) != null) {
					String[] split = nextLine.split(" ");
	
					if (split.length != 2) {
						System.err.println("Error: cannot interpret slave file line " + lineNumber + ": " + nextLine + ", got the following array: " + split);
					} else {
						try {
							SlaveThread newSlave = new SlaveThread(split[0], Integer.parseInt(split[1]));
							newSlave.start();
						} catch (NumberFormatException e) {
							System.err.println("Error: cannot read port number in line " + lineNumber + ": " + nextLine +  ", '" + split[1] + "'");					
						}  catch(IOException e) {
							System.err.println("Error: couldn't connect to (line " + lineNumber + "): " + nextLine);
							e.printStackTrace();
						}
					}
					lineNumber++;
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		synchronized(this) {
			while (!evolutionDone) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
