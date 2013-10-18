package main;

import evolutionaryrobotics.PostEvaluation;

public class pmain {
	
	public static void main(String[] args) {
//		PostEvaluation p = new PostEvaluation(new String[] {"dir=Evolution/Foraging/Evo3","samples=100","singleevaluation=1"});
		PostEvaluation p = new PostEvaluation(new String[] {"dir=bigdisk/paper_hugearena_1sensor/cooperative_foraging_90/3","samples=100","singleevaluation=1","localevaluation=1"});
		
		double[][] vals = p.runPostEval();
		
		double avg = p.getAverageFitness(vals);
		System.out.println("AVERAGE: " + avg);
	}
}
