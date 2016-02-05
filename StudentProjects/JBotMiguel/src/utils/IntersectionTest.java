package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import simulation.Simulator;
import simulation.util.Arguments;
import taskexecutor.ParallelTaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class IntersectionTest {
	
	private String[] steps = new String[]{"30"};
	
//	private String prefix = "bigdisk/december2015/10samples/";private String maze = "obstacle";private static double subtract = 10;
//	private String prefix = "bigdisk/december2015/foraging/";private String maze = "foraging";private static double subtract = 0;
	
	private String prefix = "";private String maze = "obstacle";private static double subtract = 10;
	
	private String repertoireFolder = prefix+"repertoire/";
	private String intersectedRepertoireFolder = prefix+"intersected_repertoire";
	
	private int runs = 30;
	private int samples = 10;
	
	private static boolean randomSeed = true;
	private static boolean postEvalBest = true;
	
	public static void main(String[] args) throws Exception{
		
		System.out.println("Type\tSetup\tRun\tRobot\tSample\tFitness");
		
		new IntersectionTest().normalEvolution(new String[]{"FWS_2Actuator","FWS_3Actuator","AWS_3Actuator","AWS_5Actuator","AWS_8Actuator"});
//		new IntersectionTest().intersectedEvolution("all",new String[]{"FWS_2Actuator","FWS_3Actuator","AWS_3Actuator","AWS_5Actuator","AWS_8Actuator"});
	}
	
	public void intersectedEvolution(String setup, String[] actuators) throws Exception{
		
		String taskFolder = prefix+"single_intersection_repertoire_"+maze+"/";
		
		for(String step : steps) {
			for(String actuator : actuators) {
				
				for(int i = 1 ; i <= runs ; i++) {
					for(String rs : steps) {
						for(String ra : actuators) {						
							try {
								String taskSetup = taskFolder+setup+"_"+actuator+"_"+step+"_"+maze+"/";
							
								for(int s = 0 ; s < samples ; s++) {
									String rFolder = intersectedRepertoireFolder+"_"+setup+"/";
									String rName = getIntersectedRepertoireName(rFolder,ra,rs);
									
									String config = getMultipleWheelRobotConfig(ra, rName, getArguments(taskSetup+i+"/show_best/showbest0.conf","robots"));
									
									double f= (executeSample(taskSetup+i+"/",config,s));
									if(f != -1) {
										String line = "intersection_"+setup+"\t"+actuator+"\t"+i+"\t"+ra+"\t"+s+"\t"+f;
										line = line.replaceAll("_30_", "_");
										line = line.replaceAll("_"+maze, "");
										System.out.println(line);
										
									}
								}
							} catch(Exception e) {
								System.out.println("Problem with "+taskFolder+" "+actuator+"/"+i+" -> "+ra);
							}
						}
					}
				}
			}
		}
	}
	
	public void normalEvolution(String[] actuators) throws Exception{
		
		String taskFolder = prefix+"repertoire_"+maze+"/";
		
		for(String a : actuators) {
			
			for(String s : steps) {
				
				String taskSetup = taskFolder+a+"_"+s+"_"+maze+"/";
				
//				System.out.print(a+"\t");
				
				for(int i = 1 ; i <= runs ; i++) {
					for(String rs : steps) {
						for(String ra : actuators) {
							try {
								for(int samp = 0 ; samp < samples ; samp++) {
									String rFolder = repertoireFolder+ra+"_"+rs+"/1/";
									String rName = getRepertoireName(rFolder);
									rFolder+=rName;
									
									String config = getMultipleWheelRobotConfig(ra, rFolder,getArguments(taskSetup+i+"/show_best/showbest0.conf","robots"));
									
									double f= (executeSample(taskSetup+i+"/",config,samp));
									
									if(f != -1) {
										String line = "repertoire\t"+a+"\t"+i+"\t"+ra+"\t"+samp+"\t"+f;
										line = line.replaceAll("_30_", "_");
										line = line.replaceAll("_"+maze, "");
										System.out.println(line);
									}
								}
							} catch(Exception e) {
								System.out.println("Problem with "+taskFolder+" "+a+"/"+i+" -> "+ra);
							}
						}
					}
				}
			}
			
		}
	}
	
	public static double executeSample(String folder, String config, int sample) throws Exception{
		
		double fitness = 0;
		
		int best = 0;
		
		if(postEvalBest)
			best = CheckFitness.getBestGeneration(folder);
		
		String fn = postEvalBest ? folder+"/show_best/showbest"+best+".conf" :  folder+"/_showbest_current.conf";
		
		if(!new File(fn).exists())
			return -1;
		
		if(randomSeed) {
			config+="\n--random-seed "+sample;
		}
		
		try {
		
			JBotEvolver jbot = new JBotEvolver(new String[]{fn});
			jbot.loadFile(fn, config);
			
			if(!postEvalBest && !jbot.getPopulation().evolutionDone())
				return -1;
			
			Simulator sim = jbot.createSimulator();
			jbot.createRobots(sim);
			jbot.setupBestIndividual(sim);
			EvaluationFunction eval = jbot.getEvaluationFunction()[0];
			sim.addCallback(eval);
			sim.simulate();
			fitness = eval.getFitness();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return fitness - subtract;
	}
	
	public static Arguments getArguments(String filename, String argName) throws Exception {
		Scanner s = new Scanner(new File(filename));
		
		boolean reading = false;
		String result = "";
		
		while(s.hasNextLine()) {
			String line = s.nextLine().trim();
			
			if(!reading && line.startsWith("--"+argName))
				reading = true;
			
			if(reading && (line.startsWith("--") || line.startsWith("%")) && !line.startsWith("--"+argName)) {
				reading = false;
				break;
			}
			
			if(reading)
				result+=line+" \n";
		}
		String result2 = result.replaceAll("\n", "").replaceAll("\t", "");
		
		s.close();
		
		String[] options = Arguments.readOptionsFromString(result2);
		Arguments args = new Arguments(options[1]);
		
		return args;
	}
	
	public static String getMultipleWheelRobotConfig(String actuatorName, String repertoire, Arguments prevArgs) {
		String result = "--robots +actuators=(MultipleWheelRepertoireActuator=(classname=MultipleWheelRepertoireActuator,"+
				"wheels=("+actuatorName+"=("+"classname="+actuatorName+",repertoire="+repertoire+")),id=1)"+getPrevArgs("actuators",prevArgs)+")\n";
		return result;
	}
	
	public static String getPrevArgs(String name, Arguments prevArgs) {
		String result = prevArgs.getArgumentAsString(name);
		if(!result.trim().isEmpty())
			result = ","+result;
		return result;
	}
	
	public static String getIntersectedRepertoireName(String folder, String actuator, String steps) {
		String file = folder+"repertoire_"+actuator+"_"+steps+"_1.txt";
		return file;
	}
	
	public static String getRepertoireName(String folder) {
		String file = folder+"repertoire_name.txt";
		String result = "";
		Scanner s;
		try {
			s = new Scanner(new File(file));
			result = s.next();
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "repertoire_"+result+".txt";
	}
	
}