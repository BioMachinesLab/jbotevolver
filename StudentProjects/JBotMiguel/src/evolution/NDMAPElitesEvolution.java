package evolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
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
public class NDMAPElitesEvolution extends GenerationalEvolution{
	
	protected int fillType = 0;
	
    public NDMAPElitesEvolution(JBotEvolver jBotEvolver, TaskExecutor taskExecutor, Arguments arg) {
    	super(jBotEvolver, taskExecutor, arg);
    	fillType = arg.getArgumentAsIntOrSetDefault("filltype", fillType);
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
			ArrayList<MOChromosome> randomChromosomes = ((NDMAPElitesPopulation)population).getRandomPopulation();
			evaluateAndAdd(randomChromosomes);
		}
		
		int nChromosomes = ((NDMAPElitesPopulation)population).getNumberOfChromosomes();
		
		while(!population.evolutionDone() && executeEvolution) {
			
			taskExecutor.setDescription(output+" "+population.getNumberOfCurrentGeneration()+"/"+population.getNumberOfGenerations() + " " + nChromosomes);
			
			if(executeEvolution) {
				
				ArrayList<MOChromosome> toEvaluate = new ArrayList<MOChromosome>(((NDMAPElitesPopulation)population).getBatchSize());
				
				for(int i = 0 ; i < ((NDMAPElitesPopulation)population).getBatchSize() ; i++) {
					MOChromosome moc = ((NDMAPElitesPopulation)population).getMutatedChromosome();
					toEvaluate.add(moc);
				}
				
				evaluateAndAdd(toEvaluate);
				
				nChromosomes = ((NDMAPElitesPopulation)population).getNumberOfChromosomes();
				
				print("\nGeneration: "+population.getNumberOfCurrentGeneration()+"\t"+
					  "\nChromosomes: "+((NDMAPElitesPopulation)population).getNumberOfChromosomes()+"\t"+
						"\nAverage Fitnes: "+population.getAverageFitness()+"\n");
				
				try {
					diskStorage.savePopulation(population);
				} catch(Exception e) {e.printStackTrace();}
				
				if(!population.evolutionDone())
					population.createNextGeneration();
			}
		}
		
		if(population.evolutionDone()) {
			
			if(fillType == 1) {
				expandToCircle((NDMAPElitesPopulation)population);
			}else if(fillType == 2)
				expandToCircleEuclidean((NDMAPElitesPopulation)population);
			
			if(fillType != 0)
				System.out.println("Expanded with filltype="+fillType+", total size: "+population.getChromosomes().length);
		}
		
		String dir = diskStorage.getOutputDirectory()+"/";
		String hash = getRepertoireHash(((NDMAPElitesPopulation)population));
    	String file = "repertoire_"+hash+".obj";
		
		serializeRepertoire(dir,file,((NDMAPElitesPopulation)population).getNDBehaviorMap());
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
    
