package vrep;

import taskexecutor.VREPTaskExecutor;
import evolutionaryrobotics.evolution.NEATEvolution;
import evolutionaryrobotics.evolution.neat.NEATGeneticAlgorithmWrapper;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.populations.NEATPopulation;

public class VRepNEATGeneticAlgorithmWrapper extends NEATGeneticAlgorithmWrapper {
	
	protected int time;
	
	public VRepNEATGeneticAlgorithmWrapper(NEATGADescriptor descriptor, NEATEvolution evo) {
		super(descriptor,evo);
	}
	
	public void setEvaluationTime(int time) {
		this.time = time;
	}
	
	protected void evaluatePopulation(Chromosome[] genotypes) {
		
		evolutionaryrobotics.neuralnetworks.Chromosome[] chromosomes = new evolutionaryrobotics.neuralnetworks.Chromosome[genotypes.length];
		
		for(int i = 0 ; i < chromosomes.length ; i++)
			chromosomes[i] = ((NEATPopulation)evo.getPopulation()).convertChromosome(genotypes[i], i);
		
		float fixedParameters[] = new float[1+1];
		fixedParameters[0] = 1; //size
		fixedParameters[1] = time; //seconds of evaluation
		
		//controller type is hardcoded
		int nTasks = VRepUtils.sendTasks((VREPTaskExecutor)evo.getTaskExecutor(), chromosomes, fixedParameters, 1);
		
		float[][] results = VRepUtils.receiveTasks((VREPTaskExecutor)evo.getTaskExecutor(), nTasks);
		
		for(int t = 0 ; t < results.length ; t++) {
			
			float[] vals = results[t];
			
			int index = 0;
			
			int nResults = (int)vals[index++];
			
			for(int res = 0 ; res < nResults ; res++) {
			
				//id
				int id = (int)vals[index++];
				
				int nVals = (int)vals[index++];
				
				float fitness = vals[index++];
				
				fitness+=10;
				
				evo.getPopulation().setEvaluationResult(chromosomes[id],fitness);
	        	genotypes[id].updateFitness(fitness);
			}
		}
    }
}