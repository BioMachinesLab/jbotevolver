package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import simulation.Simulator;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class IntersectionTest {
	
	private String[] steps = new String[]{"30"};
	private String maze = "obstacle";
	
	private String repertoireFolder = "repertoire/";
	private String intersectedRepertoireFolder = "intersected_repertoire";
	
	private int runs = 30;
	private static double subtract = 10;
	
	public static void main(String[] args) {
		
		System.out.println("Type\tSetup\tRun\tRobot\tFitness");
		
		new IntersectionTest().normalEvolution(new String[]{"FWS_2Actuator","FWS_3Actuator","AWS_3Actuator","AWS_5Actuator","AWS_8Actuator"});
		
		new IntersectionTest().intersectedEvolution("all",new String[]{"FWS_2Actuator","FWS_3Actuator","AWS_3Actuator","AWS_5Actuator","AWS_8Actuator"});
	}
	
	public void intersectedEvolution(String setup, String[] actuators) {
		
		String taskFolder = "single_intersection_repertoire_obstacle/";
		
		for(String step : steps) {
			for(String actuator : actuators) {
				
				String taskSetup = taskFolder+setup+"_"+actuator+"_"+step+"_"+maze+"/";
				
				for(String ra : actuators) {
					for(String rs : steps) {
						for(int i = 1 ; i <= runs ; i++) {
							String rFolder = intersectedRepertoireFolder+"_"+setup+"/";
							String rName = getIntersectedRepertoireName(rFolder,ra,rs);
							
							String config = getMultipleWheelRobotConfig(ra, rName);
							
							double f= (executeSample(taskSetup+i+"/",config));
							if(f != -1)
								System.out.println("intersection_"+setup+"\t"+actuator+"\t"+i+"\t"+ra+"\t"+f);
						}
					}
				}
			}
		}
	}
	
	public void normalEvolution(String[] actuators) {
		
		String taskFolder = "repertoire_obstacle/";
		
		for(String a : actuators) {
			for(String s : steps) {
				
				String taskSetup = taskFolder+a+"_"+s+"_"+maze+"/";
				
//				System.out.print(a+"\t");
				
				for(String ra : actuators) {
					for(String rs : steps) {
						for(int i = 1 ; i <= runs ; i++) {
							String rFolder = repertoireFolder+ra+"_"+rs+"/1/";
							String rName = getRepertoireName(rFolder);
							rFolder+=rName;
							
							String config = getMultipleWheelRobotConfig(ra, rFolder);
							
							double f= (executeSample(taskSetup+i+"/",config));
							
							if(f != -1)
								System.out.println("repertoire_obstacle\t"+a+"\t"+i+"\t"+ra+"\t"+f);
						}
					}
				}
			}
		}
	}
	
	public static double executeSample(String folder, String config) {
		
		double fitness = 0;
		String fn = folder+"/_showbest_current.conf";
		
		if(!new File(fn).exists())
			return -1;
		
		try {
		
			JBotEvolver jbot = new JBotEvolver(new String[]{fn});
			jbot.loadFile(fn, config);
			
			if(!jbot.getPopulation().evolutionDone())
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
	
	public static String getMultipleWheelRobotConfig(String actuatorName, String repertoire) {
		String result = "--robots +actuators=(MultipleWheelRepertoireActuator=(classname=MultipleWheelRepertoireActuator,"+
				"wheels=("+actuatorName+"=("+"classname="+actuatorName+",repertoire="+repertoire+")),id=1))\n";
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