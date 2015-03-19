package evolutionaryrobotics.evolution;

import java.util.Random;

import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.results.PostEvaluationResult;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.SimpleSampleTask;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.GenerationalEvolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class SingleSampleGenerationalEvolution extends GenerationalEvolution {
	
	public SingleSampleGenerationalEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
	}
	
	@Override
	public void executeEvolution() {
		
		if(population.getNumberOfCurrentGeneration() == 0)
			population.createRandomPopulation();
		
		int samples = population.getNumberOfSamplesPerChromosome();
		
		if(!population.evolutionDone()) {
			taskExecutor.setTotalNumberOfTasks((population.getNumberOfGenerations()-population.getNumberOfCurrentGeneration())*population.getPopulationSize()*samples);
		}
		
		double highestFitness = 0;
		
		while(!population.evolutionDone() && executeEvolution) {
			
			double d = Double.valueOf(df.format(highestFitness));
			taskExecutor.setDescription(output+" "+population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations() + " " + d);
			
			Chromosome c;
			
			int totalSamples = 0;
			
			while ((c = population.getNextChromosomeToEvaluate()) != null && executeEvolution) {
				
				Random r = new Random(population.getGenerationRandomSeed());
				for(int i = 0 ; i < samples ; i++) {
					taskExecutor.addTask(new SimpleSampleTask(c.getID(),new JBotEvolver(jBotEvolver.getArgumentsCopy(), jBotEvolver.getRandomSeed()),i,c,r.nextLong()));
					print(".");
				}
				totalSamples+=samples;
			}
			
			print("\n");
			
			double values[] = new double[population.getPopulationSize()];
			
			while(totalSamples-- > 0 && executeEvolution) {
				PostEvaluationResult result = (PostEvaluationResult)taskExecutor.getResult();
				print("!");
				values[result.getRun()]+=result.getFitness();
			}
			
			for(int i = 0; i < values.length && executeEvolution ; i++) {
				population.setEvaluationResultForId(i,values[i]/samples);
			}
			
			if(executeEvolution) {
				print("\nGeneration "+population.getNumberOfCurrentGeneration()+
						"\tHighest: "+population.getHighestFitness()+
						"\tAverage: "+population.getAverageFitness()+
						"\tLowest: "+population.getLowestFitness()+"\n");
				
				try {
					diskStorage.savePopulation(population);
				} catch(Exception e) {e.printStackTrace();}
				
				highestFitness = population.getHighestFitness();
				population.createNextGeneration();
			}
		}
		evolutionFinished = true;
		diskStorage.close();
	}
}