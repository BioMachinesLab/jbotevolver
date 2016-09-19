package main;

import java.io.File;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckFitness {

	//static int samples = 500;
	static int samples = 500;
	//static String prefix = "rita/OFICIALPAPER_AA/";	
	static String prefix = "experiments/20160301/coevolution_patrolling2_tests160304_varrobots/";	

	private static boolean coevolution = false;

	public static void main(String[] args) throws Exception{

		String f = "";
		String[] setups = new String[]{f+"19/",f+"1/",f+ "12/"};
		//		String[] setups = new String[]{f+"all_repertoire"+m,f+"multiple_intersection_repertoire"+m};

		System.out.println("Type\tSetup\tRun\tSample\tFitness");

		for(String s : setups)
			checkFitness(prefix+s);
	}



	private static String checkFitness(String folder) throws Exception {

		String result = "";

		if(!new File(folder+"/_showbest_current.conf").exists()) { 
			if(!new File(folder+"/_ashowbest_current.conf").exists()){
				System.out.println("Doesnt exist!");
				return result;
			} else {
				coevolution =true;
			}
		}

		if(coevolution){
			
			JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_ashowbest_current.conf"}); 

			result+=runCoEvaluation(folder,jbot);
			
			return result;
			
		} else {

			JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"}); 

			result+=runEvaluation(folder,jbot);

			return result;
		}
	}

	private static String runEvaluation(String folder, JBotEvolver jbot) throws Exception{
		String result = "";
		String[] split = folder.split("/");

		String filename =  folder+"/_showbest_current.conf"; 

		for(int i = 0 ; i < samples ; i++) {

			//jbot.loadFile(filename, "--environment +fitnesssample="+i+"\n--random-seed "+i+"\n--evaluation +classname=FunctionEvalutionTime");
			jbot.loadFile(filename, "--environment +fitnesssample="+i+",wallswidth=100,wallsdistance=25"+"\n--random-seed "+i); // <------------- extra parameters go here !!!!!

			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = jbot.getEvaluationFunction()[0];
			sim.addCallback(eval);
			sim.simulate();

			double res = eval.getFitness();

			String line= split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+i+"\t"+res+"\n";
			result+=line;
			System.out.print(line);
		}
		return result;
	}
	
	private static String runCoEvaluation(String folder, JBotEvolver jbot) throws Exception{
		String result = "";
		String[] split = folder.split("/");

		String filename =  folder+"/_ashowbest_current.conf"; 

		for(int i = 0 ; i < samples ; i++) {

			//jbot.loadFile(filename, "--environment +fitnesssample="+i+"\n--random-seed "+i+"\n--evaluation +classname=FunctionEvalutionTime");
			jbot.loadFile(filename, "--environment +fitnesssample="+i+",wallswidth=100,wallsdistance=25"+"\n--random-seed "+i); //<-------------------- extra parameters go here !!!

			Simulator sim = jbot.createSimulator();
			jbot.setupBestCoIndividual(sim);
			EvaluationFunction evaluationFunctionA = jbot.getSpecificEvaluationFunction("a");
			sim.addCallback(evaluationFunctionA);
			EvaluationFunction evaluationFunctionB = jbot.getSpecificEvaluationFunction("b");
			sim.addCallback(evaluationFunctionB);
			sim.simulate();

			double[] res = {evaluationFunctionA.getFitness(), evaluationFunctionB.getFitness()};

			String line= split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+i+"\t"+res[0]+"\t"+res[1]+"\n";
			result+=line;
			System.out.print(line);
		}
		return result;
	}
}