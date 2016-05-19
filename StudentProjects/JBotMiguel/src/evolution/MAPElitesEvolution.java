package evolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import multiobjective.MOFitnessResult;
import multiobjective.MOTask;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.results.VectorBehaviourExtraResult;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;
import evaluationfunctions.OrientationEvaluationFunction;
import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evolution.GenerationalEvolution;
import evolutionaryrobotics.neuralnetworks.Chromosome;

/**
 *	MAP-Elites "illumination" algorithm
 *	J.-B. Mouret and J. Clune, Illuminating search spaces by mapping elites, arXiv 2015
 * @author miguelduarte
 */
public class MAPElitesEvolution extends GenerationalEvolution{
	
	protected double pruneThreshold = 0.0;
	
    public MAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
    	super(jBotEvolver, taskExecutor, arg);
    	pruneThreshold = arg.getArgumentAsDoubleOrSetDefault("prune", pruneThreshold);
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
		
		if(population.evolutionDone()) {
			prune((MAPElitesPopulation)population, pruneThreshold);
			expandToCircle((MAPElitesPopulation)population);
		}
		
		String dir = diskStorage.getOutputDirectory()+"/";
		String hash = getRepertoireHash((MAPElitesPopulation)population);
    	String file = "repertoire_"+hash+".txt";
		
		saveRepertoireTxt(dir,file,(MAPElitesPopulation)population);
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
	}
    
    public static void printRepertoire(MAPElitesPopulation p) {
    	for(int x = 0 ; x < p.getMap().length ; x++) {
			System.out.println();
			for(int y = 0 ; y < p.getMap()[x].length ; y++) {
				if(p.getMap()[x][y] == null)
					System.out.print(" ");
				else
					System.out.print("X");
			}
		}
    }
    
    public static void expandToCircle(MAPElitesPopulation p, double prune) {
    	MOChromosome[][] map = p.getMap();
    	double maxDist = 0;
//    	double resolution = p.getMapResolution();
    	
    	for(int x = 0 ; x < map.length ; x++) {
    		for(int y = 0 ; y < map[x].length ; y++) {
    			if(map[x][y] != null) {
	    			int posX = x;
	    			int posY = y;
	    			posX-= map.length/2;
	        		posY-= map[0].length/2;
	        		maxDist = Math.max(maxDist,new Vector2d(posX,posY).length());
    			}
    		}
    	}
    	
    	//create a copy of the map so that we only expand the map
    	//with some of the original behaviors
    	MOChromosome[][] mapNew = new MOChromosome[map.length][map[0].length];
    	
    	for(int x = 0 ; x < map.length ; x++) {
    		for(int y = 0 ; y < map[x].length ; y++) {
    			
    			double len = new Vector2d(x-map.length/2,y-map[x].length/2).length();
    			if(len <= maxDist) {
//    				int[] pos = p.getLocationFromBehaviorVector(new double[]{x,y});
    				if(map[x][y] == null) {
    					mapNew[x][y] = findNearest(x,y,map,prune);
    				}
    			}
    		}
    	}
    	
    	//merge the two maps
    	for(int i = 0 ; i < mapNew.length ; i++) {
    		for(int j = 0 ; j < mapNew[i].length ; j++) {
    			if(mapNew[i][j] != null) {
    				p.addToMapForced(mapNew[i][j],new int[]{i,j});
    			}
    		}
    	}
    }
    
    public static void expandToCircle(MAPElitesPopulation p) {
    	expandToCircle(p,0.0);
    }
    
    private static MOChromosome findNearest(int x, int y, MOChromosome[][] map, double prune) {
    	
    	MOChromosome nearest = null;
    	double nearestDistance = Double.MAX_VALUE;
    	Vector2d refPos = new Vector2d(x,y);
    	
    	for(int i = 0 ; i < map.length ; i++) {
    		for(int j = 0 ; j < map[i].length ; j++) {
    			if(map[i][j] != null) {
    				double dist = new Vector2d(i,j).distanceTo(refPos);
    				double fitness = getFitness(map[i][j]);
    				if(fitness >= prune && dist < nearestDistance) {
    					nearestDistance = dist;
    					nearest = map[i][j];
    				}
    			}
    		}
    	}
    	return nearest;
    }
    
    public static String getRepertoireHash(MAPElitesPopulation p) {
    	StringBuffer sb = new StringBuffer();
    	for(Chromosome c : p.getChromosomes()) {
    		for(double d : c.getAlleles())
    			sb.append(d+" ");
    	}
    	return ""+sb.toString().hashCode();
    }
    
    public static void saveRepertoireTxt(String dir, String file, MAPElitesPopulation p) {
    	
    	MOChromosome[][] map = p.getMap();
    	
    	try {
    	
    		FileWriter fw = new FileWriter(new File(dir+file));
    		StringBuffer buffer = new StringBuffer();
    		
    		buffer.append(map.length+" "+p.getMapResolution()+" ");
    		
    		for(int y = 0 ; y < map.length ; y++) {
    			for(int x = 0 ; x < map[y].length ; x++) {
    				MOChromosome moc = map[y][x];
    				if(moc != null) {
    					buffer.append(x+" "+y+" ");
	    				for(double d : moc.getAlleles())
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
    
    public static double getFitness(MOChromosome moc) {
    	ExpandedFitness fit = (ExpandedFitness)moc.getEvaluationResult();
		BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
		
		double[] behavior = (double[])br.value();
		Vector2d pos = new Vector2d(behavior[0],behavior[1]);
		double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
		double fitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
		return fitness;
    }
    
    //TODO make this more abstract
    public static void prune(MAPElitesPopulation mapPop, double pruneThreshold) {
    	
    	for(Chromosome c : mapPop.getChromosomes()) {
    		MOChromosome moc = (MOChromosome)c;
    		double fitness = getFitness(moc);
			
			if(fitness < pruneThreshold) {
				mapPop.removeFromMap(moc);
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
}