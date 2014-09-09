package neat.evolution;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import org.encog.Encog;


import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.Evolution;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;

public class NEATGenerationalEvolution extends Evolution {

	protected ERNEATPopulation population;
	protected boolean supressMessages = false;
	protected DiskStorage diskStorage;
	protected String output = "";
	
	protected final String STATISTICS_FILENAME = "statistics";


	public NEATGenerationalEvolution(JBotEvolver jBotEvolver,
			TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
		Arguments populationArguments = jBotEvolver.getArguments().get("--population"), 
				outputArguments = jBotEvolver.getArguments().get("--output");
		supressMessages = args.getArgumentAsIntOrSetDefault("supressmessages", 0) == 1;

		try {
			population = (ERNEATPopulation) Population.getPopulation(populationArguments);
			if(populationArguments.getArgumentIsDefined("generations"))
				population.setNumberOfGenerations(populationArguments.getArgumentAsInt("generations"));
			population.setGenerationRandomSeed(jBotEvolver.getRandomSeed());
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

		if (outputArguments != null) {
			output = outputArguments.getCompleteArgumentString();
			diskStorage = new DiskStorage(outputArguments.getCompleteArgumentString());
			try {
				diskStorage.start();
				diskStorage.saveCommandlineArguments(jBotEvolver.getArguments());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	@Override
	public void executeEvolution() {
		if(population.getNumberOfCurrentGeneration() == 0)
			population.createRandomPopulation(jBotEvolver);

		print("CREATED POPULATION...");
		if(!population.evolutionDone())
			taskExecutor.prepareArguments(jBotEvolver.getArguments());

//		taskExecutor.setTotalNumberOfTasks((population.getNumberOfGenerations() - 
//				population.getNumberOfCurrentGeneration())*population.getPopulationSize() * population.getNumberOfObjectives());

		print("GOING TO EVOLVE.\n");
		while(!population.evolutionDone()) {
			
			String fitness = (""+population.getHighestFitness());
			
			fitness = fitness.substring(0,Math.min(6,fitness.length()));

			taskExecutor.setDescription(output+" "+
					population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations()+" "+fitness);

			population.evolvePopulation(jBotEvolver, taskExecutor);
			
			population.updateStatistics();

			//print("\nSpecies " + population.getNumberOfSpecies() + "\n");
			
			print("\nGeneration "+population.getNumberOfCurrentGeneration()+
					"\tHighest: "+population.getHighestFitness()+
					"\tAverage: "+population.getAverageFitness()+
					"\tLowest: "+population.getLowestFitness()+"\n");
			
			try {
				
				diskStorage.savePopulation(population);
				//this.storeGenerationStatistics();
			} catch(Exception e) {e.printStackTrace();}
			
			population.resetStatistics();
		}
		
		Encog.getInstance().shutdown();
	}


	private void print(String s) {
		if(!supressMessages)
			System.out.print(s);
	}

}
