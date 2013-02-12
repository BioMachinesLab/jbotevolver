package evolutionaryrobotics.evolution;

import java.util.Random;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.GenerationalTask;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class GenerationalEvolution extends Evolution {
	
	private Population population;
	private boolean supressMessages = false;

	public GenerationalEvolution(JBotEvolver jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
		Arguments populationArguments = jBotEvolver.getArguments().get("--population");
		populationArguments.setArgument("genomelength", getGenomeLength());
		supressMessages = args.getArgumentAsIntOrSetDefault("supressmessages", 0) == 1;
		
		try {
			population = Population.getPopulation(jBotEvolver.getArguments().get("--population"));
			population.setRandomNumberGenerator(new Random(jBotEvolver.getRandom().nextInt()));
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	@Override
	public void executeEvolution() {
		
		if(population.getNumberOfCurrentGeneration() == 0)
			population.createRandomPopulation();
		
		while(!population.evolutionDone()) {
			
			Chromosome c;
			
			int totalChromosomes = 0;
			
			while ((c = population.getNextChromosomeToEvaluate()) != null) {
				jBotEvolver.getRandom().setSeed(population.getGenerationRandomSeed());
				
				int samples = population.getNumberOfSamplesPerChromosome();
				jBotEvolver.submitTask(new GenerationalTask(jBotEvolver,samples,c,population.getGenerationRandomSeed()));
				totalChromosomes++;
				print(".");
			}
			
			print("\n");
			
			while(totalChromosomes-- > 0) {
				SimpleFitnessResult result = (SimpleFitnessResult)jBotEvolver.getResult();
				population.setEvaluationResultForId(result.getChromosomeId(), result.getFitness());
				print("!");
			}
			
			print("\nGeneration "+population.getNumberOfCurrentGeneration()+
					"\tHighest: "+population.getHighestFitness()+
					"\tAverage: "+population.getAverageFitness()+
					"\tLowest: "+population.getLowestFitness()+"\n");
			
			try {
				jBotEvolver.getDiskStorage().savePopulation(population, jBotEvolver.getRandom());
			} catch(Exception e) {e.printStackTrace();}
			
			population.createNextGeneration();
		}
	}
	
	private int getGenomeLength() {
		
		Simulator sim = jBotEvolver.createSimulator(new Random(jBotEvolver.getRandom().nextLong()));
		Robot r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		Controller c = Controller.getController(sim,r, jBotEvolver.getArguments().get("--controllers"));
		
		int genomeLength = 0;
		
		if(c instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)c;
			genomeLength = controller.getGenomeLength();
		}
		return genomeLength;
	}
	
	private void print(String s) {
		if(!supressMessages)
			System.out.print(s);
	}
}