package main;


import evolutionaryrobotics.NEATPostEvaluation;

public class pnmain {

	private static boolean printValues = false;
	
	public static void main(String[] args) {
//		PostEvaluation p = new PostEvaluation(new String[] {"dir=Evolution/Foraging/Evo3","samples=100","singleevaluation=1","fitnesssamples=9"});
		NEATPostEvaluation p = new NEATPostEvaluation(new String[] {"dir=waypoint/waypoint/","samples=5","singleevaluation=0","localevaluation=0","fitnesssamples=2","showoutput=0"});
		
		int nRuns = 2;
		int fitnessSamples = 2;
		
		double[][][] values = p.runPostEval();
		
		if(printValues)
			printValues(values);
		
		System.out.println("################### ANALYSING ##################");
		
		double[] results = new double[nRuns];
		double[][] samplesAverages = new double[nRuns][fitnessSamples];
		double[][] samplesStdDeviations = new double[nRuns][fitnessSamples];
		
		int maxIndex = 0;
		
		for (int x = 0; x < values.length; x++) {
			double val = 0;

			for (int y = 0; y < values[x].length; y++) {
				for (int z = 0; z < values[x][y].length; z++) 
					val+= values[x][y][z];
				
				samplesAverages[x][y] = getAverage(values[x][y]);
				samplesStdDeviations[x][y] = getStdDeviation(values[x][y], samplesAverages[x][y]);
			}
			
			results[x] = val;
			
			if(results[x] > results[maxIndex])
				maxIndex = x;
		}
		
		int best = maxIndex+1;
		
		int generationsNumber = values[0][0].length;
		double[][] generationAverages = new double[nRuns][generationsNumber];
		
		for (int x = 0; x < values.length; x++) {
			for (int z = 0; z < values[x][0].length; z++) {
				double sampleAverage = 0;
				for (int y = 0; y < values[x].length; y++) {
					sampleAverage += values[x][y][z];
				}
				sampleAverage /= values[x].length;
				generationAverages[x][z] = sampleAverage; 
				
			}
		}
		
		int[] bestGenerations = new int[nRuns];
		
		for (int x = 0; x < generationAverages.length; x++) {
			double bestFitness = Double.MIN_VALUE;
			for (int y = 0; y < generationAverages[x].length; y++) {
				if(generationAverages[x][y] > bestFitness){
					bestFitness= generationAverages[x][y];
					bestGenerations[x] = y;
				}
			}
		}
		
		double[] runAverages = new double[nRuns];
		double[] runStdDeviations = new double[nRuns];
		
		for(int x = 0 ; x < runAverages.length ; x++) {
			runAverages[x] = getAverage(samplesAverages[x]);
			runStdDeviations[x] = getStdDeviation(samplesAverages[x], runAverages[x]);
		}
		
		String result = "#best: "+best+"\n";
		for(int i = 0 ; i < samplesAverages.length ; i++) {
			result+=(i+1)+" ";
			for(double j : samplesAverages[i])
				result+=j+" ";
			
			result+="("+runAverages[i]+" +- "+runStdDeviations[i]+") " + bestGenerations[i] + "\n";
		}
		
		double overallAverage = getOverallAverage(samplesAverages, nRuns);
		
		result+="Overall: "+overallAverage+" +- "+getOverallStdDeviation(samplesAverages, nRuns);
		
		System.out.println(result);
	}
	
	private static void printValues(double[][][] array){
		//runs
		for (int x = 0; x < array.length; x++) {
			System.out.println("run " + x);
			//fitness samples
			for (int y = 0; y < array[x].length; y++) {
				System.out.println("\tfitness sample: " + y);
				//generations
				for (int z = 0; z < array[x][y].length; z++) {
					System.out.println("\t\tGeneration_"+z+": "+array[x][y][z]);
				}
			}
		}
		System.out.println();
	}
	
	private static double getAverage(double[] values) {
		
		double avg = 0;
		
		for(double i : values)
			avg+=i;
		
		return avg/(double)values.length;
	}
	
	private static double getStdDeviation(double[] values, double avg) {
		
		double stdDeviation = 0;
		
		for(double d : values)
			stdDeviation+=Math.pow(d-avg,2);
		
		return Math.sqrt(stdDeviation/(double)values.length);
	}

	private static double getOverallAverage(double[][] values, int runs) {
		double[] avgs = new double[runs];
		
		for(int i = 0 ; i < avgs.length ; i++) 
			avgs[i] = getAverage(values[i]);
			
		return getAverage(avgs);
	}
	
	private static double getOverallStdDeviation(double[][] values, int runs) {
		double[] avgs = new double[runs];
		double[] stds = new double[runs];
		
		for(int i = 0 ; i < avgs.length ; i++) {
			avgs[i] = getAverage(values[i]);
			stds[i] = getStdDeviation(values[i], avgs[i]);
		}
		
		double overallAverage = getAverage(avgs);
		
		return getStdDeviation(avgs, overallAverage);
	}
	
}
