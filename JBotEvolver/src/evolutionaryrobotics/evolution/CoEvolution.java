package evolutionaryrobotics.evolution;

import java.text.DecimalFormat;
import java.util.Random;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import taskexecutor.results.SimpleCoEvolutionFitnessResult;
import taskexecutor.tasks.CoEvolutionTask;
import controllers.Controller;
import controllers.FixedLenghtGenomeEvolvableController;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.util.PopulationTable;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;
import evolutionaryrobotics.util.DiskStorage;

public class CoEvolution extends Evolution {
	
	private Population populationA;
	private Population populationB;
	private PopulationTable tA;
	private PopulationTable tB;
	
	private boolean supressMessages = false;
	private DiskStorage diskStorage;
	private String output = "";
	private int tablesize;
	private int gA;
	private int gB;
	private int preys;
	private boolean resume = false;
	private DecimalFormat df = new DecimalFormat("#.##");

	public CoEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments args) {
			super(jBotEvolver, taskExecutor, args);
			//Arguments population A
			Arguments populationAArguments = jBotEvolver.getArguments().get("--populationa");
			Arguments populationBArguments = jBotEvolver.getArguments().get("--populationb");
			
			if(populationAArguments == null){
				//resume
				Arguments tempArgs = jBotEvolver.getArguments().get("--population");
				try {
					resume = true;
					
					populationA = Population.getCoEvolutionPopulations(tempArgs, "a");
					populationB = Population.getCoEvolutionPopulations(tempArgs, "b");
					
					tA = (PopulationTable)populationA.getSerializableObjects().getFirst();
					tB = (PopulationTable)populationB.getSerializableObjects().getFirst();
					
					gA = populationA.getPopulationSize();
					gB = populationB.getPopulationSize();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else {
				populationAArguments.setArgument("genomelength", getGenomeLength());
				populationBArguments.setArgument("genomelength", getGenomePredatorLength());
				
				//Obter valor do tamanho da tabela
				tablesize = args.getArgumentAsIntOrSetDefault("tablesize", 10);
				// Obter o numero de gerações da população A
				Arguments ppA = jBotEvolver.getArguments().get("--populationa");
				gA = ppA.getArgumentAsIntOrSetDefault("size", 100);
				// Obter o numero de gerações da população B 
				Arguments ppB = jBotEvolver.getArguments().get("--populationb");
				gB = ppB.getArgumentAsIntOrSetDefault("size", 100);
				
				try {
					//populationA
					populationA = Population.getPopulation(jBotEvolver.getArguments().get("--populationa"));
					if(jBotEvolver.getArguments().get("--populationa").getArgumentIsDefined("generations"))
						populationA.setNumberOfGenerations(jBotEvolver.getArguments().get("--populationa").getArgumentAsInt("generations"));
					populationA.setGenerationRandomSeed(jBotEvolver.getRandomSeed());
					tA = new PopulationTable(tablesize);
					populationA.getSerializableObjects().add(tA);
					
					//populationB
					populationB = Population.getPopulation(jBotEvolver.getArguments().get("--populationb"));
					if(jBotEvolver.getArguments().get("--populationb").getArgumentIsDefined("generations"))
						populationB.setNumberOfGenerations(jBotEvolver.getArguments().get("--populationb").getArgumentAsInt("generations"));
					populationB.setGenerationRandomSeed(jBotEvolver.getRandomSeed());
					tB = new PopulationTable(tablesize);
					populationB.getSerializableObjects().add(tB);
						
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			
			supressMessages = args.getArgumentAsIntOrSetDefault("supressmessages", 0) == 1;
			
			// Obter o numero de presas
			Arguments numbPreys= jBotEvolver.getArguments().get("--robots");
			preys = numbPreys.getArgumentAsIntOrSetDefault("numberofrobots", 1);
			
			if (jBotEvolver.getArguments().get("--output") != null) {
				output = jBotEvolver.getArguments().get("--output").getCompleteArgumentString();
				System.out.println(output);
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
			//Criação as duas primeiras gerações 
			//Escolhe um chromosome aleatório e adiciona a tabela
			Random r = new Random(jBotEvolver.getRandomSeed());
			
			if(populationA.getNumberOfCurrentGeneration() == 0){
				populationA.createRandomPopulation();
				tA.add(populationA.getChromosome(r.nextInt(populationA.getPopulationSize())));
			}
			if(populationB.getNumberOfCurrentGeneration() == 0){
				populationB.createRandomPopulation();
				tB.add(populationB.getChromosome(r.nextInt(populationB.getPopulationSize())));
			}
			
			taskExecutor.setTotalNumberOfTasks((populationA.getNumberOfGenerations()-populationA.getNumberOfCurrentGeneration())*populationA.getPopulationSize()*tablesize*2);
			
			double highestFitnessA = 0;
			double highestFitnessB = 0;
			
			while(!populationA.evolutionDone()) {
				double[] fitnessA = new double[gA];
				double[] fitnessB = new double[gB];
				
				double fA = Double.valueOf(df.format(highestFitnessA));
				double fB = Double.valueOf(df.format(highestFitnessB));
				
				taskExecutor.setDescription(output+" "+populationA.getNumberOfCurrentGeneration()+"/"+populationA.getNumberOfGenerations() + " " + Double.valueOf(df.format(fA)) + " " + Double.valueOf(df.format(fB)));
				
				Chromosome c;
				
				int totalChromosomes = 0;
				
				//populationA
				
				
				while ((c = populationA.getNextChromosomeToEvaluate()) != null) {
					int samples = populationA.getNumberOfSamplesPerChromosome();
					
					for (Chromosome cB : tB.getTable()) {
						taskExecutor.addTask(new CoEvolutionTask(
								new JBotEvolver(jBotEvolver.getArgumentsCopy(), jBotEvolver.getRandomSeed()),
								samples,c,cB,preys,populationA.getGenerationRandomSeed(),"--evaluationa"));
						totalChromosomes++;
						print(".");
					}
				}
				
				print("\n");
				
				//Vai recebendo os resultados e insere no vector da fitnessA
				while(totalChromosomes > 0) {
					SimpleCoEvolutionFitnessResult result = (SimpleCoEvolutionFitnessResult)taskExecutor.getResult();
					addFitness(result, fitnessA, result.getChromosomeIdA());
					print("!");
					totalChromosomes--;
				}
				
				if(!resume){
					//Atribui a cada geração a sua fitness
					for (int i = 0; i < fitnessA.length; i++) {
//						System.out.println("Individuo Ai " + i+" : "+(fitnessA[i]/tB.getTable().size()));
						populationA.setEvaluationResultForId(i, (fitnessA[i]/tB.getTable().size()));
					}
					
					//Adiciona o melhor chormosoma a tabela de A
					tA.add(populationA.getBestChromosome());
				}
				
				highestFitnessA = populationA.getHighestFitness();
				highestFitnessB = populationB.getHighestFitness();
				
				print("\nPopulation A: " + 
						"\nGeneration "+populationA.getNumberOfCurrentGeneration()+
						"\tHighest: "+populationA.getHighestFitness()+
						"\tAverage: "+populationA.getAverageFitness()+
						"\tLowest: "+populationA.getLowestFitness()+"\n");
				
				//populationB
				
				while ((c = populationB.getNextChromosomeToEvaluate()) != null) {
					int samples = populationB.getNumberOfSamplesPerChromosome();
					
					for (Chromosome cA : tA.getTable()) {
						taskExecutor.addTask(new CoEvolutionTask(
								new JBotEvolver(jBotEvolver.getArgumentsCopy(), jBotEvolver.getRandomSeed()),
								samples,cA,c,preys,populationB.getGenerationRandomSeed(),"--evaluationb"));
						totalChromosomes++;
						print(".");
					}
				}
				
				print("\n");
				
				//Vai recebendo os resultados e insere no vector da fitnessB
				while(totalChromosomes > 0) {
					SimpleCoEvolutionFitnessResult result = (SimpleCoEvolutionFitnessResult)taskExecutor.getResult();
					addFitness(result, fitnessB, result.getChromosomeIdB());
					print("!");
					totalChromosomes--;
				}

				if(!resume){
					//Atribui a cada geração a sua fitness
					for (int i = 0; i < fitnessB.length; i++) {
//						System.out.println("Individuo Bi " + i+" : "+(fitnessB[i]/tA.getTable().size()));
						populationB.setEvaluationResultForId(i, (fitnessB[i]/tA.getTable().size()));
					}

					//Adiciona o melhor chormosoma a tabela de B
					tB.add(populationB.getBestChromosome());
				}
				
				print("\nPopulation B: " + 
						"\nGeneration "+populationB.getNumberOfCurrentGeneration()+
						"\tHighest: "+populationB.getHighestFitness()+
						"\tAverage: "+populationB.getAverageFitness()+
						"\tLowest: "+populationB.getLowestFitness()+"\n");
				
//				Imprimir a tabela de A e B:
//				printTables(tA, tB);
				
				if(!resume){
					try {
						diskStorage.savePopulations(populationA, populationB);
					} catch(Exception e) {e.printStackTrace();}
				}else{
					resume = false;
				}
				
				populationA.createNextGeneration();
				populationB.createNextGeneration();
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
		
		private int getGenomePredatorLength() {
			
			Simulator sim = jBotEvolver.createSimulator();
			Robot r = Robot.getRobot(sim, jBotEvolver.getArguments().get("--robots2"));
			Controller c = Controller.getController(sim,r, jBotEvolver.getArguments().get("--controllers2"));
			
			int genomeLength = 0;
			
			if(c instanceof FixedLenghtGenomeEvolvableController) {
				FixedLenghtGenomeEvolvableController controller = (FixedLenghtGenomeEvolvableController)c;
				genomeLength = controller.getGenomeLength();
			}
			return genomeLength;
		}
		
		private void printTables(PopulationTable tableA, PopulationTable tableB) {
			System.out.println("A Table");
			for(int i = 0 ; i < tableA.getTable().size() ; i++)
				System.out.print(tableA.getTable().get(i).getID()+" ");
			System.out.println();
			System.out.println("B Table");
			for(int i = 0 ; i < tableB.getTable().size() ; i++)
				System.out.print(tableB.getTable().get(i).getID()+" ");
		}
		
		private void addFitness(SimpleCoEvolutionFitnessResult result, double[] vector, int index){
			double fitness = vector[index];
			fitness += result.getFitness();
			vector[index] = fitness;
		}
		
		@Override
		public Population getPopulation() {
			return populationA;
		}
		
}
