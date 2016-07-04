package utils.evorbc;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import fourwheeledrobot.MultipleWheelRepertoireActuator;
import fourwheeledrobot.MultipleWheelRepertoireNDActuator;

public class ExportEvoRBCHeatmap {
	
	int samples = 50;
	int fitnessSamples = 5;
	boolean randomizeSeed = false;
	boolean postEvalBest = false;
	static String f = "bigdisk/evorbc2/qualitymetrics/";
	static FileWriter fw;
	
	public static void main(String[] args) throws Exception{
		
//		f = "bigdisk/evorbc2/multimaze/"; String[] setups = new String[]{f+"repertoire_obstacle/"};
//		f = "bigdisk/evorbc2/qualitymetrics/"; String[] setups = new String[]{f+"maze_radial/",f+"maze_distance/",f+"maze_quality/"};
//		f = "bigdisk/evorbc2/repertoiresize/"; String[] setups = new String[]{f+"maze_quality_5/",f+"maze_quality_10/",f+"maze_quality_20/",f+"maze_quality_30/",f+"maze_quality_50/",f+"maze_quality_100/"};
		f = "bigdisk/repertoireresolution/"; String[] setups = new String[]{f+"maze_quality_5/",f+"maze_quality_10/",f+"maze_quality_20/",f+"maze_quality_50/",f+"maze_quality_100/",f+"maze_quality_200/"};
//		f = "bigdisk/behaviormapping/"; String[] setups = new String[]{f+"maze_type0_20/",f+"maze_type1_20/",f+"maze_type3_20/"};
//		f = "bigdisk/orientation/"; String[] setups = new String[]{f+"maze_orientation/"};

		fw = new FileWriter(new File("out.txt"));
		
		fw.append("Folder\tSetup\tRun\tSample\tMaze\tStep\tX\tY\n");
//		fw.append("Folder\tSetup\tRun\tSample\tMaze\tStep\tX\tY\tZ\n");
		
		for(String s : setups)
			new ExportEvoRBCHeatmap(s);
		
		fw.close();
	}
	
	public ExportEvoRBCHeatmap(String folder) throws Exception{
		
		File f = new File(folder);
		
		for(String s : f.list()) {
			checkSubFolders(new File(folder+s));
		}
		
//		System.out.println(result);
	}
	
	private void checkSubFolders(File folder) throws Exception {
		
		if(folder.list() == null) {
			return;
		}
		
		for(String f : folder.list()) {
			
			String fn = folder.getPath()+"/"+f;
			
			if(f.equals("_fitness.log")) {
				checkFitness(folder.getPath());
			} else if(new File(fn).isDirectory()) {
				checkSubFolders(new File(fn));
			}
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
	
	private void runEvaluation(String folder, JBotEvolver jbot, String act, int actNumber) throws Exception{
		StringBuffer result = new StringBuffer();
		String[] split = folder.split("/");
		
		int generation = 0;
		
		if(postEvalBest)
			generation = getBestGeneration(folder);
		
		String filename = postEvalBest ? folder+"/show_best/showbest"+generation+".conf" :  folder+"/_showbest_current.conf";
		for(int sample = 0 ; sample < samples ; sample++) {
			
			String randomizeSeed = this.randomizeSeed ? "\n--random-seed "+sample : "";
			randomizeSeed = randomizeSeed + (this.randomizeSeed ? "\n--simulator +fixedseed=0" : ""); 
			
			String options = "--environment +fitnesssample="+sample+randomizeSeed+"\n";
			options+="--robots +chosenactuator="+actNumber+"\n";
			options+="--simulator +folder=("+f+")\n";
		
			jbot.loadFile(filename, options);
			
			if(!postEvalBest && !jbot.getPopulation().evolutionDone())
				return;
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = jbot.getEvaluationFunction()[0];
			sim.addCallback(eval);
			
			MultipleWheelRepertoireActuator actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
//			MultipleWheelRepertoireNDActuator actuator = (MultipleWheelRepertoireNDActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireNDActuator.class);
			
			String bigLine = "";
			sim.setupEnvironment();
			
			String folderName = split[split.length-4];
			String setupName = split[split.length-2]+"_"+split[split.length-3];
			String runName = split[split.length-1];
			
			for(double time = 0 ; time < sim.getEnvironment().getSteps() ; time++){
				sim.performOneSimulationStep(time);
				Vector2d point = actuator.getLastPoint();
				String line= folderName+"\t"+setupName+"\t"+runName+"\t"+sample+"\t"+(sample % fitnessSamples)+"\t"+(int)time+"\t"+(int)point.x+"\t"+(int)point.y+"\n";
//				int[] point = actuator.getPrevBehavior();
//				int[] point = actuator.getRealPrevBehavior();
//				String line= folderName+"\t"+setupName+"\t"+runName+"\t"+sample+"\t"+(sample % fitnessSamples)+"\t"+(int)time+"\t"+(int)point[0]+"\t"+(int)point[1]+"\t"+(int)point[2]+"\n";
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