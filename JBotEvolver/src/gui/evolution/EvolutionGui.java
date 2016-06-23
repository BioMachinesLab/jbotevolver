package gui.evolution;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.populations.Population;
import gui.Gui;
import gui.renderer.Renderer;
import gui.util.Graph;
import simulation.JBotSim;
import simulation.Simulator;
import simulation.Updatable;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;

public class EvolutionGui extends Gui {

	private static final long serialVersionUID = -63231826531435127L;
	private static final long PREVIEW_SLEEP = 10;
	private Graph fitnessGraph;
	private JProgressBar evolutionProgressBar;
	private JProgressBar generationsProgressBar;
	private JTextField elapsedTextField;
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
	private Renderer renderer;
	private JBotEvolver jBotEvolver;
	private boolean enablePreview = false;
	private JTextField previewGenerationTextField;
	private UpdateEvolutionThread updateThread;
	
	public EvolutionGui(JBotSim jBotSim, Arguments args) {
		super(jBotSim, args);
		
		if(jBotSim instanceof JBotEvolver)
			this.jBotEvolver = (JBotEvolver)jBotSim;
		
		setLayout(new BorderLayout());
		
		fitnessGraph = new Graph();
		fitnessGraph.setxLabel("Generations");
		fitnessGraph.setyLabel("Fitness");
		
		graphPanel = new JPanel(new BorderLayout());
		graphPanel.setBorder(BorderFactory.createTitledBorder("Fitness Graph"));
		graphPanel.add(fitnessGraph, BorderLayout.CENTER);
		
		add(graphPanel, BorderLayout.CENTER);
		add(createInfoPanel(), BorderLayout.SOUTH);
		add(createPreviewPanel(), BorderLayout.EAST);
		
		setSize(880, 320);
		
		ShowBestThread showBestThread = new ShowBestThread();
		showBestThread.start();
		
		updateThread = new UpdateEvolutionThread();
		updateThread.start();
	}
	
	public void init() {
		init(jBotEvolver);
	}
	
	public void init(JBotEvolver jBotEvolver) {
		
		this.jBotEvolver = jBotEvolver;
		
		taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
		taskExecutor.start();
		evo = Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
		population = evo.getPopulation();
		
		configName = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
		
		fitnessGraph.setxLabel("Generations ("+population.getNumberOfGenerations()+")");
		fitnessGraph.setyLabel("Fitness");
		
		time = System.currentTimeMillis();
		elapsedTextField.setText(calculateTime(System.currentTimeMillis() - time));
		
	}
	
	public void executeEvolution() {
		evolutionProgressBar.setValue(0);
		evolutionProgressBar.setString("0%");
		generationsProgressBar.setValue(0);
		generationsProgressBar.setString("0%");
		stopButton.setEnabled(true);
		fitnessGraph.clear();
		fitnessGraph.setShowLast(population.getNumberOfGenerations());
		File f = new File(jBotEvolver.getArguments().get("--output").getCompleteArgumentString()+"/_fitness.log");
		fitnessGraph.addLegend(f.getAbsolutePath());
		newGeneration();
		evo.executeEvolution();
		updateThread.updateGraph();
		taskExecutor.stopTasks();
		stopButton.setEnabled(false);
	}
	
