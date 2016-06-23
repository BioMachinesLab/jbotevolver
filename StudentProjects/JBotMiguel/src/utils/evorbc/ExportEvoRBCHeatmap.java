package utils.evorbc;

import java.io.File;
import java.util.Scanner;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import fourwheeledrobot.MultipleWheelRepertoireActuator;

public class ExportEvoRBCHeatmap {
	
	int samples = 1;
	boolean randomizeSeed = false;
	boolean postEvalBest = false;
	static String f = "bigdisk/evorbc2/qualitymetrics/";
	
	public static void main(String[] args) throws Exception{
		
//		f = "bigdisk/evorbc2/multimaze/"; String[] setups = new String[]{f+"repertoire_obstacle/"};
//		f = "bigdisk/evorbc2/qualitymetrics/"; String[] setups = new String[]{f+"maze_radial/",f+"maze_distance/",f+"maze_quality/"};
		f = "bigdisk/evorbc2/repertoiresize/"; String[] setups = new String[]{f+"maze_quality_5/",f+"maze_quality_10/",f+"maze_quality_20/",f+"maze_quality_30/",f+"maze_quality_50/",f+"maze_quality_100/"};
		
		System.out.println("Folder\tSetup\tRun\tStep\tX\tY");
		
		for(String s : setups)
			new ExportEvoRBCHeatmap(s);
	}
	
	public ExportEvoRBCHeatmap(String folder) throws Exception{
		
		File f = new File(folder);
		
		String result = "";
		
		for(String s : f.list()) {
			result=checkSubFolders(new File(folder+s));
		}
		
//		System.out.println(result);
	}
	
	private String checkSubFolders(File folder) throws Exception {
		
		String result = "";
		
		if(folder.list() == null) {
			return result;
		}
		
		for(String f : folder.list()) {
			
			String fn = folder.getPath()+"/"+f;
			
			if(f.equals("_fitness.log")) {
				result=checkFitness(folder.getPath());
			} else if(new File(fn).isDirectory()) {
				checkSubFolders(new File(fn));
			}
		}
		return result;
	}
	
	private String checkFitness(String folder) throws Exception {
		
		String result = "";
		
		if(!new File(folder+"/_showbest_current.conf").exists())
			return result;
		
		JBotEvolver jbot = new JBotEvolver(new String[]{folder+"/_showbest_current.conf"});
		
//		if(!jbot.getPopulation().evolutionDone())
//			return result;
		
		Arguments actArgs = new Arguments(jbot.getArguments().get("--robots").getArgumentAsString("actuators"));
		Arguments mWheel = new Arguments(actArgs.getArgumentAsString(actArgs.getArgumentAt(0)));
		Arguments wheel = new Arguments(mWheel.getArgumentAsString("wheels"));
		int nActs = wheel.getNumberOfArguments();
		
		if(nActs == 0) {
			String actName = actArgs.getArgumentAt(0).split("=")[0];
			result+=runEvaluation(folder,jbot,actName,0);
		} else {
			for(int a = 0 ; a < nActs ; a++) {
				String actName = wheel.getArgumentAt(a).split("=")[0];
				result+=runEvaluation(folder,jbot,actName,a);
			}
		}
		
		return result;
	}
	
	private String runEvaluation(String folder, JBotEvolver jbot, String act, int actNumber) throws Exception{
		String result = "";
		String[] split = folder.split("/");
		
		int generation = 0;
		
		if(postEvalBest)
			generation = getBestGeneration(folder);
		
		String filename = postEvalBest ? folder+"/show_best/showbest"+generation+".conf" :  folder+"/_showbest_current.conf";
		for(int i = 0 ; i < samples ; i++) {
			
			String randomizeSeed = this.randomizeSeed ? "\n--random-seed "+i : "";
			randomizeSeed = randomizeSeed + (this.randomizeSeed ? "\n--simulator +fixedseed=0" : ""); 
			
			String options = "--environment +fitnesssample="+i+randomizeSeed+"\n";
			options+="--robots +chosenactuator="+actNumber+"\n";
			options+="--simulator +folder=("+f+")\n";
		
			jbot.loadFile(filename, options);
			
			if(!postEvalBest && !jbot.getPopulation().evolutionDone())
				return "";
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = jbot.getEvaluationFunction()[0];
			sim.addCallback(eval);
			
			MultipleWheelRepertoireActuator actuator = (MultipleWheelRepertoireActuator)sim.getRobots().get(0).getActuatorByType(MultipleWheelRepertoireActuator.class);
			
			String bigLine = "";
			sim.setupEnvironment();
			for(double time = 0 ; time < sim.getEnvironment().getSteps() ; time++){
				sim.performOneSimulationStep(time);
				Vector2d point = actuator.getLastPoint();
				String line= split[split.length-4]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+(int)time+"\t"+(int)point.x+"\t"+(int)point.y+"\n";
				line = line.replaceAll("_obstacle", "");
				bigLine+= line;
			}
			
			
			if(generation == 0)
				generation = jbot.getPopulation().getNumberOfCurrentGeneration();
			
			
			result+=bigLine;
			System.out.print(bigLine);
		}
		return result;
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