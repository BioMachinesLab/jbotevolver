package evolutionaryrobotics;

import java.io.File;
import java.util.HashMap;

import evolutionaryrobotics.util.DiskStorage;

import result.Result;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.SimpleSampleTask;
import tasks.Task;

public class PostEvaluation implements MainExecutor {
	
	private int startTrial = 0;
	private int maxTrial = 0;
	private int samples = 100;
	private int fitnesssamples = 1;
	private double targetfitness = 0;
	private String dir = "";
	
	private TaskExecutor taskExecutor;
	
	public PostEvaluation(String[] args) {
		
		for(String s : args) {
			String[] a = s.split("=");
			if(a[0].equals("dir")) dir = a[1];
			if(a[0].equals("samples")) samples = Integer.parseInt(a[1]);
			if(a[0].equals("fitnesssamples")) fitnesssamples = Integer.parseInt(a[1]);
			if(a[0].equals("targetfitness")) targetfitness = Double.parseDouble(a[1]);
		}
		
		if(!dir.endsWith("/"))
			dir+="/";
		
		File subFolder;
		int currentFolder = 0;
		do {
			currentFolder++;
			subFolder = new File(dir+currentFolder);
		}while(subFolder.exists());
		
		startTrial = 1;
		maxTrial = --currentFolder;
	}
	
	public String runPostEval() {

		String result = "";
		
		try {
			for(int i = startTrial ; i <= maxTrial ; i++) {
				String file = dir+i+"/_showbest_current.conf";
				JBotEvolver jBotEvolver = new JBotEvolver(new String[]{file});
				
				if (jBotEvolver.getArguments().get("--executor") != null) {
					taskExecutor = TaskExecutor.getTaskExecutor(this, jBotEvolver, jBotEvolver.getArguments().get("--executor"));
					taskExecutor.setDaemon(true);
					taskExecutor.start();
				}
				
				result+=startTrial;
				for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
					for(int sample = 0 ; sample < samples ; sample++) {
						SimpleSampleTask t =
							new SimpleSampleTask(jBotEvolver,fitnesssample,jBotEvolver.getPopulation().getBestChromosome(),sample);
						submitTask(t);
					}
					
					double fitness = 0;
					
					for(int sample = 0 ; sample < samples ; sample++) {
						SimpleFitnessResult sfr = (SimpleFitnessResult)getResult();
						if(targetfitness > 0) {
							if(sfr.getFitness() > targetfitness)
								fitness+=1;
						}else
							fitness+= sfr.getFitness()/samples; 
					}
					result+=" "+(int)fitness;
				}
				result+="\n";
			}
		} catch(Exception e) {e.printStackTrace();}
		
		return result;
	}

	@Override
	public void submitTask(Task task) {
		taskExecutor.addTask(task);
	}

	@Override
	public Result getResult() {
		return taskExecutor.getResult();
	}

	@Override
	public DiskStorage getDiskStorage() {
		return null;
	}

	@Override
	public void prepareArguments(HashMap<String, Arguments> arguments) {
		taskExecutor.prepareArguments(arguments);
	}
}