package utils;

import java.io.File;
import java.util.HashMap;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckFitness {
	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		String m = "_obstacle/";
		String[] setups = new String[]{f+"wheels"+m,f+"repertoire"+m,f+"multiple_intersection_repertoire"+m,f+"all_repertoire"+m};
		
		System.out.println("Type\tSetup\tRun\tRobot\tFitness");
		
		for(String s : setups)
			new CheckFitness(s);
	}
	
	private int count = 0;
	private double subtract = 10;
	private double threshold = 0;
	
	public CheckFitness(String folder) throws Exception{
		
		File f = new File(folder);
		
		String result = "";
		
		for(String s : f.list()) {
			result+=checkSubFolders(new File(folder+s));
		}
		
		System.out.println(result);
	}
	
	private String checkSubFolders(File folder) throws Exception {
		
		String result = "";
		
		if(folder.list() == null) {
			return result;
		}
		
		for(String f : folder.list()) {
			
			String fn = folder.getPath()+"/"+f;
			
			if(f.equals("_fitness.log")) {
				result+=checkFitness(folder.getPath());
			} else if(new File(fn).isDirectory()) {
				result+= checkSubFolders(new File(fn));
			}
		}
		return result;
	}
	
	private String checkFitness(String folder) throws Exception {
		
		String result = "";
		
		if(!new File(folder+"/_showbest_current.conf").exists())
			return result;
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		
		if(!jbot.getPopulation().evolutionDone())
			return result;
		
		String[] split = folder.split("/");
		
		int nFitnessSamples = jbot.getPopulation().getNumberOfSamplesPerChromosome();
		
		for(int i = 0 ; i < nFitnessSamples ; i++) {
		
			jbot.loadFile(folder+"/_showbest_current.conf", "--environment +fitnesssample="+i);
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = jbot.getEvaluationFunction()[0];
			sim.addCallback(eval);
			sim.simulate();
			
			double res = eval.getFitness();
			res-=subtract;
			
			Arguments actArgs = new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("actuators"));
			Arguments mWheel = new Arguments(actArgs.getArgumentAsString(actArgs.getArgumentAt(0)));
			Arguments wheel = new Arguments(mWheel.getArgumentAsString("wheels"));
			int nActs = actArgs.getNumberOfArguments();
			
			if(wheel.getNumberOfArguments() != 0)
				result+= split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+wheel.getArgumentAt(i%nActs)+"\t"+res+"\n";
			else
				result+= split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+actArgs.getArgumentAt(i%nActs)+"\t"+res+"\n";
		}
		
//		print();
	
		return result;
	}
	
	private void print() {
		if(count++ % 50 == 0)
			System.out.println();
		System.out.print(".");
	}

}
