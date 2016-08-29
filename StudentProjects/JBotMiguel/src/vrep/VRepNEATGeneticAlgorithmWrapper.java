package vrep;

import taskexecutor.VREPTaskExecutor;
import evolutionaryrobotics.evolution.NEATEvolution;
import evolutionaryrobotics.evolution.neat.NEATGeneticAlgorithmWrapper;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.populations.NEATPopulation;

public class VRepNEATGeneticAlgorithmWrapper extends NEATGeneticAlgorithmWrapper {
	
	protected int controllerType;
	protected int time;
	protected int nParams;
	protected int inputs;
	protected int outputs;
	
	public VRepNEATGeneticAlgorithmWrapper(NEATGADescriptor descriptor, NEATEvolution evo) {
		super(descriptor,evo);
	}
	
	public void setParameters(int controllerType, int time, int nParams, int inputs, int outputs) {
		this.controllerType = controllerType;
		this.time = time;
		this.nParams = nParams;
		this.inputs = inputs;
		this.outputs = outputs;
	}
	
	protected void evaluatePopulation(Chromosome[] genotypes) {
		
		evolutionaryrobotics.neuralnetworks.Chromosome[] chromosomes = new evolutionaryrobotics.neuralnetworks.Chromosome[genotypes.length];
		
		for(int i = 0 ; i < chromosomes.length ; i++)
			chromosomes[i] = ((NEATPopulation)evo.getPopulation()).convertChromosome(genotypes[i], i);
		
		float[][] packet = createDataPacket(chromosomes);
		
		float fixedParameters[] = new float[1+1];
		fixedParameters[0] = 1; //size
		fixedParameters[1] = time; //seconds of evaluation
		
		//controller type is hardcoded
		int nTasks = VRepUtils.sendTasks((VREPTaskExecutor)evo.getTaskExecutor(), fixedParameters, packet);
		
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
	
	private float[][] createDataPacket(evolutionaryrobotics.neuralnetworks.Chromosome[] chromosomes) {
		
		
		float[][] totalPackets = new float[chromosomes.length][];
		
		for(int i = 0 ; i < totalPackets.length ; i++) {
                        int index = 0;
		
			evolutionaryrobotics.neuralnetworks.Chromosome c = chromosomes[i];
			
			float[] params = new float[6 + c.getAlleles().length];
			
			params[index++] = c.getID(); //id of the chromosome
			params[index++] = c.getAlleles().length + 4; //length of chromosome + type
			params[index++] = controllerType; //type
			params[index++] = nParams;//the number of locomotion parameters of the controller
			params[index++] = inputs; //inputs
			params[index++] = outputs; //outputs
			
			for(int j = 0 ; j < c.getAlleles().length ; j++) {
				params[index++] = (float)c.getAlleles()[j];
			}
			
			totalPackets[i] = params;
		}
		
		return totalPackets;
	}
}