
package main;

import java.io.File;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import evaluationfunctions.flocking.metrics.Alignment;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class Check {
	
	//static int samples = 500;
	static int samples = 100;
	//static int samples = 1200;
	//static String prefix = "rita/SabsConferenceSensors8and4/";
	static String name="tese38-reynoldsRangeWithouAlig";
	
	static String prefix ="../JBotEvolver/rita//"+name+"/";
	//static String prefix = "experiments/SabsConference10Range30cm/";	
	//static String prefix = "experiments/";
	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		
		//String[] setups = new String[]{f+"allComponents/4/",f+"justCohesion/4/" };
		String[] setups = new String[]{f+"ReynoldsTrulyRange/1/",f+"ReynoldsTrulyRange/5/",f+"reynoldsPerceived/4/",f+"reynoldsPerceivedPriority/2/"};

		//String[] setups = new String[]{f+"flock2/"};
		//System.out.println("Type\tSetup\tRun\tSample\tTimeStep\tFitness");
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
		
		//System.out.println("GENERATION"+ jbot.getPopulation().getNumberOfGenerations());

			
		for(int i = 0 ; i < samples ; i++) {
			
		
			jbot.loadFile(filename, "--environment +fitnesssample="+4+"\n--random-seed "+i+"\n--evaluation +classname=NumberFragmentation");
			
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
			
			//jbot.getArguments()
			/*
			sim.setup();
			
			for (double time = Double.valueOf(1); time < 3000 && !sim.getStopSimulation(); time++) {
				sim.performOneSimulationStep(time);
				
				double res=eval[0].getCurrentFitness();
				System.out.println(split[split.length - 3]+"\t"+split[split.length - 2]+"\t"+split[split.length - 1]+"\t"+i+"\t"+time+"\t"+res);

		
			}
			sim.terminate();
			*/
			System.out.println(split[split.length - 3]+"\t"+split[split.length - 2]+"\t"+split[split.length - 1]+"\t"+i+"\t"+eval[0].getFitness());

			
		}
		return result;
	}
}