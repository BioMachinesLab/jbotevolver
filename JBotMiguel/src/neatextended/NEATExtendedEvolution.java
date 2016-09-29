package neatextended;

import java.util.Collections;

import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.NEATEvolution;
import evolutionaryrobotics.evolution.neat.NEATGeneticAlgorithmWrapper;
import evolutionaryrobotics.evolution.neat.PreEvaluatedFitnessFunction;
import evolutionaryrobotics.evolution.neat.core.InnovationDatabase;
import evolutionaryrobotics.evolution.neat.core.NEATPopulation4J;
import evolutionaryrobotics.evolution.neat.core.mutators.NEATMutator;
import evolutionaryrobotics.evolution.neat.core.pselectors.TournamentSelector;
import evolutionaryrobotics.evolution.neat.core.xover.NEATCrossover;
import evolutionaryrobotics.evolution.neat.ga.core.Chromosome;

public class NEATExtendedEvolution extends NEATEvolution {
	
	
	public NEATExtendedEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
	}

	@Override
	public void executeEvolution() {
		
		NEATGeneticAlgorithmWrapper algorithm;
		
		int i = population.getNumberOfCurrentGeneration();
		
		if(i == 0) {
			algorithm = new NEATGeneticAlgorithmWrapper(descriptor, this);
		}else {
			algorithm = new NEATGeneticAlgorithmWrapper(descriptor, this);
			algorithm.loadPopulation(population.getNEATPopulation4J());
		}
		
		algorithm.pluginFitnessFunction(new PreEvaluatedFitnessFunction(Collections.<Chromosome, Float> emptyMap()));
		algorithm.pluginCrossOver(new NEATCrossover());
		algorithm.pluginMutator(new NEATMutator());
		algorithm.pluginParentSelector(new TournamentSelector());
		
		if(i == 0) {
			algorithm.createPopulation();
			population.setGenerationRandomSeed(jBotEvolver.getRandomSeed());
			population.createRandomPopulation();
			population.setNEATPopulation4J((NEATPopulation4J)algorithm.population());
			population.getNEATPopulation4J().setSpecies(algorithm.getSpecies());
		}
		
		if(!population.evolutionDone())
			taskExecutor.setTotalNumberOfTasks((population.getNumberOfGenerations()-population.getNumberOfCurrentGeneration())*population.getPopulationSize());
		
		double highestFitness = 0;
		
		while (!population.evolutionDone() && executeEvolution) {
			
			double d = Double.valueOf(df.format(highestFitness));
			taskExecutor.setDescription(output+" "+population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations() + " " + d);
			
			algorithm.runEpoch();

			i++;
			
			if(executeEvolution) {
				  System.out.println("\nGeneration "+getPopulation().getNumberOfCurrentGeneration()+
							"\tHighest: "+population.getHighestFitness()+
							"\tAverage: "+population.getAverageFitness()+
							"\tLowest: "+population.getLowestFitness());
				  
				  try {
						diskStorage.savePopulation(population);
					} catch(Exception e) {e.printStackTrace();}
					
					highestFitness = population.getHighestFitness();
					population.createNextGeneration();
			}
		}
		
        InnovationDatabase db = algorithm.innovationDatabase();
		System.err.println("Innovation Database Stats - Hits:" + db.hits + " - misses:" + db.misses);
		
		
	}
	
	@Override
	protected void configureDescriptor(Arguments args) {
		
		int[] neurons = getInputOutputNeurons();
		args.setArgument("extraAlleles",neurons[1]);
		
		super.configureDescriptor(args);
	}
	
}
