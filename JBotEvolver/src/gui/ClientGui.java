package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.FileDataSet;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Smooth;
import com.panayotis.gnuplot.style.Style;
import evolutionaryrobotics.mains.ClientMain;
import evolutionaryrobotics.parallel.ParallellerClient;
import gui.util.GraphPlotter;

public class ClientGui {
	
	private static int DELAY_TIME = 60*1000;
	private static int REFRESH_TIME = 10*1000;
	private static String DEFAULT_ARGS = "--output _TRIAL_ \n" +
										 "--random-seed _TRIAL__TRIAL__TRIAL_";
	private ClientMain main;
	
	private JTextField numberField = new JTextField("10");
	private JTextField serverField = new JTextField("evolve.dcti.iscte.pt");
	private JTextField fileField = new JTextField(".");
	
	private JButton fileButton = new JButton("Open...");
	private JButton launchButton = new JButton("Launch");
	private JButton plotButton = new JButton("Plot fitness");
	
	private JFrame frame;
	private JPanel mainPanel = new JPanel();
	private JPanel progressPanel = new JPanel();
	private JEditorPane extraArgs = new JEditorPane();
	
	private LinkedList<ClientWrapper> workers = new LinkedList<ClientWrapper>();
	private LinkedList<JProgressBar> progressBars = new LinkedList<JProgressBar>();
	private LinkedList<String> outputPaths = new LinkedList<String>();
	
	private JCheckBox resume = new JCheckBox("Resume");
	private JCheckBox delay = new JCheckBox("Delay launch");
	
	public ClientGui(ClientMain main) {
		this.main = main;
		
		extraArgs.setText(DEFAULT_ARGS);
		fileField.setColumns(20);
		
		frame = new JFrame("Client Launcher"); 
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createTitledBorder("Configuration File"));
		
		topPanel.add(fileField);
		topPanel.add(fileButton);
		topPanel.add(resume);
		topPanel.add(delay);
		
		JPanel sidePanel = new JPanel(new GridLayout(7,1));
		sidePanel.setBorder(BorderFactory.createTitledBorder("Controls"));
		
		sidePanel.add(new JLabel("Server address"));
		sidePanel.add(serverField);
		sidePanel.add(new JLabel("Number of Trials"));
		sidePanel.add(numberField);
		sidePanel.add(plotButton);
		sidePanel.add(new JPanel());
		sidePanel.add(launchButton);
		
		progressPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
		mainPanel.add(topPanel,BorderLayout.NORTH);
		mainPanel.add(extraArgs,BorderLayout.CENTER);
		mainPanel.add(sidePanel,BorderLayout.EAST);
		mainPanel.add(progressPanel,BorderLayout.SOUTH);
		
		initListeners();
		
		frame.setSize(600,350);
		frame.setLocationRelativeTo(null);
		frame.add(mainPanel);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void initProgressPanel() {
	
		progressPanel.setLayout(new GridLayout(workers.size(),2));
		
		for(int i = 0 ; i < workers.size() ; i++) {
			JProgressBar progressBar = new JProgressBar(0, workers.get(i).getNumberOfGenerations());
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressBar.setBackground(Color.GREEN);
			
			progressPanel.add(new JLabel("Trial #"+(i+1)));
			progressPanel.add(progressBar);
			progressBars.add(progressBar);
		}
		
		frame.invalidate();
		frame.pack();
		progressPanel.revalidate();
		mainPanel.revalidate();
	}
	
	private void initListeners() {
		
		launchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Thread t = new Thread( new Runnable(){
					public void run(){
						disableControls();
						launchClients();
				    	initProgressPanel();
				    	refresh();
					}
				});
				t.start();
			}
		});
		
		fileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.setCurrentDirectory(new File("."));
				int returnVal = chooser.showOpenDialog(null);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					fileField.setText(chooser.getSelectedFile().getPath());
				}
			}
		});
		
		plotButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Thread t = new Thread( new Runnable(){
					public void run(){
						plotFitness();
					}
				});
				t.start();
			}
		});
	}
	
	private void plotFitness() {
		
		String[] files = new String[outputPaths.size()];
		
		for(int i = 0 ; i < files.length ; i++)
			files[i] = outputPaths.get(i)+"/_fitness.log";
		
		new GraphPlotter(files);
	}
	
	private void disableControls() {
		launchButton.setEnabled(false);
		fileButton.setEnabled(false);
		fileField.setEditable(false);
		extraArgs.setEditable(false);
	}

	private void launchClients() {
		try {
			int totalNumber = Integer.parseInt(numberField.getText());
			
			String filename = fileField.getText().trim();
			
			for(int i = 1 ; i <= totalNumber ; i++) {
				String finalFilename = resume.isSelected() ? filename+"/"+(i)+"/_restartevolution.conf" : filename;
	
				String extraArguments = resume.isSelected() ? "" : extraArgs.getText().replaceAll("_TRIAL_", ""+i);
				extraArguments+="\n--paralleler-client server="+serverField.getText().trim();
				ClientWrapper w = new ClientWrapper(finalFilename, extraArguments);
				outputPaths.add(main.getCurrentOutputPath());
				workers.add(w);
				w.start();
				
				if(delay.isSelected())
					Thread.sleep(DELAY_TIME);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void refresh() {
		try {
			while(!workers.isEmpty()) {
				for(int i = 0 ; i < workers.size() ; i++) {
					progressBars.get(i).setValue(workers.get(i).getNumberOfCurrentGeneration());
					
					if(!workers.get(i).isWorking() && progressBars.get(i).getValue() < progressBars.get(i).getMaximum())
						progressBars.get(i).setBackground(Color.RED);	
				}
				Thread.sleep(REFRESH_TIME);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private class ClientWrapper extends Thread {
		
		private ParallellerClient client;
		private boolean working = true;
		
		public ClientWrapper(String file, String extraArguments) {
			try {
				main.loadFile(file,extraArguments);
				client = main.getNewClient();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		@Override
		public void run() {
			client.execute();
			working = false;
		}
		
		public int getNumberOfGenerations() {
			return client.getNumberOfGenerations();
		}
		
		public boolean isWorking() {
			return working;
		}
		
		public int getNumberOfCurrentGeneration() {
			return client.getNumberOfCurrentGeneration();
		}
	}
}