package novelty.postevaluators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import multiobjective.MOChromosome;
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
        List<MOChromosome> pool = getArchiveChromosomes();
        
        // Current population
        for (Chromosome ind : pop.getChromosomes()) {
        	MOChromosome moc = (MOChromosome)ind;
            pool.add(moc);
        }
        
        KNNDistanceCalculator calc = useKDTree && pool.size() >= k * 2 ?
                new KDTreeCalculator() :
                new BruteForceCalculator();
        calc.setPool(pool);
        
        // Calculate novelty for each Chromosome
        for (Chromosome ind : pop.getChromosomes()) {
        	MOChromosome moc = (MOChromosome)ind;
            double novScore = calc.getDistance(moc, k);
            
            ExpandedFitness expandedFit = (ExpandedFitness) moc.getEvaluationResult();
            expandedFit.setScore(scoreName, novScore);
            expandedFit.setFitness(novScore);
        }
    }


    protected void updateArchive(Population pop) {
    	
    	//TODO a lot of overhead calculations because of tree recalculations... probably should make this better
    	
	    Chromosome[] popInds = pop.getChromosomes();
	    Chromosome[] copy = Arrays.copyOf(popInds, popInds.length);
	    
	    List<MOChromosome> pool = getArchiveChromosomes();
	    
	    //TODO check if need to add the current pop or not. I don't think it's necessary
	    KNNDistanceCalculator calc = useKDTree && pool.size() >= k * 2 ?
                new KDTreeCalculator() :
                new BruteForceCalculator();
        calc.setPool(pool);
        
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
        	
        	double noveltyScore = exp.getScore(scoreName); 
        	
        	if(noveltyScore > noveltyThreshold) {
//        		System.out.println("added to archive");
        		addToArchive(copy[j],pop);
        		//TODO check if this is correct
        		calc.setPool(getArchiveChromosomes());
        	}
        	
            //Local competition based on fitness performance
        	
        	List<MOChromosome> neighbours = calc.getNeighbours(moc, 1);
        	
        	if(neighbours.size() > 0) {
	        	MOChromosome closestNeighbour = neighbours.get(0);
	        	
	        	double perf = exp.getScore("fitness");
	        	double perfNeighbour = ((ExpandedFitness)closestNeighbour.getEvaluationResult()).getScore("fitness");
	        	
	        	if(perf > perfNeighbour) {
//	        		System.out.println("replaced in archive");
	        		replaceInArchive(moc, closestNeighbour, pop);
	        		//TODO check if this is correct
	        		calc.setPool(getArchiveChromosomes());
	        	}
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