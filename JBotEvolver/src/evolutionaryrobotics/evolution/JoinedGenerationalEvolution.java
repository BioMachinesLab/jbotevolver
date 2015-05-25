package evolutionaryrobotics.evolution;

import java.text.DecimalFormat;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;
import taskexecutor.TaskExecutor;
import taskexecutor.results.SimpleFitnessResult;
import taskexecutor.tasks.MultipleChromosomeTask;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.neuralnetworks.MultipleChromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;

public class JoinedGenerationalEvolution extends Evolution{
	
	protected Population population;
	@ArgumentsAnnotation(name="supressmessages", values={"0","1"}, help="Set to 1 to show information about the evolution on the java console")
	protected boolean supressMessages = false;
	protected DiskStorage diskStorage;
	protected String output = "";
	protected DecimalFormat df = new DecimalFormat("#.##");
	private int numberOfChromossomes;

	public JoinedGenerationalEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
		super(jBotEvolver, taskExecutor, args);
		
		numberOfChromossomes = args.getArgumentAsIntOrSetDefault("numberofchromossomes", 2);
		
		Arguments populationArguments = jBotEvolver.getArguments().get("--population");
		populationArguments.setArgument("genomelengthstr", getGenomeLengthStr());
		populationArguments.setArgument("genomelength", getGenomeLength());
		populationArguments.setArgument("numberofchromossomes", numberOfChromossomes);
		
		supressMessages = args.getArgumentAsIntOrSetDefault("supressmessages", 0) == 1;
		
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
			
			MultipleChromosome c;
			
			int totalChromosomes = 0;
			
			while ((c = (MultipleChromosome) population.getNextChromosomeToEvaluate()) != null && executeEvolution) {
				int samples = population.getNumberOfSamplesPerChromosome();
				
				taskExecutor.addTask(new MultipleChromosomeTask(
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
	}
	
	protected void print(String s) {
		if(!supressMessages)
			System.out.print(s);
	}
	
	private String getGenomeLengthStr() {
		Simulator sim = jBotEvolver.createSimulator();
		
		String genomeLengthInfo = "";
		
		for(int i = 0; i < numberOfChromossomes; i++){
			
			Robot r = null;
			
			if(i == 0)
				 r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
			else
				 r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots" + i));
			
			Controller ctl;
			
			if(i == 0)
				ctl = Controller.getController(sim, r, jBotEvolver.getArguments().get("--controllers"));
			else
				ctl = Controller.getController(sim, r, jBotEvolver.getArguments().get("--controllers" + i));
			
			int genomeLength = 0;
			
			if(ctl instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)ctl;
				genomeLength = controller.getGenomeLength();
			}
			
			genomeLengthInfo += ":" + genomeLength;
		}
		
		//Estrutura da String -> ":genLen1:genLen2:genLen3..."		
		return genomeLengthInfo;
	}
	
	private int getGenomeLength() {
		Simulator sim = jBotEvolver.createSimulator();
		
		int totalGenomeLength = 0;
		
		for(int i = 0; i < numberOfChromossomes; i++){
			
			Robot r = null;
			
			if(i == 0)
				 r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots"));
			else
				 r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots" + i));
			
			Controller ctl = null;
			
			if(i == 0)
				ctl = Controller.getController(sim, r, jBotEvolver.getArguments().get("--controllers"));
			else
				ctl = Controller.getController(sim, r, jBotEvolver.getArguments().get("--controllers" + i));
					
			int genomeLength = 0;
			
			if(ctl instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)ctl;
				genomeLength = controller.getGenomeLength();
			}
			
			totalGenomeLength += genomeLength;
		}
			
		return totalGenomeLength;

	}
	
	@Override
	public Population getPopulation() {
		return population;
	}
}