    public static void expandToCircleEuclidean(NDMAPElitesPopulation pop) {
    	System.out.print("expandToCircleEuclidean.");
    	
    	NDBehaviorMap map = pop.getNDBehaviorMap();
    	
    	NDMAPElitesPopulation copy = new NDMAPElitesPopulation(pop.getArguments());
    	int dimensions = map.getNDimensions();
    	int orientationDmension = dimensions-1;
    	
    	if(dimensions == 3) {
    		
    		double[] xBuckets = map.getBucketListing(0);
			double[] yBuckets = map.getBucketListing(1);
			double[] orBuckets = map.getBucketListing(orientationDmension);
			
			//we need to find the radius of the circle
			 double maxDist = 0;
			 for(int x = 0 ; x < xBuckets.length ; x++ ) {
				 for(int y = 0 ; y < yBuckets.length ; y++) {
					 //grab any chromosome just to see if the x,y position is filled
					 for(int or = 0 ; or < orBuckets.length ; or++) {
						 Chromosome c = map.getChromosomeFromBehaviorVector(new double[]{xBuckets[x],yBuckets[y],orBuckets[or]});
						 if(c != null) {
							 //record the x,y distance and move to the next one
							 int posX = x - map.getSizes()[0]/2;
							 int posY = y - map.getSizes()[1]/2;
							 maxDist = Math.max(maxDist,new Vector2d(posX,posY).length());
							 break;
						 }
					 }
				 }
			 }
			 
			 map.setCircleRadius(maxDist);
			 
			 System.out.print(".");
			 
			//fill the "copy" population with the copies
			for(int i = 0 ; i < xBuckets.length ; i++ ) {
				double x = xBuckets[i];
				 for(int j = 0 ; j < yBuckets.length ; j++) {
					 double y = yBuckets[j];
					 
					 int posX = i - map.getSizes()[0]/2;
					 int posY = j - map.getSizes()[1]/2;
					 
					 double dist = new Vector2d(posX,posY).length();
					 
					 if(dist < maxDist) {
						 //find at least one orientation in the xy bucket make it elligible for filling
						 for(int u = 0 ; u < orBuckets.length ; u++) {
							 double or = orBuckets[u];
							 
							 double[] targetVec = new double[]{x,y,or};
							 
							 MOChromosome res = map.getChromosomeFromBehaviorVector(targetVec);
							 
							 if(res == null) {
								 int[] targetBuckets = map.getBucketsFromBehaviorVector(targetVec);
								 MOChromosome neareast = findNearest(map, targetBuckets);
								 copy.addToMapForced(neareast, targetVec);
							 }
						 }
					 }
				 }
			 }
			
			System.out.print(".");
			
			 //Copy new behaviors to the original population
			 for(int x = 0 ; x < xBuckets.length ; x++ ) {
				 for(int y = 0 ; y < yBuckets.length ; y++) {
					 for(int z = 0 ; z < orBuckets.length ; z++) {
						 double xPos = xBuckets[x];
						 double yPos = yBuckets[y];
						 double orPos = orBuckets[z];
						 double[] vec = new double[]{xPos,yPos,orPos};
						 MOChromosome newChromosome = copy.getNDBehaviorMap().getChromosomeFromBehaviorVector(vec);
						 if(newChromosome != null && map.getChromosomeFromBehaviorVector(vec) == null) {
							 pop.addToMapForced(newChromosome, vec);
						 }
					 }
				 }
			 }
			
    	} else {
 			throw new RuntimeException("Still need to implement filling for ndimensions > 3! [NDMapElitesEvolution]"); 
 		 }
    }
    
    public static MOChromosome findNearest(NDBehaviorMap map, int[] target) {
    	int[] nearest = findNeareast(map, target, new int[target.length], 0);
    	return map.getChromosomeFromBehaviorVector(map.getValuesFromBucketVector(nearest));
    }
    
    private static int[] findNeareast(NDBehaviorMap map, int[] target, int[] current, int currentDim) {
    	
    	int[] allTimeNeareast = null;
    	
		double allTimeDistance = Double.MAX_VALUE;
		
    	if(currentDim == current.length) {
    		//went through all dims, check if the chromosome exists
    		
    		if(map.getChromosomeFromBehaviorVector(map.getValuesFromBucketVector(current)) != null)
    			return current;
    		else
    			return null;
    	} else {
    		//go deeper!
    		double[] buckets = map.getBucketListing(currentDim);
    		for(int i = 0 ; i < buckets.length ; i++) {
    			
    			int[] clone = current.clone();
    			clone[currentDim] = i;
    			int[] currentNeareast = findNeareast(map, target, clone, currentDim+1);
    			
    			if(currentNeareast != null) {
    				
    	    		double distance =  euclideanDistance(currentNeareast, target,map.getCircularDimensions());
    				
    	    		if(allTimeNeareast == null || distance < allTimeDistance) {
    	    			allTimeNeareast = currentNeareast;
    	    			allTimeDistance = distance;
    	    		}
    			}
    		}
    	}
    	
    	return allTimeNeareast; 
    }
    
