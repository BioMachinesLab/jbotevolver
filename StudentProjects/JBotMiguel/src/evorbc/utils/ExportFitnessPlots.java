package evorbc.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.Scanner;

import simulation.util.Arguments;
import utils.TraverseFolders;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evorbc.qualitymetrics.CircularQualityMetric;

public class ExportFitnessPlots extends TraverseFolders{
	
	private static String FOLDER_NAME = "export";
	
	
	private String fileName;
	private FileWriter fw;
	private static int MAX_RUN = 30;
	
	public ExportFitnessPlots(String baseFolder, String[] setups, String fileName) throws Exception{
		super(baseFolder, setups);
		this.fileName = fileName;
	}
	
	public ExportFitnessPlots(String baseFolder, String fileName) throws Exception{
		super(baseFolder);
		this.fileName = fileName;
	}
	
	public static void main(String[] args) throws Exception{
//		new ExportFitnessPlots("bigdisk/vsvanilla/", "fitness-vsvanilla.txt").traverse();
//		new ExportFitnessPlots("bigdisk/behaviormapping/", "fitness-bm.txt").traverse();
//		new ExportFitnessPlots("bigdisk/qualitymetrics/", "fitness-qm.txt").traverse();
//		new ExportFitnessPlots("bigdisk/binsize/", "fitness-binsize.txt").traverse();
//		new ExportFitnessPlots("bigdisk/time/", "fitness-time.txt").traverse();
		new ExportFitnessPlots("bigdisk2/", "fitness-w1.txt").traverse();
		new ExportFitnessPlots("bigdisk/", "fitness-w2.txt").traverse();
	}
	
	@Override
	protected void traverseStarted() {
		System.out.println(baseFolder+" started!");
		try {
			new File(FOLDER_NAME).mkdir();
			new File(FOLDER_NAME+"/"+fileName).delete();
			fw = new FileWriter(new File(FOLDER_NAME+"/"+fileName));
			fw.append("Method\tMapping\tQuality\tTime\tBinSize\tRobot\tRun\tGeneration\tHighestFitness\tAverageFitness\tLowestFitness\n");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void traverseEnded() {
		try {
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(baseFolder+" ended!");
	}
	
	@Override
	protected boolean actFilter(File folder) {
		
		if(new File(folder.getPath()+"/repertoire_name.txt").exists())
			return false;
		
		if(new File(folder.getPath()+"/_fitness.log").exists()) {
			
			if(MAX_RUN > 0) {
				String[] split = folder.getPath().split("/");
				int run = Integer.parseInt(split[split.length-1]);
				if(run <= MAX_RUN)
					return true;
				else
					return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void act(File folder) {
		
		try {
			fw.append(getFitnessPlot(folder.getPath()));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getFitnessPlot(String folder) throws Exception {
		
		System.out.println(folder);
		
		String[] split = folder.split("/");
		
		String baseFolder = "";
		
		for(int i = 0 ; i < split.length-3 ; i++ ) {
			baseFolder+=split[i]+"/";
		}
		
		String mapping = "0";
		String time = "0";
		String quality = "0";
		String binsize = "0";
		String robot = "";
		String method = "";
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf","--simulator","+folder="+baseFolder});
		Serializable obj = jbot.getSerializableObjectHashMap().get("repertoirepath");
		
		String actuatorType = new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("actuators")).getArgumentAt(0);
		
		if(actuatorType.equals("MultipleWheelRepertoireActuator")) {
			method = "EvoRBC";
			String repertoirePath = (String)obj;
			JBotEvolver repertoireEvo = new JBotEvolver(new String[]{repertoirePath,"--init","skip=1"});
			MAPElitesPopulation pop = (MAPElitesPopulation)repertoireEvo.getPopulation();
			
			EvaluationFunction[] functions = repertoireEvo.getEvaluationFunction();
			quality = functions[0].getClass().getSimpleName().replace("QualityMetric", "");
			if(functions[0].getArgs().getFlagIsTrue("distance"))
				quality+="+Distance";
			
			binsize = pop.getMapResolution()*100+"";
			
			time = (repertoireEvo.getArguments().get("--environment").getArgumentAsDouble("steps")/10.0)+"";
			
			Arguments wheelArgs = new Arguments(new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("actuators")).getArgumentAsString("MultipleWheelRepertoireActuator"));
			mapping = new Arguments(wheelArgs.getArgumentAsString("mapping")).getArgumentAsString("classname").split("\\.")[2].replace("MappingFunction", "").replace("180", "");
			robot = new Arguments(wheelArgs.getArgumentAsString("wheels")).getArgumentAt(0).replace("Actuator", "").replace("_", "-").replace("A", "4").replace("F", "2");
		} else {
			method = "Vanilla";
			robot = new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("actuators")).getArgumentAt(0).replace("Actuator", "").replace("_", "-").replace("A", "4").replace("F", "2");
		}
		
		String runNumber = split[split.length-1];
		
		String prefix = method+"\t"+
				mapping+"\t"+
				quality+"\t"+
				time+"\t"+
				binsize+"\t"+
				robot+"\t"+
				runNumber;
		
		Scanner s = new Scanner(new File(folder+"/_fitness.log"));
		String prevLine = "";
		
		StringBuffer result = new StringBuffer();
		
		while(s.hasNextLine()) {
			String line = s.nextLine().trim().replaceAll("\\s+", "\t");
			
			String newLine = prefix+"\t"+line;
			
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