import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.PostEvaluation;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class Evolution extends Thread {
	
	private Controller controller;
	private HashMap<String, Arguments> defaultArgs;
	private Main main;
	private String outputFolder;
	
	public Evolution(Main main, Controller controller, HashMap<String, Arguments> arguments) {
		this.controller = controller;
		this.defaultArgs = arguments;
		this.main = main;
		controller.setEvolving(true);
	}
	
	@Override
	public void run() {
		
		controller.createArguments(defaultArgs);
		
		if(!controller.skipEvolution()) {
			try {
				outputFolder = main.getFolderName()+"/"+controller.getName();
				
				createConfigFile(outputFolder,controller.getName()+".conf");
				runEvolutions();
				System.out.println("Evolution finished, running Post Eval on "+controller.getName());
				int run = runPostEvaluation();
				System.out.println("Post-evaluation on "+controller.getName()+": "+run);
				String weights = getWeights(run);
				System.out.println("Got weights (#"+weights.length()+") for "+controller.getName()+": "+run);
				controller.setWeights(weights);
				createConfigFile(outputFolder,"best.conf");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		main.evolutionFinished(controller.getName());
	}
	
	private String getWeights(int run) {
		
		String execute = outputFolder+"/"+run+"/_showbest_current.conf";
		String weights = "";
		try {
			JBotEvolver jBotEvolver = new JBotEvolver(execute.split(" "));
			Chromosome chromosome = jBotEvolver.getPopulation().getBestChromosome();
			weights = chromosome.getAllelesString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(weights.isEmpty())
			throw new RuntimeException("Weights are empty!");
		
		return weights;
	}
	
	private int runPostEvaluation() throws Exception {
		
		File post = new File(outputFolder+"/post.txt");
		
		if(post.exists())
			return Integer.parseInt(new Scanner(post).nextLine().split(":")[1].trim());
		
		String runsVariable = main.getGlobalVariable("%runs");
		int nRuns = 1;
		if(runsVariable != null)
			nRuns = Integer.parseInt(runsVariable);
		
		Arguments postArguments = controller.getArguments("--postevaluation");
		String stringArguments = "dir="+outputFolder;
		if(postArguments != null) {
			for(String arg : postArguments.getArguments())
				stringArguments+=" "+arg+"="+postArguments.getArgumentAsString(arg);
		}
		
		PostEvaluation postEval = new PostEvaluation(stringArguments.split(" "));
		double[][] values = postEval.runPostEval();
		
		double[] results = new double[nRuns];
		double[] averages = new double[nRuns];
		double[] stdDeviations = new double[nRuns];
		
		int maxIndex = 0;
		
		for(int i = 0 ; i < results.length ; i++) {
			
			double val = 0;
			
			for(int j = 0 ; j < values[i].length ; j++)
				val+= values[i][j];
			
			averages[i] = getAverage(values[i]);
			stdDeviations[i] = getStdDeviation(values[i],averages[i]);
			
			results[i] = val;
			
			if(results[i] > results[maxIndex])
				maxIndex = i;
		}
		
		int best = maxIndex+1;
		
		String result = "#best: "+best+"\n";
		
		for(int i = 0 ; i < values.length ; i++) {
			result+=(i+1)+" ";
			for(double j : values[i])
				result+=j+" ";
			
			result+="("+averages[i]+" +- "+stdDeviations[i]+")\n";
		}
		
		double overallAverage = getAverage(averages);
		
		result+="Overall: "+overallAverage+" +- "+getStdDeviation(averages, overallAverage);
		
		System.out.println(result);
		
		createFile(outputFolder,"post.txt",result);
		
		return best;
	}
	
	private void runEvolutions() throws Exception {
		
		if(!new File(outputFolder+"/post.txt").exists()) {
			int nRuns = 1;
			String runsVariable = main.getGlobalVariable("%runs");
			if(runsVariable != null)
				nRuns = Integer.parseInt(runsVariable);
			
			Worker[] workers = new Worker[nRuns];
			
			for(int i = 0 ; i < nRuns ; i++) {
				workers[i] = new Worker(i+1);
				workers[i].start();
			}
			
			for(Worker w : workers)
				w.join();
		}
	}
	
	private void createConfigFile(String folderName, String configName) throws Exception {
		createFile(folderName,configName,controller.getCompleteConfiguration());
	}
	
	private void createFile(String folderName, String fileName, String contents) throws Exception {
		File f = new File(folderName);
		
		if(!f.exists())
			f.mkdir();
		
		f = new File(folderName+"/"+fileName);
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write(contents);
		bw.flush();
		bw.close();
	}
	
	private double getAverage(double[] values) {
		
		double avg = 0;
		
		for(double i : values)
			avg+=i;
		
		return avg/(double)values.length;
	}
	
	private double getStdDeviation(double[] values, double avg) {
		
		double stdDeviation = 0;
		
		for(double d : values)
			stdDeviation+=Math.pow(d-avg,2);
		
		return Math.sqrt(stdDeviation/(double)values.length);
	}
	
	class Worker extends Thread {
		
		private int run;
		
		public Worker(int run) {
			this.run = run;
		}
		
		@Override
		public void run() {
			try {
				main.incrementEvolutions();
			
				File f = new File(outputFolder+"/"+run+"/_restartevolution.conf");
				
				long seed = run;
				
				if(controller.getArguments("--random-seed") != null) 
					seed+= Long.parseLong(controller.getArguments("--random-seed").getCompleteArgumentString());
				
				int generations = controller.getArguments("--population").getArgumentAsInt("generations");
				
				String runEvolution = outputFolder+"/"+controller.getName()+".conf --output "+outputFolder+"/"+run+" --random-seed "+seed;
				String resumeEvolution = outputFolder+"/"+run+"/_restartevolution.conf --population +generations="+generations;
				
				String execute = f.exists() ? resumeEvolution : runEvolution;
				System.out.println(execute);
				
				JBotEvolver jBotEvolver = new JBotEvolver(execute.split(" "));
				TaskExecutor taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
				taskExecutor.start();
				evolutionaryrobotics.evolution.Evolution a = evolutionaryrobotics.evolution.Evolution.getEvolution(jBotEvolver, taskExecutor, jBotEvolver.getArguments().get("--evolution"));
				a.executeEvolution();
				taskExecutor.stopTasks();
				
				main.decrementEvolutions();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}