package evolutionaryrobotics.evolution.neat;

import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.GenerationalTask;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.NEATEvolution;
import evolutionaryrobotics.evolution.neat.core.InnovationDatabase;
import evolutionaryrobotics.evolution.neat.core.NEATGADescriptor;
import evolutionaryrobotics.evolution.neat.core.NEATGeneticAlgorithm;
import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;
import evolutionaryrobotics.evolution.neat.ga.core.Species;
import evolutionaryrobotics.populations.NEATPopulation;

public class NEATGeneticAlgorithmWrapper extends NEATGeneticAlgorithm {
	
	private NEATEvolution evo;

	public NEATGeneticAlgorithmWrapper(NEATGADescriptor descriptor, NEATEvolution evo) {
		super(descriptor);
		this.evo = evo;
	}
	
	protected void evaluatePopulation(Chromosome[] genotypes) {
        int i;
        
        evolutionaryrobotics.neuralnetworks.Chromosome[] convertedGenotypes =
        		new evolutionaryrobotics.neuralnetworks.Chromosome[genotypes.length];
        
        for (i = 0; i < genotypes.length && evo.continueExecuting(); i++) {
        	
        	int samples = evo.getPopulation().getNumberOfSamplesPerChromosome();
        	
        	Chromosome neatChromosome = genotypes[i];
        	
        	evolutionaryrobotics.neuralnetworks.Chromosome jBotChromosome =
        			((NEATPopulation)evo.getPopulation()).convertChromosome(neatChromosome, i);
        	
        	convertedGenotypes[i] = jBotChromosome;
        	
        	GenerationalTask neatTask = new GenerationalTask(
        			new JBotEvolver(evo.getJBotEvolver().getArgumentsCopy(),
        			evo.getJBotEvolver().getRandomSeed()),
        			samples, jBotChromosome, evo.getPopulation().getGenerationRandomSeed());
        	evo.getTaskExecutor().addTask(neatTask);
    		System.out.print(".");
        }
        
        System.out.println();
        
        for(i = 0 ; i < genotypes.length && evo.continueExecuting() ; i++) {
        	SimpleFitnessResult r = (SimpleFitnessResult)evo.getTaskExecutor().getResult();
        	evo.getPopulation().setEvaluationResult(convertedGenotypes[r.getChromosomeId()],r.getFitness());
        	genotypes[r.getChromosomeId()].updateFitness(r.getFitness());
        	System.out.print("!");
        }
    }
	
}