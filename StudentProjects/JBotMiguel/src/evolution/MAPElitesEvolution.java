package evolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import multiobjective.MOChromosome;
import multiobjective.MOFitnessResult;
import multiobjective.MOTask;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.GenerationalEvolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evorbc.mappingfunctions.MappingFunction;

/**
 *	MAP-Elites "illumination" algorithm
 *	J.-B. Mouret and J. Clune, Illuminating search spaces by mapping elites, arXiv 2015
 * @author miguelduarte
 */
public class MAPElitesEvolution extends GenerationalEvolution{
	
    public MAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
    	super(jBotEvolver, taskExecutor, arg);
    }
    
    /*	procedure MAP-ELITES ALGORITHM (SIMPLE, DEFAULT VERSION)
		
		(P ← ∅, X ← ∅)							◃ Create an empty, N -dimensional map of elites: {solutions X and their performances P }
		for iter = 1 → I do							◃ Repeat for I iterations.
			if iter < G then						◃ Initialize by generating G random solutions
				x′ ← random solution()
			else									◃ All subsequent solutions are generated from elites in the map
				x ← random selection(X )			◃ Randomly select an elite x from the map X
				x′ ← random variation(x)			◃ Create x′, a randomly modified copy of x (via mutation and/or crossover)
			b′ ←feature descriptor(x′)				◃ Simulate the candidate solution x′ and record its feature descriptor b′
			p′ ←performance(x′)						◃ Record the performance p′ of x′
			if P(b′) = ∅ or P(b′) < p′ then			◃ If the appropriate cell is empty or its occupants’s performance is ≤ p′, then
				P(b′) ← p′							◃ store the performance of x′ in the map of elites according to its feature descriptor b′
				X (b′ ) ← x′						◃ store the solution x′ in the map of elites according to its feature descriptor b′
	    return feature-performance map (P and X )
     */
    @Override
	public void executeEvolution() {
    	
    	taskExecutor.setTotalNumberOfTasks((population.getNumberOfGenerations()-population.getNumberOfCurrentGeneration())*population.getPopulationSize());
    	
    	if(population.getNumberOfCurrentGeneration() == 0)
			population.createRandomPopulation();
		
		if(!population.evolutionDone()) {
			taskExecutor.setTotalNumberOfTasks((population.getNumberOfGenerations()-population.getNumberOfCurrentGeneration())*population.getPopulationSize());
		}
		
		if(population.getNumberOfCurrentGeneration() == 0) {
			ArrayList<MOChromosome> randomChromosomes = ((MAPElitesPopulation)population).getRandomPopulation();
			evaluateAndAdd(randomChromosomes);
		}
		
		int nChromosomes = ((MAPElitesPopulation)population).getNumberOfChromosomes();
		
		while(!population.evolutionDone() && executeEvolution) {
			
			taskExecutor.setDescription(output+" "+population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations() + " " + nChromosomes);
			
			if(executeEvolution) {
				
				ArrayList<MOChromosome> toEvaluate = new ArrayList<MOChromosome>(((MAPElitesPopulation)population).getBatchSize());
				
				for(int i = 0 ; i < ((MAPElitesPopulation)population).getBatchSize() ; i++) {
					MOChromosome moc = ((MAPElitesPopulation)population).getMutatedChromosome();
					toEvaluate.add(moc);
				}
				
				evaluateAndAdd(toEvaluate);
				
				nChromosomes = ((MAPElitesPopulation)population).getNumberOfChromosomes();
				
				print("\nGeneration: "+population.getNumberOfCurrentGeneration()+"\t"+
					  "\nChromosomes: "+((MAPElitesPopulation)population).getNumberOfChromosomes()+"\t"+
						"\nAverage Fitnes: "+population.getAverageFitness()+"\n");
				
				try {
					diskStorage.savePopulation(population);
				} catch(Exception e) {e.printStackTrace();}
				
				if(!population.evolutionDone())
					population.createNextGeneration();
			}
		}
		
		String dir = diskStorage.getOutputDirectory()+"/";
		String hash = getRepertoireHash((MAPElitesPopulation)population);
    	String file = "repertoire_"+hash+".txt";
		
    	System.out.println("Saving repertoire to file...");
		saveRepertoireTxt(dir,file,(MAPElitesPopulation)population);
		System.out.println("Saved!");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir+"repertoire_name.txt")));
			bw.write(hash);
			bw.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		try {
			diskStorage.savePopulation(population);
		} catch(Exception e) {e.printStackTrace();}
		
		
		evolutionFinished = true;
		diskStorage.close();
		System.out.println("Finished evolution!");
	}
    
    public static void printRepertoire(MOChromosome[][] map) {
    	for(int x = 0 ; x < map.length ; x++) {
			System.out.println();
			for(int y = 0 ; y < map[x].length ; y++) {
				if(map[x][y] == null)
					System.out.print(" ");
				else
					System.out.print("X");
			}
		}
    }
    
    public static void printRepertoire(MOChromosome[][] map, int dx, int dy) {
    	for(int x = 0 ; x < map.length ; x++) {
			System.out.println();
			for(int y = 0 ; y < map[x].length ; y++) {
				
				if (dx == x && dy == y) {
					System.out.print("#");
					continue;
				}
				
				if(map[x][y] == null)
					System.out.print(" ");
				else
					System.out.print("X");
			}
		}
    }
    
	protected void evaluateAndAdd(ArrayList<MOChromosome> randomChromosomes) {
		
		int totalEvaluations = 0;
		
		for(Chromosome c : randomChromosomes) {

			if(!executeEvolution)
				break;
			
			JBotEvolver jbot = new JBotEvolver(jBotEvolver.getArgumentsCopy(), jBotEvolver.getRandomSeed());
	
			int samples = population.getNumberOfSamplesPerChromosome();
		
			taskExecutor.addTask(new MOTask(jbot,
					samples,c,population.getGenerationRandomSeed())
			);
			totalEvaluations++;
			print(".");
			
		}
		
		print("\n");
		
		while(totalEvaluations-- > 0 && executeEvolution) {
			MOFitnessResult result = (MOFitnessResult)taskExecutor.getResult();
			MOChromosome moc = result.getChromosome();
			moc.setEvaluationResult(result.getEvaluationResult());
			((MAPElitesPopulation)population).addToMap(moc);
			print("!");
		}
	}
	
	public static String getRepertoireHash(MAPElitesPopulation p) {
    	StringBuffer sb = new StringBuffer();
    	for(Chromosome c : p.getChromosomes()) {
    		for(double d : c.getAlleles())
    			sb.append(d+" ");
    	}
    	return ""+sb.toString().hashCode();
    }
	
	public void saveRepertoireTxt(String dir, String file, MAPElitesPopulation p) {
		
		MOChromosome[][] map = p.getMap();
		
		double[][][] rep = new double[map.length][map[0].length][];
		
		for(int x = 0 ; x < map.length ; x++) {
			for(int y = 0 ; y < map[x].length ; y++) {
				if(map[x][y] != null)
					rep[x][y] = map[x][y].getAlleles();
			}
		}
		
		//get rid of isolated behaviors
		System.out.println("[MAPElitesEvolution] Behaviors before pruning: "+MappingFunction.countBehaviors(rep));
		MappingFunction.prune(rep);
		System.out.println("[MAPElitesEvolution] Behaviors after pruning: "+MappingFunction.countBehaviors(rep));
    	
    	try {
    	
    		FileWriter fw = new FileWriter(new File(dir+file));
    		StringBuffer buffer = new StringBuffer();
    		
    		buffer.append(map.length+" "+p.getMapResolution()+" ");
    		
    		for(int y = 0 ; y < rep.length ; y++) {
    			for(int x = 0 ; x < rep[y].length ; x++) {
    				double[] c = rep[y][x];
    				if(rep != null) {
    					buffer.append(x+" "+y+" ");
	    				for(double d : c)
	    					buffer.append(d+" ");
    				}
        		}
    		}
    		
    		fw.write(buffer.toString().trim());
    		
    		fw.close();
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
}