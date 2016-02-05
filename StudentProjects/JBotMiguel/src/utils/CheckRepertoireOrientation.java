package utils;

import java.io.File;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckRepertoireOrientation {
	
	public static void main(String[] args) throws Exception{
		
		String f = "repertoire/";
		
		System.out.println("Setup\tGeneration\tAverageFitness\tChromosomes");
		
		for(String s : new File(f).list()) {
			if(new File(f+s).isDirectory()) {
				new CheckRepertoireOrientation(f+s);
			}
		}
	}
	
	private int count = 0;
	
	public CheckRepertoireOrientation(String folder) throws Exception{
		
		File f = new File(folder);
		
		String result = "";
		
		for(String s : f.list()) {
			result=checkSubFolders(new File(folder+"/"+s+"/"));
			if(!result.isEmpty())
				System.out.println(result);
		}
	}
	
	private String checkSubFolders(File folder) throws Exception {
		
		String result = "";
		
		if(folder.list() == null) {
			return result;
		}
		
		for(String f : folder.list()) {
			
			String fn = folder.getPath()+"/"+f;
			
			if(f.equals("_fitness.log")) {
				result+=checkOrientation(folder.getPath()+"/");
			} else if(new File(fn).isDirectory()) {
				result+= checkSubFolders(new File(fn));
			}
		}
		return result;
	}
	
	private String checkOrientation(String folder) throws Exception {
		
		StringBuffer result = new StringBuffer();
		
		if(!new File(folder+"_showbest_current.conf").exists() || folder.contains("_20"))
			return result.toString();
		
		int gens = 0;
		
		Scanner s = new Scanner(new File(folder+"_generationnumber"));
		gens = s.nextInt();
		s.close();
		
		String[] split = folder.split("/");
		
		for(int i = 0 ; i <= gens ; i++) {
			JBotEvolver jbot = new JBotEvolver(new String[]{folder+"show_best/showbest"+i+".conf"});
			MAPElitesPopulation pop = (MAPElitesPopulation)jbot.getPopulation();
			result.append(split[split.length-2]);
			result.append("\t");
			result.append(i);
			result.append("\t");
			result.append(pop.getAverageFitness());
			result.append("\t");
			result.append(pop.getChromosomes().length+"\n");
		}
		
		return result.toString();
	}
	
	private void print() {
		if(count++ % 50 == 0)
			System.out.println();
		System.out.print(".");
	}

}
