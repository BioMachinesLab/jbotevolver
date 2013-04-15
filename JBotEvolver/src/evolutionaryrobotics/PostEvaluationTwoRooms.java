package evolutionaryrobotics;

import java.io.File;

import taskexecutor.SequentialTaskExecutor;
import taskexecutor.TaskExecutor;
import taskexecutor.results.PostEvaluationResult;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.PostEvaluationTask;

public class PostEvaluationTwoRooms {
	
	private int startTrial = 0;
	private int maxTrial = 0;
	private int samples = 100;
	private int fitnesssamples = 1;
	private double targetfitness = 0;
	private String dir = "";
	
	private TaskExecutor taskExecutor;
	private String args ="";
	
	public PostEvaluationTwoRooms(String[] args) {
		
//		for(String s : args) {
//			String[] a = s.split("=");
//			if(a[0].equals("dir")) dir = a[1];
//			if(a[0].equals("samples")) samples = Integer.parseInt(a[1]);
//			if(a[0].equals("fitnesssamples")) fitnesssamples = Integer.parseInt(a[1]);
//			if(a[0].equals("targetfitness")) targetfitness = Double.parseDouble(a[1]);
//		}
//		
//		if(!dir.endsWith("/"))
//			dir+="/";
//		
//		File subFolder;
//		int currentFolder = 0;
//		do {
//			currentFolder++;
//			subFolder = new File(dir+currentFolder);
//		}while(subFolder.exists());
//		
//		startTrial = 1;
//		maxTrial = --currentFolder;
	}
	
	public double[][] runPostEval() {

		double[][] result = new double[maxTrial][fitnesssamples];
		
		try {
		
			String file = "bigdisk/miguel/newconillon/two_rooms_lag/main_arbitrator_5cm/4/_showbest_current.conf";
			JBotEvolver jBotEvolver = new JBotEvolver(new String[]{file,"--environment","+posteval=1,postevaltime=3000,steps=3000,splitratio=0,randomizesplitratio=0,timebetweenpreys=150"});
			
			if (jBotEvolver.getArguments().get("--executor") != null) {
//				taskExecutor = TaskExecutor.getTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
				taskExecutor = new SequentialTaskExecutor(jBotEvolver, jBotEvolver.getArguments().get("--executor"));
				taskExecutor.prepareArguments(jBotEvolver.getArguments());
				taskExecutor.start();
			}
		
			jBotEvolver = new JBotEvolver(new String[]{file,"--environment","+posteval=1,postevaltime=3000,steps=3000,splitratio=0.5,randomizesplitratio=0,timebetweenpreys=150"});
			
//			int avg = 0;
			
//			for(int i = 0 ; i < 100 ; i++) {
				taskExecutor.prepareArguments(jBotEvolver.getArguments());
				
				JBotEvolver newJBot = new JBotEvolver(jBotEvolver.getArgumentsCopy(),jBotEvolver.getRandomSeed());
				PostEvaluationTask t = new PostEvaluationTask(0,newJBot,0,newJBot.getPopulation().getBestChromosome(),100,0);
				taskExecutor.addTask(t);
				
				PostEvaluationResult sfr = (PostEvaluationResult)taskExecutor.getResult();
				System.out.println(sfr.getFitness());
//				avg+=sfr.getFitness();
//			}
//			System.out.println("Avg: "+(avg/100));
			
//			for(int i = startTrial ; i <= maxTrial ; i++) {
//				for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
			
//					result[sfr.getRun()-1][sfr.getFitnesssample()] = sfr.getFitness();
//				}
//			}
				
			taskExecutor.stopTasks();
			System.exit(0);
			
		} catch(Exception e) {e.printStackTrace();}
		
		return result;
	}
	
	public static void main(String[] args) {
		new PostEvaluationTwoRooms(new String[]{""}).runPostEval();
	}
}