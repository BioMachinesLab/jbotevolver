package utils.evorbc;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import utils.TraverseFolders;

public class ExportFitnessPlots extends TraverseFolders{
	
	private static String FOLDER_NAME = "fitness-export";
	
	private String fileName;
	private FileWriter fw;
	
	public ExportFitnessPlots(String baseFolder, String[] setups, String fileName) throws Exception{
		super(baseFolder, setups);
		
		this.fileName = fileName;
		
		new File(FOLDER_NAME).mkdir();
		new File(FOLDER_NAME+"/"+fileName).delete();
		fw = new FileWriter(new File(this.fileName));
	}
	
	public void export() throws Exception{
		fw.append("Setup\tRun\tGeneration\tHighestFitness\tAverageFitness\tLowestFitness\n");
		traverse();
		fw.close();
	}
	
	@Override
	protected void act(File folder) {
		try {
			for(String f : folder.list()) {
				if(f.equals("_fitness.log")) {
					fw.append(getFitnessPlot(folder.getPath()));
					break;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		new ExportFitnessPlots("bigdisk/multimaze/", new String[]{"repertoire_obstacle/"}, "multimaze.txt").export();
		new ExportFitnessPlots("bigdisk/qualitymetrics/", new String[]{"maze_radial/","maze_distance/","maze_quality/"}, "qualitymetrics.txt").export();
		new ExportFitnessPlots("bigdisk/repertoiresize/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_30/","maze_quality_50/","maze_quality_100/"}, "repertoiresize.txt").export();
		new ExportFitnessPlots("bigdisk/repertoireresolution/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_50/","maze_quality_100/","maze_quality_200/"} , "repertoireresolution.txt").export();
		new ExportFitnessPlots("bigdisk/behaviormapping/", new String[]{"maze_type0_20/","maze_type1_20/","maze_type3_20/"}, "behaviormapping.txt").export();
		new ExportFitnessPlots("bigdisk/orientation/", new String[]{"maze_orientation/"}, "orientation.txt").export();
		new ExportFitnessPlots("bigdisk/ann/", new String[]{"maze_ann/"}, "ann.txt").export();
	}
	
	private String getFitnessPlot(String folder) throws Exception {
		
		StringBuffer result = new StringBuffer();
		
		if(!new File(folder+"/_fitness.log").exists())
			return result.toString();
		
		Scanner s = new Scanner(new File(folder+"/_fitness.log"));
		
		String prevLine = "";
		
		String[] split = folder.split("/");
		String prefix = split[split.length-3]+"_"+split[split.length-2]+"\t"+split[split.length-1];
		
		while(s.hasNextLine()) {
			String line = s.nextLine().trim().replaceAll("\\s+", "\t");
			
			String newLine = prefix+"\t"+line;
			newLine = newLine.replaceAll("Actuator", "");
			newLine = newLine.replaceAll("_maze", "");
			
			if(!line.startsWith("#") && !line.equals(prevLine) && !line.isEmpty()) {
				result.append(newLine);
				result.append("\n");
				prevLine = line;
			}
		}
		
		s.close();
		
		return result.toString();
	}	
}