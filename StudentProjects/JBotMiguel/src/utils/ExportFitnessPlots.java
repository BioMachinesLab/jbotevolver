package utils;

import java.io.File;
import java.util.Scanner;

public class ExportFitnessPlots {
	
	public static void main(String[] args) throws Exception{
		
		System.out.println("Type\tSetup\tRun\tGeneration\tHighestFitness\tAverageFitness\tLowestFitness");
		
//		String f = "bigdisk/december2015/10samples/";String m = "_obstacle/";
//		String f = "bigdisk/december2015/foraging/";String m = "_foraging/";
		String f = "bigdisk/locking/";String m = "";//"_obstacle/";
		
//		String[] setups = new String[]{f+"wheels"+m,f+"repertoire"+m,f+"all_repertoire"+m+"all_30"+m};
		String[] setups = new String[]{f};
		
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
			line = line.replaceAll("_30_", "_");
			line = line.replaceAll("_foraging", "");
			line = line.replaceAll("_obstacle", "");
			if(!line.startsWith("#") && !line.equals(prevLine) && !line.isEmpty()) {
				prevLine = line;
				result.append(prefix);
				result.append("\t");
				result.append(line.replaceAll("_30", "").replaceAll("_foraging", "").replaceAll("_obstacle", ""));
				result.append("\n");
			}
		}
		
		s.close();
		
		return result.toString();
	}	
}