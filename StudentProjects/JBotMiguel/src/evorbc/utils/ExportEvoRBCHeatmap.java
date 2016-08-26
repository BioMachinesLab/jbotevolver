package evorbc.utils;

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
	
	private static String FOLDER_NAME = "export";
	
	private int samples = 25;//50
	private int maxRuns = 30;//30
	private int fitnessSamples = 5;
	private boolean realPoint = false;
	private boolean evorbcND = false;
	
	private boolean randomizeSeed = false;
	private boolean postEvalBest = false;
	
	private String fileName;
	private FileWriter fw;
	
	public ExportEvoRBCHeatmap(String baseFolder, String[] setups, String fileName) throws IOException {
		super(baseFolder, setups);
		this.fileName = fileName;
	}
	
	public static void main(String[] args) throws Exception{
		
		ExportEvoRBCHeatmap heatmap = null;
		/*
		heatmap = new ExportEvoRBCHeatmap("bigdisk/multimaze/", new String[]{"repertoire_obstacle/"}, "multimaze.txt");
		heatmap.realPoint = false;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/multimaze/", new String[]{"repertoire_obstacle/"}, "multimaze-real.txt");
		heatmap.realPoint = true;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/qualitymetrics/", new String[]{"maze_radial/","maze_distance/","maze_quality/"}, "qualitymetrics.txt");
		heatmap.realPoint = false;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/qualitymetrics/", new String[]{"maze_radial/","maze_distance/","maze_quality/"}, "qualitymetrics-real.txt");
		heatmap.realPoint = true;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/repertoiresize/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_30/","maze_quality_50/","maze_quality_100/"}, "repertoiresize.txt");
		heatmap.realPoint = false;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/repertoiresize/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_30/","maze_quality_50/","maze_quality_100/"}, "repertoiresize-real.txt");
		heatmap.realPoint = true;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/repertoireresolution/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_50/","maze_quality_100/","maze_quality_200/"}, "repertoireresolution.txt");
		heatmap.realPoint = false;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/repertoireresolution/", new String[]{"maze_quality_5/","maze_quality_10/","maze_quality_20/","maze_quality_50/","maze_quality_100/","maze_quality_200/"}, "repertoireresolution-real.txt");
		heatmap.realPoint = true;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/behaviormapping/", new String[]{"maze_type0_20/","maze_type1_20/","maze_type3_20/"}, "behaviormapping.txt");
		heatmap.realPoint = false;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		heatmap = new ExportEvoRBCHeatmap("bigdisk/behaviormapping/", new String[]{"maze_type0_20/","maze_type1_20/","maze_type3_20/"}, "behaviormapping-real.txt");
		heatmap.realPoint = true;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		

		*/
		heatmap = new ExportEvoRBCHeatmap("/Volumes/Orico/ec/miguel/orientation/", new String[]{"maze_orientation/"}, "orientation.txt");
		heatmap.realPoint = false;
		heatmap.evorbcND = true;
		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
		
		
//		heatmap = new ExportEvoRBCHeatmap("/Volumes/Orico/ec/miguel/orientation/", new String[]{"maze_orientation/"}, "orientation-real.txt");
//		heatmap.realPoint = true;
//		heatmap.evorbcND = true;
//		heatmap.traverse(); System.out.println("Done "+heatmap.fileName);
	}
	
	@Override
	protected void traverseStarted() {
		
		try {
		
			new File(FOLDER_NAME).mkdir();
			new File(FOLDER_NAME+"/heatmap-"+this.fileName).delete();
			fw = new FileWriter(new File(FOLDER_NAME+"/heatmap-"+this.fileName));
			
			if(evorbcND)
				fw.append("Folder Setup Run X Y Z Count\n");
			else
				fw.append("Folder Setup Run X Y Count\n");
		
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
	}
	
	@Override
	protected boolean actFilter(File folder) {
		
		if(new File(folder.getPath()+"/repertoire_name.txt").exists())
			return false;
		
		if(new File(folder.getPath()+"/_showbest_current.conf").exists())
			return true;
		
		return false;
	}
	
	protected void act(File folder) {
		
		try {
			exportHeatmap(folder.getPath());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void exportHeatmap(String folder) throws Exception {
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		
//		if(!jbot.getPopulation().evolutionDone())
//			return result;
		
		System.out.println(folder);
		
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
		jbot.loadFile(filename, "--simulator +folder="+baseFolder);
		
		Simulator sim = jbot.createSimulator();
		jbot.createRobots(sim);
		jbot.setupBestIndividual(sim);
		MultipleWheelRepertoireActuator actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
		String string = baseFolder+actuator.getRepertoireLocation();
		
		File f = new File(string);
		jbot.loadFile(f.getParent()+"/_showbest_current.conf", "--simulator +folder="+baseFolder);
		
		return (MAPElitesPopulation)jbot.getPopulation();
	}
	
	private void runEvaluation(String folder, JBotEvolver jbot, String act, int actNumber) throws Exception{
		
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
		
		if(realPoint && !evorbcND)
			mapPop = getMapPop(jbot,folder);
		
		String filename = postEvalBest ? folder+"/show_best/showbest"+generation+".conf" :  folder+"/_showbest_current.conf";
		
		int[][] count = null;
		int[][][] countND = null;
		
		for(int sample = 0 ; sample < samples ; sample++) {
			
			System.out.print(".");
			
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
			
			sim.setupEnvironment();
			
			MultipleWheelRepertoireNDActuator actuatorND = null;
			MultipleWheelRepertoireActuator actuator = null;
			
			if(evorbcND)
				actuatorND = (MultipleWheelRepertoireNDActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireNDActuator.class);
			else
				actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
			
			if(evorbcND && countND == null) {
				countND = new int[actuatorND.getMap().getBucketListing(0).length][actuatorND.getMap().getBucketListing(1).length][actuatorND.getMap().getBucketListing(2).length];
			} else if(!evorbcND && count == null) {
				count = new int[actuator.getRepertoire().length][actuator.getRepertoire()[0].length];
			}
			
			for(double time = 0 ; time < sim.getEnvironment().getSteps() ; time++){
				sim.performOneSimulationStep(time);
				
				if(evorbcND) {
					
					int[] points = actuatorND.getPrevBehavior();
					
					if(realPoint)
						points = actuatorND.getRealPrevBehavior();
					
					countND[(int)points[0]][(int)points[1]][(int)points[2]]++;
					
				} else {
					
					
					Vector2d point = actuator.getLastPoint();
					
					if(realPoint) {
						MOChromosome c = mapPop.getChromosomoeFromLocation(new int[]{(int)point.x,(int)point.y});
						int[] loc = mapPop.getLocationFromBehaviorVector(mapPop.getBehaviorVector(c));
						point = new Vector2d(loc[0],loc[1]);
					}
					count[(int)point.x][(int)point.y]++;
				}

			}
			
			if(generation == 0)
				generation = jbot.getPopulation().getNumberOfCurrentGeneration();
			
//			result.append(bigLine);
		}
		
		
		StringBuffer result = new StringBuffer();
		String info = folderName+" "+setupName+" "+runName;
		
		info = info.replaceAll("_obstacle", "");
		info = info.replaceAll("_maze", "");
		info = info.replaceAll("_20", "");
		
		if(evorbcND) {
			for(int x = 0 ; x < countND.length ; x++) {
				for(int y = 0 ; y < countND[0].length ; y++) {
					for(int z = 0 ; z < countND[0][0].length ; z++) {
						if(countND[x][y][z] != 0) {
							result.append(info);
							result.append(" "+x+" "+y+" "+z+" "+countND[x][y][z]+"\n");
						}
					}
				}
			}
		} else {
			for(int x = 0 ; x < count.length ; x++) {
				for(int y = 0 ; y < count[0].length ; y++) {
					if(count[x][y] != 0) {
						result.append(info);
						result.append(" "+x+" "+y+" "+count[x][y]+"\n");
					}
				}
			}
		}
		
		System.out.println();
		fw.append(result);
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