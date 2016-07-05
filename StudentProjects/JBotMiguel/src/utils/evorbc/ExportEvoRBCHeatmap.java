package utils.evorbc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import simulation.Simulator;
import simulation.util.Arguments;
import utils.TraverseFolders;
import evolution.MAPElitesPopulation;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import fourwheeledrobot.MultipleWheelRepertoireActuator;
import fourwheeledrobot.MultipleWheelRepertoireNDActuator;

public class ExportEvoRBCHeatmap extends TraverseFolders{
	
	private static String FOLDER_NAME = "heatmap-export";
	
	private int samples = 50;
	private int maxRuns = 0;
	private int fitnessSamples = 5;
	private boolean realPoint = false;
	private boolean evorbcND = false;
	
	private boolean randomizeSeed = false;
	private boolean postEvalBest = false;
	
	private String fileName;
	private FileWriter fw;
	
	public ExportEvoRBCHeatmap(String baseFolder, String[] setups, String fileName) throws IOException {
		super(baseFolder, setups);
		
		new File(FOLDER_NAME).mkdir();
		new File(FOLDER_NAME+"/"+fileName).delete();
		fw = new FileWriter(new File(fileName));
		
		if(evorbcND)
			fw.append("Folder\tSetup\tRun\tSample\tMaze\tStep\tX\tY\tZ\n");
		else
			fw.append("Folder\tSetup\tRun\tSample\tMaze\tStep\tX\tY\n");
		
	}
	
