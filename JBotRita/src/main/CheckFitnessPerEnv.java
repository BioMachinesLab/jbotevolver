package main;

import java.io.File;

import simulation.Simulator;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckFitnessPerEnv {
	static int samples = 500;
	//static String prefix = "rita/OFICIALPAPER_AA/";	
	static String prefix = "rita/LatexFinalEnv/";	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		String[] setups = new String[]{f+"Kanguru_10Neurons_oficial_exactTime_WithMoreSamples_PreyInt1/31/",f+"jumpingSumo_10Neurons_oficial_exactTime_WithMoreSamples_PreyInt1/8/",f+ "drivegRobot_10Neurons_oficial_exactTime_WithMoreSamples_PreyInt1/35/" };
//		String[] setups = new String[]{f+"all_repertoire"+m,f+"multiple_intersection_repertoire"+m};
		
		System.out.println("Type\tSetup\tRun\tSample\tFitness");
		
		for(String s : setups)
			checkFitness(prefix+s);
	}
	
	
	
	private static String checkFitness(String folder) throws Exception {
		
		String result = "";
		
		if(!new File(folder+"/_showbest_current.conf").exists())
			return result;
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		
		result+=runEvaluation(folder,jbot);
			
		return result;
	}
	
	private static String runEvaluation(String folder, JBotEvolver jbot) throws Exception{
		String result = "";
		String[] split = folder.split("/");
		
		String filename =  folder+"/_showbest_current.conf";
		
		for(int i = 0 ; i < samples ; i++) {
		
			jbot.loadFile(filename, "--environment +fitnesssample="+i+"\nrandom-seed "+i);
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = null;
			//ATENÇÃO ANTES ERA ASSIM: MS cm o update do JBotSim deixou de dar. depois perguntar ao Miguel.
			//EvaluationFunction eval = jbot.getEvaluationFunction() ;
			sim.addCallback(eval);
			sim.simulate();
			
			double res = eval.getFitness();
			
			String line= split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+i+"\t"+res+"\n";
			result+=line;
			System.out.print(line);
		}
		return result;
	}
}
