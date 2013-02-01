package evolutionaryrobotics.parallel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import evolutionaryrobotics.util.Util;

public class MultiMasterRemoteGui extends JFrame {
	JButton addSlave = new JButton("Add slave");
	JButton quit = new JButton("Quit");
	JTextField slaveAdress = new JTextField("localhost 8000");
	JTextField numChromToEvaluate = new JTextField("");
	JTextField numChromEvaluated = new JTextField("");
	JTextField numSlaves = new JTextField("");
	JTextField timeETA = new JTextField("N/A");
	JTextField timeSpent = new JTextField("N/A");
	JTextField calcPower = new JTextField("0");
	JTextField numClients = new JTextField("0");
	SlaveTableModel slaveTableModel;

	private int multiMasterPort;
	private String masterAddress;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;

	private Hashtable<String, SlaveData> slaveDataVector = new Hashtable<String, SlaveData>();
	protected String[] keys = new String[10];

	public MultiMasterRemoteGui(String[] args) {


		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("RobotSimulator -- Master");
		//setExtendedState(JFrame.MAXIMIZED_BOTH);
		JPanel sideWrapperPanel = new JPanel();
		sideWrapperPanel.setLayout(new BorderLayout());
		JPanel sidePanel = new JPanel();
		sideWrapperPanel.add(sidePanel, BorderLayout.NORTH);

		sidePanel.setLayout(new GridLayout(12, 2));
		sidePanel.add(new JLabel("Master version: "));
		sidePanel.add(new JLabel(Master.MASTERVERSION.toString()));
		//		sidePanel.add(new JLabel("Output directory: "));
		//		 sidePanel.add(new JLabel());//diskStorage.getOutputDirectory()));
		//		sidePanel.add(new JLabel("New slave ip port: "));
		//		sidePanel.add(slaveAdress);
		//		sidePanel.add(new JLabel(""));
		//		sidePanel.add(addSlave);


		//		Number of Chromosomes to be evaluated: 3883963
		//		Number of Chromosomes evaluated: 7420937
		//		Current calculating power: 23.4 Chromosomes/sec
		//		Number of clients: 14
		//		Number of slaves: 30
		//		Time spent:       71h32m12s
		//		Time left:        36h40m54s

		sidePanel.add(new JLabel("# Chromosomes: "));
		sidePanel.add(numChromToEvaluate);
		sidePanel.add(new JLabel("# Chromosomes evaluated: "));
		sidePanel.add(numChromEvaluated);
		sidePanel.add(new JLabel("Power (Chrom/s): "));
		sidePanel.add(calcPower);
		sidePanel.add(new JLabel("# Clients: "));
		sidePanel.add(numClients);
		sidePanel.add(new JLabel("# Slaves: "));
		sidePanel.add(numSlaves);
		sidePanel.add(new JLabel("time spent: "));
		sidePanel.add(timeSpent);
		sidePanel.add(new JLabel("time left: "));
		sidePanel.add(timeETA);
		sidePanel.add(new JLabel(""));
		sidePanel.add(quit);
		//		addSlave.addActionListener(this);
		//		slaveAdress.addActionListener(this);
		//		quit.addActionListener(this);
		add(sideWrapperPanel, BorderLayout.WEST);

		slaveTableModel = new SlaveTableModel();
		getContentPane().add(new JScrollPane(new JTable(slaveTableModel)),
				BorderLayout.CENTER);
		setVisible(true);

		multiMasterPort = MultiMaster.DEFAULTGUIPORT;
		if(args.length==1){
			masterAddress = args[0];
		} else {
			masterAddress = "evolve.dcti.iscte.pt";
		}

		InetAddress address;
		try {
			address = InetAddress.getByName(masterAddress);
			socket = new Socket(address, multiMasterPort);
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeInt(MultiMaster.STARTING_GUI);

			new Thread() {
				int oldNumberOfSlaves = 0;

				@SuppressWarnings("unchecked")
				public void run() {
					int currentRefresh = 0;
					try{
						while (true) {
							try {
								slaveDataVector = (Hashtable<String, SlaveData>) in.readObject();
								if(slaveDataVector.keySet() !=  null){
									keys =  slaveDataVector.keySet().toArray(keys);
									slaveTableModel.fireTableDataChanged();
								} else { 
									keys = null;
								}


								String currentServerTime = (String) in.readObject();

								numChromToEvaluate.setText(in.readInt()+"");

								numChromEvaluated.setText(in.readInt()+"");

								calcPower.setText(in.readDouble()+"");

								numClients.setText(in.readInt()+"");
								numSlaves.setText(in.readInt()+"");

								timeSpent.setText((String) in.readObject());

								timeETA.setText((String) in.readObject());

							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							//					currentGeneration.setText(population
							//							.getNumberOfChromosomesEvaluated()
							//							+ " / "
							//							+ population.getPopulationSize()
							//							+ " ["
							//							+ population.getNumberOfCurrentGeneration()
							//							+ " / "
							//							+ population.getNumberOfGenerations() + "]");
							//					numberOfSlavesTextField.setText("" + numberOfRunningSlaves);
							//					timeSpent.setText(Util.formatIntoHHMMSS((System
							//							.currentTimeMillis() - startTime) / 1000));
							//
							//					if (totalNumberOfEvaluatiosDone > 0
							//							&& currentRefresh % 50 == 0) {
							//						timeETA
							//								.setText(Util
							//										.formatIntoHHMMSS((System
							//												.currentTimeMillis() - startTime)
							//												/ totalNumberOfEvaluatiosDone
							//												* (totalNumberOfEvaluationsNecessary - totalNumberOfEvaluatiosDone)
							//												/ 1000));
							//					}
							//
							//					if (totalNumberOfEvaluatiosDone > 0
							//							&& currentRefresh % 50 == 0) {
							//						timeAverageTimePerChromosome
							//								.setText(getAverageTimePerChromosomeOfRunningSlaves()
							//										+ " s");
							//					}
							//
							//					maxFitnessTextField.setText(String.format("%12.6f",
							//							maxFitness));
							//					avgFitnessTextField.setText(String.format("%12.6f",
							//							averageFitness));
							//
							//					if (currentRefresh % 10 == 0) {
							//						slaveTableModel.fireTableDataChanged();
							//					}
							//
							//					if (oldNumberOfSlaves != slaveDataVector.size()) {
							//						oldNumberOfSlaves = slaveDataVector.size();
							//						slaveTableModel.fireTableStructureChanged();
							//					}

							repaint();
							currentRefresh++;

							//					try {
							//						sleep(100);
							//					} catch (InterruptedException e) {
							//						e.printStackTrace();
							//					}
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}.start();

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	class SlaveTableModel extends AbstractTableModel {
		// @Override
		public int getColumnCount() {

			return 8;
		}

		// @Override
		public int getRowCount() {
			return slaveDataVector.size();
		}

		public String getColumnName(int x) {
			switch (x) {
			case 0:
				return "Number";
			case 1:
				return "IP";
			case 2:
				return "Port";
			case 3:
				return "Start time";
			case 4:
				return "End time";
			case 5:
				return "Chromosomes processed";
			case 6:
				return "Average time per chromosome";
			case 7:
				return "Status";

			}
			return "Unknown";
		}

		// @Override
		public Object getValueAt(int y, int x) {

			SlaveData slaveData = slaveDataVector.get(keys [slaveDataVector.size()- y - 1]);
			switch (x) {
			case 0:
				return new Integer(slaveDataVector.size() - y - 1);
			case 1:
				return slaveData.slaveAddress;
			case 2:
				return new Integer(slaveData.slavePort);
			case 3:
				return slaveData.startTime;
			case 4:
				return slaveData.endTime;
			case 5:
				return new Integer(slaveData.numberOfChromosomesProcessed);
			case 6:
				return String.format("%4.2fs",
						slaveData.averageTimePerChromosome);
			case 7:
				return slaveData.slaveStatus;
			}
			return "Unknown";
		}
	}

	public static void main(String[] args) {
		MultiMasterRemoteGui gui = new MultiMasterRemoteGui(args);
		gui.setVisible(true);
	}

}
