package utils;

import java.io.File;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class CheckFrontBack {
	
	int samples = 1;
	boolean randomizeSeed = true;
	boolean postEvalBest = true;
//	static String prefix = "bigdisk/december2015/10samples/";static String m = "_obstacle/";private double subtract = 10;
	static String prefix = "bigdisk/december2015/foraging/";static String m = "_foraging/";private double subtract = 0;
	
//	static String prefix = "";static String m = "_obstacle/";private double subtract = 10;
	
	public static void main(String[] args) throws Exception{
		
		String f = "";
		String[] setups = new String[]{f+"repertoire"+m+"AWS_3Actuator_30_foraging/",f+"repertoire"+m+"AWS_5Actuator_30_foraging/",f+"repertoire"+m+"AWS_8Actuator_30_foraging/"};
		
		System.out.println("Type\tSetup\tRun\tSample\tRobot\tGeneration\tFitness");
		
		for(String s : setups)
			new CheckFrontBack(prefix+s);
	}
	
	public CheckFrontBack(String folder) throws Exception{
		
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
		
			jbot.loadFile(filename, "--environment +fitnesssample="+i+randomizeSeed+"\n--robots +chosenactuator="+actNumber+"\n--evaluation classname=FrontBackEvaluationFunction");
			
			if(!postEvalBest && !jbot.getPopulation().evolutionDone())
				return "";
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = jbot.getEvaluationFunction()[0];
			sim.addCallback(eval);
			sim.simulate();
			
			if(generation == 0)
				generation = jbot.getPopulation().getNumberOfCurrentGeneration();
			
			double res = eval.getFitness();
			res-=subtract;
			
			String line= split[split.length-3]+"\t"+split[split.length-2]+"\t"+split[split.length-1]+"\t"+i+"\t"+act+"\t"+generation+"\t"+res+"\n";
			line = line.replaceAll("_30", "");
			line = line.replaceAll("_foraging", "");
			line = line.replaceAll("_obstacle", "");
			result+=line;
//			System.out.print(line);
			if(res < 0) {
				System.out.println(split[split.length-2]+"\t"+split[split.length-1]);
			}
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