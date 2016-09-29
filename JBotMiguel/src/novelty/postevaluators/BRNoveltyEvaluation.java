package novelty.postevaluators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class BRNoveltyEvaluation extends NoveltyEvaluation {
	
	public static final String P_NOVELTY_THRESHOLD = "novelty-threshold";
	protected double noveltyThreshold;
	
    public BRNoveltyEvaluation(Arguments args) {
    	super(args);
    	this.noveltyThreshold = args.getArgumentAsDouble(P_NOVELTY_THRESHOLD);
    }

    protected void setNoveltyScores(Population pop) {
        // Set the Chromosomes pool that will be used to compute novelty
        List<MOChromosome> pool = getArchiveAndPopulationChromosomes(pop);
        
        KNNDistanceCalculator calc = new BruteForceCalculator();
//        KNNDistanceCalculator calc = useKDTree && pool.size() >= k * 2 ?
//                new KDTreeCalculator() :
//                new BruteForceCalculator();
        calc.setPool(pool);
        
        // Calculate novelty for each Chromosome
        for (Chromosome ind : pop.getChromosomes()) {
        	MOChromosome moc = (MOChromosome)ind;
            double novScore = calc.getDistance(moc, k);
            
            ExpandedFitness expandedFit = (ExpandedFitness) moc.getEvaluationResult();
            expandedFit.setScore(scoreName, novScore);
            expandedFit.setFitness(novScore);
//            System.out.println(ind.getID()+" "+novScore);
        }
    }


    protected void updateArchive(Population pop) {
    	
    	//TODO a lot of overhead calculations because of tree recalculations... probably should make this better
	    Chromosome[] popInds = pop.getChromosomes();
	    Chromosome[] copy = Arrays.copyOf(popInds, popInds.length);
	    
	    List<MOChromosome> archive = getArchiveChromosomes();
	    
	    KNNDistanceCalculator calcArchive = new BruteForceCalculator();
//	    KNNDistanceCalculator calcArchive = useKDTree && archive.size() >= k * 2 ?
//                new KDTreeCalculator() :
//                new BruteForceCalculator();
        calcArchive.setPool(archive);
        
        Arrays.sort(copy, new Comparator<Chromosome>() {
            @Override
            public int compare(Chromosome o1, Chromosome o2) {
            	MOChromosome moc1 = (MOChromosome)o1;
            	MOChromosome moc2 = (MOChromosome)o2;
                double nov1 = ((ExpandedFitness) moc1.getEvaluationResult()).getScore(scoreName);
                double nov2 = ((ExpandedFitness) moc2.getEvaluationResult()).getScore(scoreName);
                return Double.compare(nov2, nov1);
            }
        });
        
        for (int j = 0; j < copy.length; j++) {
        	Chromosome c = copy[j];
        	MOChromosome moc = (MOChromosome)c;

        	ExpandedFitness exp = (ExpandedFitness)moc.getEvaluationResult();
        	BehaviourResult res = (BehaviourResult)exp.getCorrespondingEvaluation(this.behaviourIndex);
        	
        	//original BR-Evolution uses noveltyScore > noveltyThreshold, but we are implementing
        	//a variation to include solutions in heavily populated areas of the behavior space
        	//at least once. We calculate the difference with the closest neighbor, and add if
        	//it's sufficiently different.
//        	double noveltyScore = exp.getScore(scoreName); 
        	List<MOChromosome> neighbours = calcArchive.getNeighbours(moc, 1);
        	
        	if(!neighbours.isEmpty()) {
        	
        		MOChromosome closestNeighbour = neighbours.get(0);
        		
        		ExpandedFitness neighbourExpFit = (ExpandedFitness)closestNeighbour.getEvaluationResult();
        		BehaviourResult neighbourBehavior = (BehaviourResult)neighbourExpFit.getCorrespondingEvaluation(this.behaviourIndex);
        		
	        	double distanceToNearest = res.distanceTo(neighbourBehavior);
	        	
	        	if(distanceToNearest > noveltyThreshold) {
	        		addToArchive(copy[j],pop);
	        		calcArchive.setPool(getArchiveChromosomes());
	        	}
        	
	        	//Local competition based on fitness performance
	        	double perf = exp.getScore("fitness");
	        	double perfNeighbour = ((ExpandedFitness)closestNeighbour.getEvaluationResult()).getScore("fitness");
	        	
	        	if(perf > perfNeighbour) {
	        		replaceInArchive(moc, closestNeighbour, pop);
	        		calcArchive.setPool(getArchiveChromosomes());
	        	}
        	} else {
        		addToArchive(copy[j],pop);
        		calcArchive.setPool(getArchiveChromosomes());
        	}
        }
        regulateArchiveSize();
    }
    
    protected List<MOChromosome> getArchiveChromosomes() {
    	List<MOChromosome> pool = new ArrayList<MOChromosome>();
        // Archive
        for (ArchiveEntry e : archive) {
            pool.add(e.getChromosome());
        }
        return pool;
    }
    
    protected List<MOChromosome> getArchiveAndPopulationChromosomes(Population pop) {
    	List<MOChromosome> pool = getArchiveChromosomes();
    	 // Current population
        for (Chromosome ind : pop.getChromosomes()) {
        	MOChromosome moc = (MOChromosome)ind;
            pool.add(moc);
        }
        return pool;
    }
    
    protected void replaceInArchive(Chromosome c, Chromosome old, Population pop) {
    	MOChromosome moc = (MOChromosome)c;
    	ArchiveEntry ar = new ArchiveEntry(
        		pop, 
                moc, 
                ((ExpandedFitness) moc.getEvaluationResult()).getFitness()
           );
    	
    	//dummy ArchiveEntry just for comparison
    	ArchiveEntry oldEntry = new ArchiveEntry(pop, (MOChromosome)old, 0);
    	
    	int index = archive.indexOf(oldEntry);
    	archive.set(index, ar);
    }
    
    protected void addToArchive(Chromosome c, Population pop) {
    	MOChromosome moc = (MOChromosome)c;
    	ArchiveEntry ar = new ArchiveEntry(
        		pop, 
                moc, 
                ((ExpandedFitness) moc.getEvaluationResult()).getFitness()
           );
    	archive.add(ar);
    }
    
    protected void regulateArchiveSize() {
    	while(archive.size() > sizeLimit) {
    		archive.remove(rnd.nextInt(archive.size()));
    	}
    }

}