    private static double euclideanDistance(int[] a, int[] b, boolean[] circular) {
    	double diffSquareSum = 0.0;
        for (int i = 0; i < a.length; i++) {
        	
        	double currentMult = 0;
        	
        	if(circular[i]) {
        		currentMult = Math.abs(a[i] - b[i]);
        		currentMult = Math.min(currentMult, Math.abs(a[i] - b[i] + a.length));
        	}else {
        		currentMult = (a[i] - b[i]) * (a[i] - b[i]);
        	}
        	
        	diffSquareSum += currentMult;
        }
        return Math.sqrt(diffSquareSum);
    }
    
    
    public static void expandToCircle(NDMAPElitesPopulation pop) {
    	
    	int total = 0;
    	
    	int originalSize = pop.getChromosomes().length;
    	int dimensions = pop.getNDBehaviorMap().getNDimensions();
    	
    	//handle orientation first -> last dimension
    	int orientationDmension = dimensions-1;
    	double[] orientationBuckets = pop.getNDBehaviorMap().getBucketListing(orientationDmension);
    	
    	NDMAPElitesPopulation copy = new NDMAPElitesPopulation(pop.getArguments());
    	
    	if(dimensions == 3) {

			 double[] xBuckets = pop.getNDBehaviorMap().getBucketListing(0);
			 double[] yBuckets = pop.getNDBehaviorMap().getBucketListing(1);
			 
			 for(int i = 0 ; i < xBuckets.length ; i++ ) {
				 for(int j = 0 ; j < yBuckets.length ; j++) {
					 double x = xBuckets[i];
					 double y = yBuckets[j];
					 
					 //find at least one orientation in the xy bucket make it elligible for filling
					 for(int u = 0 ; u < orientationBuckets.length ; u++) {
						 double or = orientationBuckets[u];
						 
						 MOChromosome res = pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(new double[]{x,y,or});
						 
						 if(res != null) {
							 MOChromosome[] current = fillOrientation(pop, new double[]{x,y}, orientationBuckets);
							 
							 for(int z = 0 ; z < current.length ; z++) {
								 copy.addToMapForced(current[z], new double[]{x,y,orientationBuckets[z]});
							 }
							 
							 break;
						 }
					 }
				 }
			 }
			 
//			 System.out.println("Copy pop has "+copy.getChromosomes().length+"...");
			 
			//all orientations filled at this point, now fill the xy circle with copies of the closest xy bucket
			 
			 //first, we need to find the radius of the circle
			 double maxDist = 0;
			 
			 for(int x = 0 ; x < xBuckets.length ; x++ ) {
				 for(int y = 0 ; y < yBuckets.length ; y++) {
					 
					 //grab any chromosome just to see if the x,y position is filled
					 Chromosome c = pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(new double[]{xBuckets[x],yBuckets[y],orientationBuckets[0]});
					 
					 //check the euclidean distance to the center of the map
					 if(c != null) {
						int posX = x;
		    			int posY = y;
		    			posX-= pop.getNDBehaviorMap().getSizes()[0]/2;
		        		posY-= pop.getNDBehaviorMap().getSizes()[1]/2;
		        		maxDist = Math.max(maxDist,new Vector2d(posX,posY).length());
					 }
				 }
			 }
			 
//			 System.out.println("Expanded orientations from "+originalSize+" to "+pop.getChromosomes().length+"...");
//			 System.out.println("Max Dist: "+maxDist);
			 
			 //next, we copy existing behaviors to the unfilled circle positions
			 for(int x = 0 ; x < xBuckets.length ; x++ ) {
				 for(int y = 0 ; y < yBuckets.length ; y++) {
					 double len = new Vector2d(x-pop.getNDBehaviorMap().getSizes()[0]/2,y-pop.getNDBehaviorMap().getSizes()[1]/2).length();
					 
					 //if we are inside the circle
					 if(len <= maxDist) {
						 
						 double xPos = xBuckets[x];
						 double yPos = yBuckets[y];
						 
						 if(pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(new double[]{xPos,yPos,orientationBuckets[0]}) == null) {
							 //we have to fill with the closest neighbour
							 
							 double[] xy = findNearest(xPos,yPos, pop);
							 
							 //copy the original one to the copy's new location
							 for(int z = 0 ; z < orientationBuckets.length ; z++) {
								 MOChromosome original = pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(new double[]{xy[0],xy[1],orientationBuckets[z]});
								 copy.addToMapForced(original, new double[]{xPos,yPos,orientationBuckets[z]});
							 }
						 } 
					 } 
				 }
//				 System.out.println();
			 }
			 
//			 System.out.println("Copy pop has "+copy.getChromosomes().length+"...");
			 
			 //Copy new behaviors to the original population
			 for(int x = 0 ; x < xBuckets.length ; x++ ) {
				 for(int y = 0 ; y < yBuckets.length ; y++) {
					 for(int z = 0 ; z < orientationBuckets.length ; z++) {
						 double xPos = xBuckets[x];
						 double yPos = yBuckets[y];
						 double orPos = orientationBuckets[z];
						 double[] vec = new double[]{xPos,yPos,orPos};
						 MOChromosome newChromosome = copy.getNDBehaviorMap().getChromosomeFromBehaviorVector(vec);
						 if(newChromosome != null && pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(vec) == null) {
							 pop.addToMapForced(newChromosome, vec);
						 }
					 }
				 }
			 }
			 
		 } else {
 			throw new RuntimeException("Still need to implement filling for ndimensions > 3! [NDMapElitesEvolution]"); 
 		 }
    	System.out.println("Expanded repertoire from "+originalSize+" to "+pop.getChromosomes().length);
    }
    
