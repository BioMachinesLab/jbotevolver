package main;

import java.io.File;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckFitness {
	
	//static int samples = 500;
	static int samples = 15;
	//static int samples = 1200;
	//static String prefix = "rita/SabsConferenceSensors8and4/";
	static String prefix ="untitledFolder/FlockNavigationOtherEnv2/";
	//static String prefix = "experiments/SabsConference10Range30cm/";	
	//static String prefix = "experiments/LatexDepoisResultados/";
	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		
		String[] setups = new String[]{f+"driving_Setup20/3",f+"driving_Setup6/2/",f+ "driving_Setup12/2/" };
		//String[] setups = new String[]{f+"driving_Setup/11/"};
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
		
			jbot.loadFile(filename, "--environment +fitnesssample="+i+"\n--random-seed "+i+"\n--evaluation +classname=FlockAlignementMetric");
			//jbot.loadFile(filename, "--environment +fitnesssample="+i+"\nrandom-seed "+i);
			//System.out.println("--environment +fitnesssample="+i+"\nrandom-seed "+i + "\n--environment +classname=JS_LoopAfter5Envs");
			//jbot.loadFile(filename, "--random-seed "+i + "\n--environment +classname=JS_LoopAfter5Envs,fitnesssample="+i+"\n--evaluation +classname=FunctionEvalutionTime");
			//jbot.loadFile(filename, "--random-seed "+i + "\n--environment +classname=JS_LoopAfterWithAll,fitnesssample="+i+"\n--evaluation +classname=FunctionEvalutionTime");
			
			//System.out.println( "--random-seed "+i + "\n--environment +classname=JS_LoopAfter5Envs,fitnesssample="+i+"\n--evaluation +classname=FunctionEvalutionTime");
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			
			
			//EvaluationFunction eval =null;
					
			EvaluationFunction[] eval =jbot.getEvaluationFunction() ; //Antes era assim, ms com o update do JBotSim já não dá. Perguntar ao Miguel

			sim.addCallback(eval[0]);
			sim.simulate();
			
			double res = eval[0].getFitness();
			
			String line= split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+i+"\t"+res+"\n";
			result+=line;
			System.out.print(line);
		}
		return result;
	}
}