	private Component createPreviewPanel() {
		int panelWidth = 500;
		
		JPanel panel = new JPanel(new BorderLayout());
		
		renderer = Renderer.getRenderer(new Arguments("classname=TwoDRenderer",true));
		renderer.setPreferredSize(new Dimension(panelWidth,panelWidth));
		
		panel.add(renderer, BorderLayout.CENTER);
		
		JPanel configPanel = new JPanel();
		
		previewGenerationTextField = new JTextField(4);
		previewGenerationTextField.setText("0");
		previewGenerationTextField.setEditable(false);
		previewGenerationTextField.setHorizontalAlignment(JTextField.CENTER);
		
		configPanel.add(new JLabel("Generation: "));
		configPanel.add(previewGenerationTextField);
		
		JCheckBox enableCheckbox = new JCheckBox();
		enableCheckbox.setSelected(false);
		enableCheckbox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enablePreview = ((JCheckBox)e.getSource()).isSelected();
				newGeneration();
			}
		});
		
		configPanel.add(new JLabel("Enable preview: "));
		configPanel.add(enableCheckbox);
		
		panel.add(configPanel, BorderLayout.SOUTH);
		panel.setBorder(BorderFactory.createTitledBorder("Preview"));
		
		return panel;
	}

	private Component createInfoPanel() {
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel infoPanel = new JPanel(new GridLayout(2, 3));
		infoPanel.setBorder(BorderFactory.createTitledBorder("Status"));
		
		JLabel generationLabel = new JLabel("Generations:");
		generationTextField = new JTextField("0");
		generationTextField.setEditable(false);
		generationTextField.setHorizontalAlignment(JTextField.CENTER);
		infoPanel.add(generationLabel);
		infoPanel.add(generationTextField);
		
		JLabel configNameLabel = new JLabel("Elapsed:");
		elapsedTextField = new JTextField("N/A");
		elapsedTextField.setEditable(false);
		elapsedTextField.setHorizontalAlignment(JTextField.CENTER);
		infoPanel.add(configNameLabel);
		infoPanel.add(elapsedTextField);
		
		JLabel etaLabel = new JLabel("ETA:");
		etaTextField = new JTextField("N/A");
		etaTextField.setEditable(false);
		etaTextField.setHorizontalAlignment(JTextField.CENTER);
		infoPanel.add(etaLabel);
		infoPanel.add(etaTextField);
		
		JLabel bestLabel = new JLabel("Best:");
		bestTextField = new JTextField("N/A");
		bestTextField.setEditable(false);
		bestTextField.setHorizontalAlignment(JTextField.CENTER);
		infoPanel.add(bestLabel);
		infoPanel.add(bestTextField);
		
		JLabel averageLabel = new JLabel("Average:");
		averageTextField = new JTextField("N/A");
		averageTextField.setEditable(false);
		averageTextField.setHorizontalAlignment(JTextField.CENTER);
		infoPanel.add(averageLabel);
		infoPanel.add(averageTextField);
		
		JLabel worstLabel = new JLabel("Worst:");
		worstTextField = new JTextField("N/A");
		worstTextField.setEditable(false);
		worstTextField.setHorizontalAlignment(JTextField.CENTER);
		infoPanel.add(worstLabel);
		infoPanel.add(worstTextField);
		
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
		if(evo != null) {
			evo.stopEvolution();
		}
	}

	private class UpdateEvolutionThread extends Thread{
		
		private static final int HUNDRED_PERCENT = 100;
		private int generation;
		
		public UpdateEvolutionThread() {
			generation = 0;
		}
		
		@Override
		public void run() {
			
			while(true){
				if(population != null && !evo.isEvolutionFinished()){
					updateGraph();
					try {
						sleep(100);
					} catch (InterruptedException e) {}
				} else {
					waitForNewGeneration();
					generation = 0;
				}
			}
		}
		
		private void updateGraph() {
			int currentGeneration = population.getNumberOfCurrentGeneration();
			
			if(generation < currentGeneration){
				generation++;
				
				//We cannot get the fitness files from the population because
				//they are reset as soon as a new generation is created! We
				//have to get the values from the file instead.
				double[] fitnessValues = getFitnessFromFile();
				
				int numberOfGenerations = population.getNumberOfGenerations();
				double highestFitness = fitnessValues[0];
				double averageFitness = fitnessValues[1];
				double lowestFitness = fitnessValues[2];
				
				generationTextField.setText(generation + "");
				bestTextField.setText(highestFitness + "");
				averageTextField.setText(averageFitness + "");
				worstTextField.setText(lowestFitness + "");

				fitnessGraph.addData(highestFitness);
				
				int evolutionProgress = generation * HUNDRED_PERCENT / numberOfGenerations;
				
				evolutionProgressBar.setValue(evolutionProgress);
				evolutionProgressBar.setString(evolutionProgress + "%");
				
				generationsProgressBar.setValue(100);
				generationsProgressBar.setString("100%");
				
				updateETA();
				newGeneration();
				
			}else{
				int chromosomesEvaluated = population.getNumberOfChromosomesEvaluated();
				int populationSize = population.getPopulationSize();
				
				int generationProgress = chromosomesEvaluated * HUNDRED_PERCENT / populationSize;
				generationsProgressBar.setValue(generationProgress);
				generationsProgressBar.setString(generationProgress + "%");
			}
			
		}
		
		private void updateETA() {
			double passedTime = (System.currentTimeMillis() - time);
			double completePercentage = generation/(double)evo.getPopulation().getNumberOfGenerations();
			int eta = (int) (passedTime / completePercentage * (1 - completePercentage));
			etaTextField.setText(calculateTime(eta));
			elapsedTextField.setText(calculateTime(System.currentTimeMillis()-time));
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
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if(scanner != null)
					scanner.close();
			}
			return null;
		}
	}
	
	public synchronized void newGeneration() {
		notifyAll();
	}
	
	private synchronized void waitForNewGeneration() {
		try {
			wait();
		} catch (InterruptedException e) {}
	}
	
	@Override
	public void dispose() {
		stopEvolution();
	}
	
	class ShowBestThread extends Thread implements Updatable {
		
		private int currentGenerationNumber = 0;
		
		@Override
		public void update(Simulator simulator) {
			if(enablePreview)
				renderer.drawFrame();
		}
		
		@Override
		public void run() {
			
			while(true) {
				if(evo != null) {
					while(!evo.isEvolutionFinished()) {
						
						int evoPopulation = evo.getPopulation().getNumberOfCurrentGeneration();
						if(enablePreview && evoPopulation > 0 && evoPopulation != currentGenerationNumber){
							currentGenerationNumber = evoPopulation;
							if(evo.getPopulation().getBestChromosome() != null) {
								Simulator sim = jBotEvolver.createSimulator(new Random().nextLong());
								sim.addCallback(jBotEvolver.getEvaluationFunction()[0]);
								sim.addCallback(this);
//								ArrayList<Robot> robots = jBotEvolver.createRobots(sim);
//								jBotEvolver.setChromosome(robots, evo.getPopulation().getBestChromosome());
								ArrayList<Robot> robots = jBotEvolver.createRobots(sim, evo.getPopulation().getBestChromosome());
								sim.addRobots(robots);
								sim.setupEnvironment();
								
								previewGenerationTextField.setText(""+currentGenerationNumber);
								
								renderer.setSimulator(sim);
								renderer.drawFrame();
								
								sim.simulate(PREVIEW_SLEEP);
							}
						}else
							waitForNewGeneration();
					}
				}
				waitForNewGeneration();
			}
		}
	}
	
	public static String calculateTime(long mili) {
		
		long sec = mili/1000;
		
	    int day = (int) TimeUnit.SECONDS.toDays(sec);
	    long hours = TimeUnit.SECONDS.toHours(sec) -
	                 TimeUnit.DAYS.toHours(day);
	    long minutes = TimeUnit.SECONDS.toMinutes(sec) - 
	                  TimeUnit.DAYS.toMinutes(day) -
	                  TimeUnit.HOURS.toMinutes(hours);
	    long seconds = TimeUnit.SECONDS.toSeconds(sec) -
	                  TimeUnit.DAYS.toSeconds(day) -
	                  TimeUnit.HOURS.toSeconds(hours) - 
	                  TimeUnit.MINUTES.toSeconds(minutes);
	    
	    while(day > 0) {
	    	hours+=24;
	    	day--;
	    }
	    
	    String h = ""+hours;
	    String m = ""+minutes;
	    String s = ""+seconds;
	    
	    if(h.length() < 2) h = "0"+h;
	    if(m.length() < 2) m = "0"+m;
	    if(s.length() < 2) s = "0"+s;
	    
	    return h+":"+m+":"+s;
	}
}