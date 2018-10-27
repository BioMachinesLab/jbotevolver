package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import taskexecutor.tasks.SingleSamplePostEvaluationTask;
import evaluationfunctions.flocking.metrics.Alignment;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class PosMetricsAll {
	
	//static int samples = 500;
	static int samples = 100;
	static int fitnesssamples=4;
	static String[] classnames = {"Alignment","NG_OneDividedByGroups","NG_PercentageOfFragmentation", "NumberOfGroupsAsNumber"};
	//static int samples = 1200;
	//static String prefix = "rita/SabsConferenceSensors8and4/";
	static String name="tese44_dividedbyGroupsWithAlig";
	
	static String prefix ="../JBotEvolver/rita//"+name+"/";
	//static String prefix = "experiments/SabsConference10Range30cm/";	
	//static String prefix = "experiments/";
	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		
		//String[] setups = new String[]{f+"allComponents/4/",f+"justCohesion/4/" };
		String[] setups = new String[]{f+"5robots/9/"};
				//,f+"ReynoldsTrulyRange/5/",f+"reynoldsPerceived/4/",f+"reynoldsPerceivedPriority/2/"};

		//String[] setups = new String[]{f+"flock2/"};
		System.out.println("Setup\tRun\tFitnessSample\tSample\tTimeStep\tFitness");
		//System.out.println("Setup\tRun\tFtinessSample\tSample\tFitness");

		for(int i=0; i<classnames.length; i++){
			System.out.println("vou comerçar nova classname "+ classnames[i]);
			for(String s : setups)
				checkFitness(prefix+s, classnames[i]);
		}
			
	}
	
	
	
	private static String checkFitness(String folder, String classname) throws Exception {
		
		String result = "";
		
		if(!new File(folder+"/_showbest_current.conf").exists()) {
			System.out.println("Doesnt exist!");
			return result;
		}
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		
		result+=runEvaluation(folder,jbot, classname);
			
		return result;
	}
	
	private static String runEvaluation(String folder, JBotEvolver jbot, String classname) throws Exception{
		String result = "Setup\tRun\tFitnessSample\tSample\tTimeStep\tFitness\n";

		String[] split = folder.split("/");
		
		
		String filename =  folder+"/_showbest_current.conf";
		
		String name_conf=split[split.length-3];
		String setup=split[split.length-2];
		
		//System.out.println("GENERATION"+ jbot.getPopulation().getNumberOfGenerations());
		
		String folderName="../JBotEvolver/rita//"+name+"/"+split[split.length - 2]+"/posMetrics2/";
		
		File f = new File(folderName);

		if (!f.exists())
			f.mkdir();

		f = new File(folderName + "/" + classname+".txt");
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));

		bw.write("Setup\tRun\tFitnessSample\tSample\tTimeStep\tFitness\n");
		
		for(int fitnesssample = 0 ; fitnesssample < fitnesssamples ; fitnesssample++) {
			
			for(int i = 0 ; i < samples ; i++) {
			
				// for fotness sample
				jbot.loadFile(filename, "--environment +fitnesssample=" + fitnesssample+ "\n--random-seed " + i + "\n--evaluation +classname="+classname);

				Simulator sim = jbot.createSimulator();
				jbot.createRobots(sim);
				jbot.setupBestIndividual(sim);
				
			
				
				//EvaluationFunction eval =null;
						
				EvaluationFunction[] eval =jbot.getEvaluationFunction() ; //Antes era assim, ms com o update do JBotSim já não dá. Perguntar ao Miguel
	
				
				sim.addCallback(eval[0]);
				//sim.simulate();
				
				//jbot.getArguments()
				
				sim.setup();
				
				for (double time = Double.valueOf(0); time < 3000 && !sim.getStopSimulation(); time++) {
					sim.performOneSimulationStep(time);
					
					//double res=eval[0].getCurrentFitness();
					
					String line=split[split.length - 2]+"\t"+split[split.length - 1]+"\t"+fitnesssample+"\t"+i+"\t"+time+"\t"+eval[0].getCurrentFitness()+"\n";
					bw.write(line);
					
					//result+=line;

			
				}
				sim.terminate();
				
	
				
			}
			System.out.println("fitnesssample"+fitnesssample);
			
		}
		//createFile("../JBotEvolver/rita//"+name+"/"+split[split.length - 2]+"/posMetrics/", classname+".txt", result);
		System.out.println("end");
		bw.close();
		return result;
	}
	
	protected static void createFile(String folderName, String fileName, String contents) throws Exception {
		File f = new File(folderName);

		if (!f.exists())
			f.mkdir();

		f = new File(folderName + "/" + fileName);

		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		bw.write(contents);
		bw.flush();
		bw.close();
	}
}