	public static void main(String[] args) throws Exception{
		
		ExportEvoRBCHeatmap heatmap = null;
		boolean realPoint = false;
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/multimaze/", new String[]{"repertoire_obstacle/"}, "multimaze.txt");
		heatmap.realPoint = realPoint;
		heatmap.export(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/qualitymetrics/", new String[]{"maze_radial/","maze_distance/","maze_quality/"}, "qualitymetrics.txt");
		heatmap.realPoint = realPoint;
		heatmap.export(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/repertoiresize/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_30/","maze_quality_50/","maze_quality_100/"}, "repertoiresize.txt");
		heatmap.realPoint = realPoint;
		heatmap.export(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/repertoireresolution/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_50/","maze_quality_100/","maze_quality_200/"}, "repertoireresolution.txt");
		heatmap.realPoint = realPoint;
		heatmap.export(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/behaviormapping/", new String[]{"maze_type0_20/","maze_type1_20/","maze_type3_20/"}, "behaviormapping.txt");
		heatmap.realPoint = realPoint;
		heatmap.export(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/orientation/", new String[]{"maze_orientation/"}, "orientation.txt");
		heatmap.realPoint = realPoint;
		heatmap.evorbcND = true;
		heatmap.export(); System.out.println("Done "+heatmap.fileName);
	}
	
	public void export() throws Exception{
		traverse();
		fw.close();
	}
	
	protected void act(File folder) {
		
		try {
			for(String f : folder.list()) {
				if(f.equals("_fitness.log")) {
					checkFitness(folder.getPath());
					break;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void checkFitness(String folder) throws Exception {
		
		if(!new File(folder+"/_showbest_current.conf").exists())
			return;
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		
//		if(!jbot.getPopulation().evolutionDone())
//			return result;
		
		Arguments actArgs = new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("actuators"));
		Arguments mWheel = new Arguments(actArgs.getArgumentAsString(actArgs.getArgumentAt(0)));
		Arguments wheel = new Arguments(mWheel.getArgumentAsString("wheels"));
		int nActs = wheel.getNumberOfArguments();
		
		if(nActs == 0) {
			String actName = actArgs.getArgumentAt(0).split("=")[0];
			try {
				runEvaluation(folder,jbot,actName,0);
			} catch(Exception e){
				e.printStackTrace();
			}
		} else {
			for(int a = 0 ; a < nActs ; a++) {
				String actName = wheel.getArgumentAt(a).split("=")[0];
				try {
					runEvaluation(folder,jbot,actName,a);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	private MAPElitesPopulation getMapPop(JBotEvolver jbot, String folder) throws Exception {
		
		String filename = folder+"/_showbest_current.conf";
		jbot.loadFile(filename, "");
		
		Simulator sim = jbot.createSimulator();
		jbot.createRobots(sim);
		jbot.setupBestIndividual(sim);
		MultipleWheelRepertoireActuator actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
		String string = baseFolder+actuator.getRepertoireLocation();
		
		File f = new File(string);
		jbot.loadFile(f.getParent()+"/_showbest_current.conf", "");
		
		return (MAPElitesPopulation)jbot.getPopulation();
	}
	
	private void runEvaluation(String folder, JBotEvolver jbot, String act, int actNumber) throws Exception{
		StringBuffer result = new StringBuffer();
		String[] split = folder.split("/");
		
		String folderName = split[split.length-4];
		String setupName = split[split.length-2]+"_"+split[split.length-3];
		String runName = split[split.length-1];
		
		if(maxRuns > 0 && Integer.parseInt(runName) > maxRuns)
			return;
		
		int generation = 0;
		
		if(postEvalBest)
			generation = getBestGeneration(folder);
		
		MAPElitesPopulation mapPop = null;
		
		if(realPoint)
			mapPop = getMapPop(jbot,folder);
		
		String filename = postEvalBest ? folder+"/show_best/showbest"+generation+".conf" :  folder+"/_showbest_current.conf";
		for(int sample = 0 ; sample < samples ; sample++) {
			
			String randomizeSeed = this.randomizeSeed ? "\n--random-seed "+sample : "";
			randomizeSeed = randomizeSeed + (this.randomizeSeed ? "\n--simulator +fixedseed=0" : ""); 
			
			String options = "--environment +fitnesssample="+sample+randomizeSeed+"\n";
			options+="--robots +chosenactuator="+actNumber+"\n";
			options+="--simulator +folder=("+baseFolder+")\n";
		
			jbot.loadFile(filename, options);
			
			if(!postEvalBest && !jbot.getPopulation().evolutionDone())
				return;
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = jbot.getEvaluationFunction()[0];
			sim.addCallback(eval);
			
			MultipleWheelRepertoireActuator actuator = null;
			MultipleWheelRepertoireNDActuator actuatorND = null;
			
			if(evorbcND) {
				actuatorND = (MultipleWheelRepertoireNDActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireNDActuator.class);
			} else {
				actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
			}
			
			String bigLine = "";
			sim.setupEnvironment();
			
			for(double time = 0 ; time < sim.getEnvironment().getSteps() ; time++){
				sim.performOneSimulationStep(time);
				Vector2d point = actuator.getLastPoint();

				if(realPoint) {
					MOChromosome c = mapPop.getChromosomoeFromLocation(new int[]{(int)point.x,(int)point.y});
					int[] loc = mapPop.getLocationFromBehaviorVector(mapPop.getBehaviorVector(c));
					point = new Vector2d(loc[0],loc[1]);
				}
				
				String line= folderName+"\t"+setupName+"\t"+runName+"\t"+sample+"\t"+(sample % fitnessSamples)+"\t"+(int)time+"\t"+(int)point.x+"\t"+(int)point.y+"\n";
				
				if(evorbcND) {
					int[] points = actuatorND.getPrevBehavior();
					line= folderName+"\t"+setupName+"\t"+runName+"\t"+sample+"\t"+(sample % fitnessSamples)+"\t"+(int)time+"\t"+(int)points[0]+"\t"+(int)points[1]+"\t"+(int)points[2]+"\n";
				}
				
				line = line.replaceAll("_obstacle", "");
				line = line.replaceAll("_maze", "");
				line = line.replaceAll("_20", "");
				bigLine+= line;
			}
			
			if(generation == 0)
				generation = jbot.getPopulation().getNumberOfCurrentGeneration();
			
			result.append(bigLine);
		}
		fw.append(result.toString());
	}
	
	public static int getBestGeneration(String folder) throws Exception{
		File f = new File(folder);
		File parent = new File(f.getParent());
		
		File post = new File(parent.getPath()+"/post.txt");
		String[] split = folder.split("/");
		
		int line = Integer.parseInt(split[split.length-1]);
		
		Scanner s = new Scanner(post);
		
		for(int i = 0 ; i < line ; i++) {
			s.nextLine();
		}
		
		String result = s.nextLine();
		
		s.close();
		
		split = result.split(" ");
		
		return Integer.parseInt(split[split.length-1]);
	}
}