package gui.evolution;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.populations.Population;
import gui.util.GraphingData;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Time;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import taskexecutor.TaskExecutor;

public class EvolutionGui extends JPanel {

	private static final long serialVersionUID = -63231826531435127L;

	private GraphingData fitnessGraph;
	
	private JProgressBar evolutionProgressBar;
	private JProgressBar generationsProgressBar;
	
	private JTextField configNameTextField;
	private JTextField generationTextField;
	private JTextField bestTextField;
	private JTextField averageTextField;
	private JTextField worstTextField;
	private JTextField etaTextField;
	private Evolution evo;
	private TaskExecutor taskExecutor;
	private long time;
	private Population population;
	private String configName = "";
	private JPanel graphPanel;
	private JButton stopButton;
	
	public EvolutionGui() {
//		super("Evolution GUI");
		
		
//		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		fitnessGraph = new GraphingData();
		fitnessGraph.setxLabel("Generations");
		fitnessGraph.setyLabel("Fitness");
		
		graphPanel = new JPanel(new BorderLayout());
		graphPanel.add(fitnessGraph, BorderLayout.CENTER);
		add(graphPanel, BorderLayout.CENTER);
		add(createnfoPanel(), BorderLayout.SOUTH);
		setSize(880, 320);
//		setLocationRelativeTo(null);
//		setVisible(true);
	}
	
	public void init(JBotEvolver jBotEvolver) {
		
		taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
		taskExecutor.start();
		evo = Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
		population = evo.getPopulation();
		
		configName = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
		configNameTextField.setText(configName);
		
		fitnessGraph.clear();
		fitnessGraph.setShowLast(population.getNumberOfGenerations());
		
		fitnessGraph.setxLabel("Generations ("+population.getNumberOfGenerations()+")");
		fitnessGraph.setyLabel("Fitness");
		
		time = System.currentTimeMillis();
		
		UpdateEvolutionThread updateThread = new UpdateEvolutionThread();
		updateThread.start();
	}
	
	public void executeEvolution() {
		evolutionProgressBar.setValue(0);
		generationsProgressBar.setValue(0);
		stopButton.setEnabled(true);
		evo.executeEvolution();
		taskExecutor.stopTasks();
		stopButton.setEnabled(false);
	}

	private Component createnfoPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
//		mainPanel.setPreferredSize(new Dimension(200,200));
		
		JPanel infoPanel = new JPanel(new GridLayout(2, 3));
		infoPanel.setBorder(BorderFactory.createTitledBorder("Status"));
		
//		for(int i = 0 ; i < 6 ; i++)
//			infoPanel.add(new JLabel());
		
		JLabel configNameLabel = new JLabel("Configuration:");
		configNameTextField = new JTextField(configName);
		configNameTextField.setEditable(false);
		infoPanel.add(configNameLabel);
		infoPanel.add(configNameTextField);
		
		JLabel generationLabel = new JLabel("Generations:");
		generationTextField = new JTextField("0");
		generationTextField.setEditable(false);
		infoPanel.add(generationLabel);
		infoPanel.add(generationTextField);
		
		JLabel etaLabel = new JLabel("ETA:");
		etaTextField = new JTextField("N/A");
		etaTextField.setEditable(false);
		infoPanel.add(etaLabel);
		infoPanel.add(etaTextField);
		
		JLabel bestLabel = new JLabel("Best:");
		bestTextField = new JTextField("N/A");
		bestTextField.setEditable(false);
		infoPanel.add(bestLabel);
		infoPanel.add(bestTextField);
		
		JLabel averageLabel = new JLabel("Average:");
		averageTextField = new JTextField("N/A");
		averageTextField.setEditable(false);
		infoPanel.add(averageLabel);
		infoPanel.add(averageTextField);
		
		JLabel worstLabel = new JLabel("Worst:");
		worstTextField = new JTextField("N/A");
		worstTextField.setEditable(false);
		infoPanel.add(worstLabel);
		infoPanel.add(worstTextField);
		
//		for(int i = 0 ; i < 6 ; i++)
//			infoPanel.add(new JLabel());
		
		JPanel mainProgressPanel = new JPanel(new BorderLayout());
		mainProgressPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
		
		JPanel progessPanel = new JPanel(new GridLayout(2,1));
		
