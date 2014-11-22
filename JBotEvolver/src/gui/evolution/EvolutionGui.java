package gui.evolution;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.ViewerMain;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import taskexecutor.TaskExecutor;

public class EvolutionGui extends JFrame {

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
	
	public EvolutionGui(JBotEvolver jBotEvolver) {
		super("Evolution GUI");
		
		TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
		taskExecutor.start();
		evo = Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
		population = evo.getPopulation();
		
		configName = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
		
		time = System.currentTimeMillis();
		
		getContentPane().add(createGraphPanel());
		getContentPane().add(createnfoPanel(), BorderLayout.EAST);
		
		UpdateEvolutionThread updateThread = new UpdateEvolutionThread();
		updateThread.start();
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(880, 320);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public void executeEvolution() {
		evo.executeEvolution();
		taskExecutor.stopTasks();
	}

	private Component createGraphPanel() {
		JPanel graphPanel = new JPanel(new BorderLayout());
		fitnessGraph = new GraphingData();
		fitnessGraph.setShowLast(population.getNumberOfGenerations());
		fitnessGraph.setxLabel("Generations", population.getNumberOfGenerations());
		
		graphPanel.add(fitnessGraph);
		return graphPanel;
	}
	
	private Component createnfoPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(350,getHeight()));
		
		JPanel infoPanel = new JPanel(new GridLayout(6, 1));
		
		JLabel configNameLabel = new JLabel("Configuration File Name:");
		configNameTextField = new JTextField(configName);
		configNameTextField.setEnabled(false);
		infoPanel.add(configNameLabel);
		infoPanel.add(configNameTextField);
		
		JLabel generationLabel = new JLabel("Generations:");
		generationTextField = new JTextField("0");
		generationTextField.setEnabled(false);
		infoPanel.add(generationLabel);
		infoPanel.add(generationTextField);
		
		JLabel bestLabel = new JLabel("Best:");
		bestTextField = new JTextField("N/A");
		bestTextField.setEnabled(false);
		infoPanel.add(bestLabel);
		infoPanel.add(bestTextField);
		
		JLabel averageLabel = new JLabel("Average:");
		averageTextField = new JTextField("N/A");
		averageTextField.setEnabled(false);
		infoPanel.add(averageLabel);
		infoPanel.add(averageTextField);
		
		JLabel worstLabel = new JLabel("Worst:");
		worstTextField = new JTextField("N/A");
		worstTextField.setEnabled(false);
		infoPanel.add(worstLabel);
		infoPanel.add(worstTextField);
		
		JLabel etaLabel = new JLabel("ETA:");
		etaTextField = new JTextField("N/A");
		etaTextField.setEnabled(false);
		etaTextField.setEditable(false);
		infoPanel.add(etaLabel);
		infoPanel.add(etaTextField);
		
		JPanel progessPanel = new JPanel(new GridLayout(3,1));
		
		JPanel generationProgessPanel = new JPanel(new GridLayout(2,1));
		JLabel generationsProgressLabel = new JLabel("Generation Progess:");
		generationsProgressBar = new JProgressBar(0,100);
		generationsProgressBar.setStringPainted(true);
		generationProgessPanel.add(generationsProgressLabel);
		generationProgessPanel.add(generationsProgressBar);
		progessPanel.add(generationProgessPanel);
		
		JPanel evolutionProgessPanel = new JPanel(new GridLayout(2,1));
		JLabel evolutionProgressLabel = new JLabel("Evolution Progess:");
		evolutionProgressBar = new JProgressBar(0,100);
		evolutionProgressBar.setStringPainted(true);
		evolutionProgessPanel.add(evolutionProgressLabel);
		evolutionProgessPanel.add(evolutionProgressBar);
		progessPanel.add(evolutionProgessPanel);
		
		JButton viewerButton = new JButton("Show Result Viewer");
		viewerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					new ViewerMain(new String[]{"--gui","classname=ResultViewerGui,renderer=(classname=TwoDRenderer))"});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		progessPanel.add(viewerButton);
		
		mainPanel.add(infoPanel);
		mainPanel.add(progessPanel, BorderLayout.SOUTH);
		
		mainPanel.setBorder(BorderFactory.createTitledBorder("Status"));
		return mainPanel;
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
