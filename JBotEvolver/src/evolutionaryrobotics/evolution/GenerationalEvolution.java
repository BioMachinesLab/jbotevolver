package evolutionaryrobotics.evolution;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import taskexecutor.TaskExecutor;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.GenerationalTask;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;

public class GenerationalEvolution extends Evolution {
	
	protected Population population;
	@ArgumentsAnnotation(name="supressmessages", values={"0","1"}, help="Set to 1 to show information about the evolution on the java console")
	protected DiskStorage diskStorage;
	protected String output = "";
	protected DecimalFormat df = new DecimalFormat("#.##");

	public GenerationalEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
		
		setupPopulation();
		setupDiskStorage();
	}
	
	@Override
	public void executeEvolution() {
		
		if(population.getNumberOfCurrentGeneration() == 0)
			population.createRandomPopulation();
		
		if(!population.evolutionDone()) {
			taskExecutor.setTotalNumberOfTasks((population.getNumberOfGenerations()-population.getNumberOfCurrentGeneration())*population.getPopulationSize());
		}
		
		double highestFitness = 0;
		
		while(!population.evolutionDone() && executeEvolution) {
			
			double d = Double.valueOf(df.format(highestFitness));
			taskExecutor.setDescription(output+" "+population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations() + " " + d);
			
			Chromosome c;
			
			int totalChromosomes = 0;
			
			while ((c = population.getNextChromosomeToEvaluate()) != null && executeEvolution) {
				int samples = population.getNumberOfSamplesPerChromosome();
				
				taskExecutor.addTask(new GenerationalTask(
						new JBotEvolver(jBotEvolver.getArgumentsCopy(), jBotEvolver.getRandomSeed()),
						samples,c,population.getGenerationRandomSeed())
				);
				
				totalChromosomes++;
				print(".");
			}
			
			print("\n");
			
			while(totalChromosomes-- > 0 && executeEvolution) {
				SimpleFitnessResult result = (SimpleFitnessResult)taskExecutor.getResult();
				population.setEvaluationResultForId(result.getChromosomeId(), result.getFitness());
				print("!");
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
	
	private int getGenomeLength() {
		
		Simulator sim = jBotEvolver.createSimulator();
		Robot r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
		Controller c = Controller.getController(sim,r, jBotEvolver.getArguments().get("--controllers"));
		
		int genomeLength = 0;
		
		if(c instanceof FixedLenghtGenomeEvolvableController) {
			FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)c;
			genomeLength = controller.getGenomeLength();
		}
		return genomeLength;
	}
	
	@Override
	public Population getPopulation() {
		return population;
	}
	
	protected void setupPopulation() {
		Arguments populationArguments = jBotEvolver.getArguments().get("--population");
		populationArguments.setArgument("genomelength", getGenomeLength());
		
		try {
			population = Population.getPopulation(jBotEvolver.getArguments().get("--population"));
			if(jBotEvolver.getArguments().get("--population").getArgumentIsDefined("generations"))
				population.setNumberOfGenerations(jBotEvolver.getArguments().get("--population").getArgumentAsInt("generations"));
			population.setGenerationRandomSeed(jBotEvolver.getRandomSeed());
		} catch(Exception e) {
			System.out.println("Problem loading population "+populationArguments.getCompleteArgumentString());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	protected void setupDiskStorage() {
		if (jBotEvolver.getArguments().get("--output") != null) {
			output = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
			diskStorage = new DiskStorage(jBotEvolver.getArguments().get("--output").getCompleteArgumentString());
			try {
				diskStorage.start();
				diskStorage.saveCommandlineArguments(jBotEvolver.getArguments());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
}