		JPanel generationProgessPanel = new JPanel(new BorderLayout());
		JLabel generationsProgressLabel = new JLabel("Generation Progress:  ");
		generationsProgressBar = new JProgressBar(0,100);
		generationsProgressBar.setStringPainted(true);
		generationProgessPanel.add(generationsProgressLabel, BorderLayout.WEST);
		generationProgessPanel.add(generationsProgressBar, BorderLayout.CENTER);
		progessPanel.add(generationProgessPanel);
		
		JPanel evolutionProgessPanel = new JPanel(new BorderLayout());
		JLabel evolutionProgressLabel = new JLabel("Evolution Progress:    ");
		evolutionProgressBar = new JProgressBar(0,100);
		evolutionProgressBar.setStringPainted(true);
		evolutionProgessPanel.add(evolutionProgressLabel, BorderLayout.WEST);
		evolutionProgessPanel.add(evolutionProgressBar, BorderLayout.CENTER);
		progessPanel.add(evolutionProgessPanel);
		
		stopButton = new JButton("Stop Evolution");
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				stopEvolution();
			}
		});
		mainProgressPanel.add(progessPanel, BorderLayout.CENTER);
		mainProgressPanel.add(stopButton, BorderLayout.EAST);
		
		mainPanel.add(infoPanel, BorderLayout.WEST);
		mainPanel.add(mainProgressPanel, BorderLayout.CENTER);
		
		return mainPanel;
	}
	
	public void stopEvolution() {
		if(evo != null)
			evo.stopEvolution();
	}

	private class UpdateEvolutionThread extends Thread{
		
		private static final int HUNDRED_PERCENT = 100;
		private int generation;
		
		public UpdateEvolutionThread() {
			generation = 0;
		}
		
		@Override
		public void run() {
			if(population != null){
				while(true){
					int currentGeneration = population.getNumberOfCurrentGeneration();
					
					if(generation < currentGeneration){
						generation = currentGeneration;
						
						double[] fitnessValues = getFitnessFromFile();
						
						int numberOfGenerations = population.getNumberOfGenerations();
						
						double highestFitness = fitnessValues[0];
						double averageFitness = fitnessValues[1];
						double lowestFitness = fitnessValues[2];
						
						generationTextField.setText(currentGeneration + "");
						bestTextField.setText(highestFitness + "");
						averageTextField.setText(averageFitness + "");
						worstTextField.setText(lowestFitness + "");

						fitnessGraph.addData(highestFitness);
						
						int evolutionProgress = currentGeneration * HUNDRED_PERCENT / numberOfGenerations;
						evolutionProgressBar.setValue(evolutionProgress);
						evolutionProgressBar.setString(evolutionProgress + "%");
						
						double passedTime = (System.currentTimeMillis() - time);
						double completePercentage = currentGeneration/(double)numberOfGenerations;
						int eta = (int) (passedTime / completePercentage * (1 - completePercentage));
						etaTextField.setText(new Time(eta- 3600000).toString());
						
					}else{
						int chromosomesEvaluated = population.getNumberOfChromosomesEvaluated();
						int populationSize = population.getPopulationSize();
						
						int generationProgress = chromosomesEvaluated * HUNDRED_PERCENT / populationSize;
						generationsProgressBar.setValue(generationProgress);
						generationsProgressBar.setString(generationProgress + "%");
					}
					
					fitnessGraph.repaint();
					
					try {
						sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private double[] getFitnessFromFile() {
			Scanner scanner = null;
			try {
				scanner = new Scanner(new File(configName + "/_fitness.log"));
				String line = "";
				
				while(scanner.hasNext()){
					line = scanner.nextLine();
				}
				
				String[] splitStrings = line.split("\t");
				double[] fitnessValeus = new double[3];
				
				try {
				
					fitnessValeus[0] = Double.valueOf(splitStrings[3].trim());
					fitnessValeus[1] = Double.valueOf(splitStrings[4].trim());
					fitnessValeus[2] = Double.valueOf(splitStrings[5].trim());
				
				} catch(NumberFormatException e){}
				
				return fitnessValeus;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}finally{
				if(scanner != null)
					scanner.close();
			}
			return null;
		}
	}
	
}
