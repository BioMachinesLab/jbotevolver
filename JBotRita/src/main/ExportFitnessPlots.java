
package main;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import simulation.Simulator;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class ExportFitnessPlots {

public static void main(String[] args) throws Exception{
		
		System.out.println("Type\tSetup\tRun\tGeneration\tHighestFitness\tAverageFitness\tLowestFitness");
		
		//String f = "rita/OFICIALPAPER_AA/";
		String f = "rita/SabsConference10Range30cm//";
		String[] setups = new String[]{f+"driving_Setup/",f+"jumping_Setup/",f+"jumpingAndDriving_Setup/"};
		
//		System.out.println("LENGTH"+setups.length);
		for(String s : setups)
			new ExportFitnessPlots(s);
	}
	
	public ExportFitnessPlots(String folder) throws Exception{
		
		File f = new File(folder);
		
		String result = "";
		
		for(String s : f.list()) {
			result+=checkSubFolders(new File(folder+s));
		}
		
		System.out.println(result);
	}
	
	private String checkSubFolders(File folder) throws Exception {
		
		StringBuffer result = new StringBuffer();
		
		if(folder.list() == null) {
			return result.toString();
		}
		
		for(String f : folder.list()) {
			
			String fn = folder.getPath()+"/"+f;
			
			if(f.equals("_fitness.log")) {
				result.append(getFitnessPlot(folder.getPath()));
			} else if(new File(fn).isDirectory()) {
				result.append(checkSubFolders(new File(fn)));
			}
		}
		
		return result.toString();
	}
	
	private String getFitnessPlot(String folder) throws Exception {
		
		StringBuffer result = new StringBuffer();
		
		if(!new File(folder+"/_fitness.log").exists())
			return result.toString();
		
		Scanner s = new Scanner(new File(folder+"/_fitness.log"));
		
		String prevLine = "";
		
		String[] split = folder.split("/");
		String prefix = split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1];
		
		while(s.hasNextLine()) {
			String line = s.nextLine().trim().replaceAll("\\s+", "\t");
			if(!line.startsWith("#") && !line.equals(prevLine) && !line.isEmpty()) {
				prevLine = line;
				result.append(prefix);
				result.append("\t");
				result.append(line);
				result.append("\n");
			}
		}
		
		s.close();
		
		return result.toString();
	}	
}