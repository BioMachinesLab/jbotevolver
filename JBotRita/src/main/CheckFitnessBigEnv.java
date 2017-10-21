package main;

import java.io.File;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckFitnessBigEnv {
	
	//static int samples = 500;
	//static int samples = 1200;
	static int samples = 100;
	//static String prefix = "rita/OFICIALPAPER_AA/";	
	static String prefix = "experiments/LatexDepoisResultados/";	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		String[] setups = new String[]{f+ "jumpingAndDriving_Setup/10/" };
//		String[] setups = new String[]{f+"all_repertoire"+m,f+"multiple_intersection_repertoire"+m};
		
		System.out.println("Type\tSetup\tRun\tSample\tFitness");
		
		for(String s : setups)
			checkFitness(prefix+s);
	}
	
	private static String checkFitness(String folder) throws Exception {
		
		String result = "";
		
		if(!new File(folder+"/_showbest_current.conf").exists()) {
			System.out.println("Doesnt exist!");
			return result;
		}
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		
		result+=runEvaluation(folder,jbot);
			
		return result;
	}
	
	private static String runEvaluation(String folder, JBotEvolver jbot) throws Exception{
		String result = "";
		String[] split = folder.split("/");
		
		String filename =  folder+"/_showbest_current.conf";
		
		for(int i = 0 ; i < samples ; i++) {
		
			//jbot.loadFile(filename, "--environment +fitnesssample="+i+"\n--random-seed "+i+"\n--evaluation +classname=FunctionEvalutionTime");
			//jbot.loadFile(filename, "--environment +fitnesssample="+i+"\nrandom-seed "+i);
			//System.out.println("--environment +fitnesssample="+i+"\nrandom-seed "+i + "\n--environment +classname=JS_LoopAfter5Envs");
			jbot.loadFile(filename, "--random-seed "+i + "\n--environment +classname=BigEnv2,fitnesssample="+i+"\n--evaluation +classname=FunctionEvalutionTime");
			System.out.println( "--random-seed "+i + "\n--environment +classname=BigEnv2,fitnesssample="+i+"\n--evaluation +classname=FunctionEvalutionTime");
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
	
	/*public static void main (String[] args ){
		double angle1=(0);
		double angle2=(Math.PI/2);
		double cos=Math.cos(angle1)+Math.cos(angle2); //2
		double sen=Math.sin(angle1)+Math.sin(angle2); //2
		double nei1= Math.pow(cos,2)+ Math.pow(sen,2);
		//double nei2= Math.pow(Math.cos(angle2),2)+Math.pow(Math.sin(angle2),2);
		System.out.println( Math.sqrt(nei1)/2);
	}*/
}