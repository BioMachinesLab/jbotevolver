package evolutionaryrobotics.evolution;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import evolutionaryrobotics.neuralnetworks.Chromosome;
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
		
		while(!population.evolutionDone()) {
			
			Chromosome c;
			
			int totalChromosomes = 0;
			
			while ((c = population.getNextChromosomeToEvaluate()) != null) {
				jBotEvolver.getRandom().setSeed(population.getGenerationRandomSeed());
				
				int samples = population.getNumberOfSamplesPerChromosome();
				int steps = population.getNumberOfStepsPerSample();
				jBotEvolver.submitTask(new GenerationalTask(samples,steps,c));
				totalChromosomes++;
			}
			
			while(totalChromosomes-- > 0) {
				ChromosomeResult result = (ChromosomeResult)jBotEvolver.getResult();
				population.setEvaluationResult(result.chromosome, result.fitness);
			}
		}
	}
	
	private int getGenomeLength() {
		
		Simulator sim = jBotEvolver.createSimulator();
		
		Robot r = RobotFactory.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		Controller c = ControllerFactory.getController(sim,r, jBotEvolver.getArguments().get("--controllers"));
		
		int genomeLength = 0;
		
		if(c instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)c;
			genomeLength = controller.getGenomeLength();
		}
		return genomeLength;
	}
	
	class ChromosomeResult {
		public double fitness;
		public Chromosome chromosome;
	}
	
	class GenerationalTask implements Callable<Object> {
		
		private int samples,steps;
		private double fitness = 0;
		private Chromosome chromosome;
		
		public GenerationalTask(int samples, int steps, Chromosome chromosome) {
			this.samples = samples;
			this.steps = steps;
			this.chromosome = chromosome;
		}
		
		@Override
		public Object call() {
			for(int i = 0 ; i < samples ; i++) {
				Simulator simulator = jBotEvolver.createSimulator();
				
				jBotEvolver.getArguments().get("--environment").setArgument("fitnesssample", i);
				
				Environment environment = jBotEvolver.getEnvironment(simulator);
				ArrayList<Robot> robots = jBotEvolver.createRobots(simulator);
				jBotEvolver.setChromosome(robots, chromosome);
				environment.addRobots(robots);
				
				EvaluationFunction eval = jBotEvolver.getEvaluationFunction(simulator);
				simulator.addCallback(eval);
				simulator.simulate(steps);
				
				fitness = eval.getFitness();
			}
			
			ChromosomeResult cr = new ChromosomeResult();
			cr.fitness = fitness;
			cr.chromosome = chromosome;
			
			return cr;
		}
	}
}