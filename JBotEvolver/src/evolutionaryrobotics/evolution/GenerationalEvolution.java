package evolutionaryrobotics.evolution;

import java.util.Random;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.GenerationalTask;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.parallel.SlaveResult;
import evolutionaryrobotics.populations.Population;
import factories.ControllerFactory;
import factories.PopulationFactory;
import factories.RobotFactory;

public class GenerationalEvolution extends Evolution {
	
	private Population population;

	public GenerationalEvolution(JBotEvolver jBotEvolver, Arguments args) {
		super(jBotEvolver, args);
		Arguments populationArguments = jBotEvolver.getArguments().get("--population");
		populationArguments.setArgument("genomelength", getGenomeLength());
		
		try {
			population = PopulationFactory.getPopulation(jBotEvolver.getArguments().get("--population"));
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
			
			System.out.println("Generation "+population.getNumberOfCurrentGeneration());
			
			while ((c = population.getNextChromosomeToEvaluate()) != null) {
				jBotEvolver.getRandom().setSeed(population.getGenerationRandomSeed());
				
				int samples = population.getNumberOfSamplesPerChromosome();
				int steps = population.getNumberOfStepsPerSample();
				jBotEvolver.submitTask(new GenerationalTask(jBotEvolver,samples,steps,c,population.getGenerationRandomSeed()));
				totalChromosomes++;
				System.out.print(".");
			}
			
			System.out.println();
			
			while(totalChromosomes-- > 0) {
				SlaveResult result = (SlaveResult)jBotEvolver.getResult();
				population.setEvaluationResultForId(result.getChromosomeId(), result.getFitness());
				System.out.print("!");
			}
			
			System.out.println("\n Highest Fitness: "+population.getHighestFitness());
			
			try {
				jBotEvolver.getDiskStorage().savePopulation(population, jBotEvolver.getRandom());
			} catch(Exception e) {e.printStackTrace();}
			
			population.createNextGeneration();
		}
		System.out.println("Evolution finished!");
	}
	
	private int getGenomeLength() {
		
		Simulator sim = jBotEvolver.createSimulator(new Random(jBotEvolver.getRandom().nextLong()));
		jBotEvolver.getEnvironment(sim);
		
		Robot r = RobotFactory.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		Controller c = ControllerFactory.getController(sim,r, jBotEvolver.getArguments().get("--controllers"));
		
		int genomeLength = 0;
		
		if(c instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)c;
			genomeLength = controller.getGenomeLength();
		}
		return genomeLength;
	}
}