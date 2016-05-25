package main;

import net.jafama.FastMath;
import evolutionaryrobotics.PostEvaluation;

public class pmain {
	
	public static void main(String[] args) {
//		PostEvaluation p = new PostEvaluation(new String[] {"dir=Evolution/Foraging/Evo3","samples=100","singleevaluation=1"});
		PostEvaluation p = new PostEvaluation(new String[] {"dir=bigdisk/shared_sensors_paper/boss/","samples=100","singleevaluation=0","localevaluation=0","fitnesssamples=9"});
		
		int nRuns = 10;
		
		double[][] values = p.runPostEval();
		
		System.out.println("################### ANALYSING ##################");
		
		double[] results = new double[nRuns];
		double[] averages = new double[nRuns];
		double[] stdDeviations = new double[nRuns];
		
		int maxIndex = 0;
		
		for(int i = 0 ; i < results.length ; i++) {
			
			double val = 0;
			
			for(int j = 0 ; j < values[i].length ; j++)
				val+= values[i][j];
			
			averages[i] = getAverage(values[i]);
			stdDeviations[i] = getStdDeviation(values[i],averages[i]);
			
			results[i] = val;
			
			if(results[i] > results[maxIndex])
				maxIndex = i;
		}
		
		int best = maxIndex+1;
		
		String result = "#best: "+best+"\n";
		
		for(int i = 0 ; i < values.length ; i++) {
			result+=(i+1)+" ";
			for(double j : values[i])
				result+=j+" ";
			
			result+="("+averages[i]+" +- "+stdDeviations[i]+")\n";
		}
		
		double overallAverage = getAverage(averages);
		
		result+="Overall: "+overallAverage+" +- "+getStdDeviation(averages, overallAverage);
		
		System.out.println(result);
	}
	
	private static double getAverage(double[] values) {
		
		double avg = 0;
		
		for(double i : values)
			avg+=i;
		
		return avg/(double)values.length;
	}
	
	private  static double getStdDeviation(double[] values, double avg) {
		
		double stdDeviation = 0;
		
		for(double d : values)
			stdDeviation+=FastMath.powQuick(d-avg,2);
		
		return FastMath.sqrtQuick(stdDeviation/(double)values.length);
	}
}