    public static double[] findNearest(double x, double y, NDMAPElitesPopulation pop) {
    	MOChromosome nearest = null;
    	double nearestDistance = Double.MAX_VALUE;
    	double[] nearestPos = new double[2];
    	Vector2d refPos = new Vector2d(x,y);
    	
    	double[] xBuckets = pop.getNDBehaviorMap().getBucketListing(0);
		double[] yBuckets = pop.getNDBehaviorMap().getBucketListing(1);
		double[] orientationBuckets = pop.getNDBehaviorMap().getBucketListing(2);
    	
    	for(int i = 0 ; i < pop.getNDBehaviorMap().getSizes()[0] ; i++) {
    		for(int j = 0 ; j < pop.getNDBehaviorMap().getSizes()[1] ; j++) {
    			
    			double xPos = xBuckets[i];
    			double yPos = yBuckets[j];
    			//any orientation is fine because they should all be filled by now, so let's go with the first bucket
    			double orPos = orientationBuckets[0];
    			
    			MOChromosome c = pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(new double[]{xPos,yPos,orPos});
    			if(c != null) {
    				double dist = new Vector2d(xPos,yPos).distanceTo(refPos);
    				if(dist < nearestDistance) {
    					nearestDistance = dist;
    					nearestPos[0] = xPos;
    					nearestPos[1] = yPos;
    				}
    			}
    		}
    	}
//    	System.out.println(x+", "+y+" -> "+nearestPos[0]+","+nearestPos[1]);
    	return nearestPos;
    }
    
    public static MOChromosome[] fillOrientation(NDMAPElitesPopulation pop, double[] vec, double[] orientationBuckets) {
    	
    	MOChromosome[] current = new MOChromosome[orientationBuckets.length];
    	
    	double[] fullVec = new double[vec.length+1];
    	
    	for(int i = 0 ; i < vec.length ; i++)
    		fullVec[i] = vec[i];
    	
    	for(int i = 0 ; i < orientationBuckets.length ; i++) {
    		
    		//fill the last position of the characterization vector with the correct orientation
    		fullVec[fullVec.length-1] = orientationBuckets[i];

    		//check if this bucket has been filled during the evolutionary process
    		current[i] = pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(fullVec);
    	}
    	
    	for(int i = 0 ; i < current.length ; i++) {
    		fullVec[fullVec.length-1] = orientationBuckets[i];
    	}
    	
    	int count = 0;
    	
    	for(int i = 0 ; i < current.length ; i++) {
    		
    		//if there is no behavior for a particular orientation, we need to fill it with the closest one
    		if(current[i] == null) {
    			
    			int minDist = Integer.MAX_VALUE;
    			MOChromosome chosen = null;
    			
    			//find the circular distance of the closest orientation
    			//modulo explanation: http://stackoverflow.com/questions/4412179/best-way-to-make-javas-modulus-behave-like-it-should-with-negative-numbers
    			for(int j = 0 ; j < current.length ; j++ ) {
    				MOChromosome cur = current[((j+i) % current.length + current.length) % current.length]; 
    				if(cur != null) {
    					if(j < minDist) {
    						chosen = cur;
    						minDist = j;
    						break;
    					}
    				}
    			}
    			for(int j = 0 ; j < current.length ; j++ ) {
    				MOChromosome cur = current[((j-i) % current.length + current.length) % current.length];
    				if(cur != null) {
    					if(j < minDist) {
    						chosen = cur;
    						minDist = j;
    						break;
    					}
    				}
    			}
    			current[i] = chosen;
    			count++;
    		}
    	}
    	
    	for(int i = 0 ; i < current.length ; i++) {
    		fullVec[fullVec.length-1] = orientationBuckets[i];
    		if(pop.getNDBehaviorMap().getChromosomeFromBehaviorVector(fullVec) == null)
    			pop.addToMapForced(current[i], fullVec);
    	}
    	
    	for(int i = 0 ; i < current.length ; i++) {
    		fullVec[fullVec.length-1] = orientationBuckets[i];
    	}
    	
    	return current;
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
			((NDMAPElitesPopulation)population).addToMap(moc);
			print("!");
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
    
    public static String getRepertoireHash(NDMAPElitesPopulation p) {
    	StringBuffer sb = new StringBuffer();
    	for(Chromosome c : p.getChromosomes()) {
    		for(double d : c.getAlleles())
    			sb.append(d+" ");
    	}
    	return ""+sb.toString().hashCode();
    }
    
    public static void serializeRepertoire(String dir, String file, NDBehaviorMap map) {
    	try {
    		FileOutputStream fout = new FileOutputStream(dir+file);
        	ObjectOutputStream oos = new ObjectOutputStream(fout);
        	oos.writeObject(map);
        	fout.close();
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }

}