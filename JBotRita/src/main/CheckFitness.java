package main;

import java.io.File;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import evaluationfunctions.flocking.metrics.Alignment;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckFitness {
	static int fitnesssamples=4;

	//static int samples = 500;
	static int samples = 15;
	//static int samples = 1200;
	//static String prefix = "rita/SabsConferenceSensors8and4/";
	static String name="tese23_all";
	
	static String prefix ="../JBotEvolver/rita//"+name+"/";
	//static String prefix = "experiments/SabsConference10Range30cm/";	
	//static String prefix = "experiments/LatexDepoisResultados/";
	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		
		//String[] setups = new String[]{f+"coverageArea_belonging/1/",f+"driving_Setup_range25/1/",f+ "driving_Setup_range20/1/" };
		String[] setups = new String[]{f+"coverageArea_belonging/1/"};
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
		
		String name_conf=split[split.length-3];
		String setup=split[split.length-2];
		
		System.out.println("GENERATION"+ jbot.getPopulation().getNumberOfGenerations());

		for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {

			for(int i = 0 ; i < samples ; i++) {
			
		
			jbot.loadFile(filename, "--environment +fitnesssample="+fitnesssample+"\n--random-seed "+i+"\n--evaluation +classname=NumberOfGroups");
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			
			
					
			EvaluationFunction[] eval =jbot.getEvaluationFunction() ; //Antes era assim, ms com o update do JBotSim já não dá. Perguntar ao Miguel

			
			sim.addCallback(eval[0]);
			sim.simulate();
			
			
			double res = eval[0].getFitness();
			
			String line=split[split.length-2]+"\t"+split[split.length-1]+"\t"+fitnesssample+"\t"+i+"\t"+res+"\n";
			result+=line;
			System.out.print(line);
			}
		}
		return result;
	}
}