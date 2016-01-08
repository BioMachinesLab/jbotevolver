package main;

import java.util.ArrayList;
import java.util.Comparator;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class DebugMain {
	
	public static void main(String[] args) {
		
		JBotEvolver jbot;
		try {
			jbot = new JBotEvolver(new String[]{"experiments/coevolutionpatrolling_tests151127/1/show_best/ashowbest348.conf"});
			
			Population popA = jbot.getSpecificPopulation("a");
			Population popB = jbot.getSpecificPopulation("b");
			
			ArrayList<Double> fitness = new ArrayList<Double>();
			
			for(int i =0; i <100; i++){
				fitness.add(popA.getChromosome(i).getFitness());
				fitness.add(popB.getChromosome(i).getFitness());			
			}		
			
			fitness.sort(new Comparator<Double>() {

				@Override
				public int compare(Double arg0, Double arg1) {
					return Double.compare(arg0, arg1);
				}
			});

			for(Double a: fitness){
				System.out.println(a);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
