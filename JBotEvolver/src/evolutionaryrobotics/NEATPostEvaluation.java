package evolutionaryrobotics;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import simulation.util.Arguments;
import taskexecutor.ConillonTaskExecutor;
import taskexecutor.TaskExecutor;
import taskexecutor.results.NEATPostEvaluationResult;
import taskexecutor.tasks.NEATMultipleSamplePostEvaluationTask;
import taskexecutor.tasks.NEATSingleSamplePostEvaluationTask;

public class NEATPostEvaluation {
	
	private int startTrial = 0;
	private int maxTrial = 0;
	private int samples = 100;
	private int sampleIncrement = 100;
	private int fitnesssamples = 1;
	private int steps = 0;
	private double targetfitness = 0;
	private String dir = "";
	
	private boolean singleEvaluation = false;
	private boolean localEvaluation = false;
	
	private TaskExecutor taskExecutor;
	private String[] args;
	
	private boolean showOutput = false;
	
	public NEATPostEvaluation(String[] args, String[] extraArgs) {
		this(args);
		this.args = extraArgs;
	}
	
	public NEATPostEvaluation(String[] args) {
		for(String s : args) {
			String[] a = s.split("=");
			if(a[0].equals("dir")) dir = a[1];
			if(a[0].equals("samples")) samples = Integer.parseInt(a[1]);
			if(a[0].equals("fitnesssamples")) fitnesssamples = Integer.parseInt(a[1]);
			if(a[0].equals("targetfitness")) targetfitness = Double.parseDouble(a[1]);
			if(a[0].equals("singleevaluation")) singleEvaluation = Integer.parseInt(a[1]) == 1;
			if(a[0].equals("localevaluation")) localEvaluation = Integer.parseInt(a[1]) == 1;
			if(a[0].equals("steps")) steps = Integer.parseInt(a[1]);
			if(a[0].equals("showoutput")) showOutput = (Integer.parseInt(a[1]) == 1);
			if(a[0].equals("sampleincrement")) sampleIncrement = Integer.parseInt(a[1]);
		}
		
		if(steps != 0) {
			this.args = new String[]{"--environment","+steps="+steps,"--evaluation","+posteval=1"};
		}else {
			this.args = new String[]{"--evaluation","+posteval=1"};
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
		
		if(singleEvaluation)
			maxTrial = 1;
	}
	
	
	public double[][][] runPostEval() {
		double[][][] result = null;
		
		try{
			String file = "";
			int generationNumber = 0;
			
			if(singleEvaluation){
				file=dir+"/_showbest_current.conf";
				generationNumber = getGenerationNumberFromFile(dir+"/_generationnumber");
			}else{
				file=dir+startTrial+"/_showbest_current.conf";
				generationNumber = getGenerationNumberFromFile(dir+startTrial+"/_generationnumber");
			}
			
			result = new double[maxTrial][generationNumber][fitnesssamples];
			
			String[] newArgs = args != null ? new String[args.length+1] : new String[1];
			
			newArgs[0] = file;
			
			for(int i = 1 ; i < newArgs.length ; i++)
				newArgs[i] = args[i-1];
				
			JBotEvolver jBotEvolver = new JBotEvolver(newArgs);
			
			if (jBotEvolver.getArguments().get("--executor") != null) {
				if(localEvaluation)
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, new Arguments("classname=ParallelTaskExecutor",true));
				else
					taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
				taskExecutor.start();
			}
			
			boolean setNumberOfTasks = false;
			int totalTasks = 0;
			
			for(int i = startTrial ; i <= maxTrial ; i++) {
				if(singleEvaluation)
					file = dir+"/show_best/";
				else
					file = dir+i+"/show_best/";
				
				File directory = new File(file);
				
				int numberOfGenerations = directory.listFiles().length;
				
				if(!setNumberOfTasks) {
					totalTasks = (maxTrial - startTrial + 1)*fitnesssamples*samples*numberOfGenerations/sampleIncrement;
					taskExecutor.setTotalNumberOfTasks(totalTasks);
					setNumberOfTasks = true;
				}
				
				
				File[] files = directory.listFiles();
				sortByNumber(files);
				
				for (File f : files) {
					int generation = Integer.valueOf(f.getName().substring(8, f.getName().indexOf(".")));

					newArgs[0] = file + f.getName();
					jBotEvolver = new JBotEvolver(newArgs);
					
					for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
						
						for(int sample = 0 ; sample < samples ; sample+=sampleIncrement) {
							JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed());
							NEATMultipleSamplePostEvaluationTask t = new NEATMultipleSamplePostEvaluationTask(i,generation,newJBot,fitnesssample,newJBot.getPopulation().getBestChromosome(),sample,sample+sampleIncrement,targetfitness);
							taskExecutor.addTask(t);
							if(showOutput)
								System.out.print(".");
						}
						if(showOutput)
							System.out.println();
					}
					
					taskExecutor.setDescription(dir+i+"/"+(generation+1)+" out of "+numberOfGenerations+" (total tasks: "+totalTasks+")");
					
					if(showOutput)
						System.out.println();
				}
				
				for(int gen = 0 ; gen < numberOfGenerations ; gen++) {
				
					for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
						
						for(int sample = 0 ; sample < samples ; sample+=sampleIncrement) {
							
							NEATPostEvaluationResult sfr = (NEATPostEvaluationResult)taskExecutor.getResult();
							result[sfr.getRun()-1][sfr.getGeneration()][sfr.getFitnesssample()]+= sfr.getFitness()*(double)sampleIncrement/(double)samples;
							if(showOutput)
								System.out.println(sfr.getRun()+" "+sfr.getGeneration()+" "+sfr.getFitnesssample()+" "+sfr.getSample()+" "+sfr.getFitness());
						}
					}
				}
			}
			
			if(showOutput)
				System.out.println();
			
			/*
			for(int i = startTrial ; i <= maxTrial ; i++) {
				
				if(singleEvaluation)
					file = dir+"/show_best/";
				else
					file = dir+i+"/show_best/";
				
				File directory = new File(file);
				
				for (int j = 0; j < directory.listFiles().length; j++) {
					for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
						for(int sample = 0 ; sample < samples ; sample++) {
							NEATPostEvaluationResult sfr = (NEATPostEvaluationResult)taskExecutor.getResult();
							if(showOutput)
								System.out.println(sfr.getRun()+" "+sfr.getGeneration()+" "+sfr.getFitnesssample()+" "+sfr.getSample()+" "+sfr.getFitness());
							result[sfr.getRun()-1][sfr.getFitnesssample()][sfr.getGeneration()]+= sfr.getFitness()/samples;
						}
					}
				}
				
			}*/
				
			taskExecutor.stopTasks();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return result;
	}
	
	 public void sortByNumber(File[] files) {
	        Arrays.sort(files, new Comparator<File>() {
	            @Override
	            public int compare(File o1, File o2) {
	                int n1 = extractNumber(o1.getName());
	                int n2 = extractNumber(o2.getName());
	                return n1 - n2;
	            }

	            private int extractNumber(String name) {
	                int i = 0;
	                name = name.replace("showbest", "");
	                name = name.replace(".conf", "");
	                try {
	                    i = Integer.parseInt(name);
	                } catch(Exception e) {
	                    i = 0; // if filename does not match the format
	                           // then default to 0
	                }
	                return i;
	            }
	        });

	    }
	
	private int getGenerationNumberFromFile(String file) {
		Scanner s = null;
		try {
			 s = new Scanner(new File(file));
			
			 return Integer.valueOf(s.next()) + 1;
			 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}finally{
			if(s != null)
				s.close();
		}
		return 0;
	}


}
