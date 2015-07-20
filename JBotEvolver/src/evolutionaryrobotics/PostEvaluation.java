package evolutionaryrobotics;

import java.io.File;

import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.results.PostEvaluationResult;
import taskexecutor.tasks.SingleSamplePostEvaluationTask;

public class PostEvaluation {
	
	private int startTrial = 0;
	private int maxTrial = 0;
	private int samples = 100;
	private int fitnesssamples = 1;
	private int steps = 0;
	private double targetfitness = 0;
	private String dir = "";
	
	private boolean singleEvaluation = false;
	private boolean localEvaluation = false;
	
	private TaskExecutor taskExecutor;
	private String[] args;
	
	public PostEvaluation(String[] args, String[] extraArgs) {
		this(args);
		this.args = extraArgs;
	}
	
	public PostEvaluation(String[] args) {
		
		for(String s : args) {
			String[] a = s.split("=");
			if(a[0].equals("dir")) dir = a[1];
			if(a[0].equals("samples")) samples = Integer.parseInt(a[1]);
			if(a[0].equals("fitnesssamples")) fitnesssamples = Integer.parseInt(a[1]);
			if(a[0].equals("targetfitness")) targetfitness = Double.parseDouble(a[1]);
			if(a[0].equals("singleevaluation")) singleEvaluation = Integer.parseInt(a[1]) == 1;
			if(a[0].equals("localevaluation")) localEvaluation = Integer.parseInt(a[1]) == 1;
			if(a[0].equals("steps")) steps = Integer.parseInt(a[1]);
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
	
	public double[][] runPostEval() {

		double[][] result = new double[maxTrial][fitnesssamples];
		
		try {
		
			String file = "";
			
			if(singleEvaluation)
				file=dir+"/_showbest_current.conf";
			else
				file=dir+startTrial+"/_showbest_current.conf";
			
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
			
			taskExecutor.setTotalNumberOfTasks((maxTrial - startTrial + 1)*fitnesssamples*samples);
		
			for(int i = startTrial ; i <= maxTrial ; i++) {
				if(singleEvaluation)
					file = dir+"_showbest_current.conf";
				else
					file = dir+i+"/_showbest_current.conf";
				newArgs[0] = file;
				jBotEvolver = new JBotEvolver(newArgs);

				for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
					for(int sample = 0 ; sample < samples ; sample++) {
						JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed());
						SingleSamplePostEvaluationTask t = new SingleSamplePostEvaluationTask(i,newJBot,fitnesssample,newJBot.getPopulation().getBestChromosome(),sample,targetfitness);
						taskExecutor.addTask(t);
						System.out.print(".");
					}
					System.out.println();
				}
				System.out.println();
			}
			System.out.println();
			for(int i = startTrial ; i <= maxTrial ; i++) {
				for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
					for(int sample = 0 ; sample < samples ; sample++) {
						PostEvaluationResult sfr = (PostEvaluationResult)taskExecutor.getResult();
						System.out.println(sfr.getRun()+" "+sfr.getFitnesssample()+" "+sfr.getSample()+" "+sfr.getFitness());
						result[sfr.getRun()-1][sfr.getFitnesssample()]+= sfr.getFitness()/samples;
					}
				}
			}
				
			taskExecutor.stopTasks();
			
		} catch(Exception e) {e.printStackTrace();}
		
		return result;
	}
	
	public double getAverageFitness(double[][] vals) {
		double avg = 0;
		
		for(int i = 0 ; i < vals.length ; i++) {
			avg+=vals[i][0];
		}
		
		return avg/vals.length;
	}
}