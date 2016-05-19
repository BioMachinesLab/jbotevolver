package novelty.postevaluators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import simulation.util.Arguments;
import edu.wlu.cs.levy.CG.Checker;
import edu.wlu.cs.levy.CG.KDTree;
import evolution.PostEvaluator;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.Population;

public class NoveltyEvaluation extends PostEvaluator {

    public static final String P_BEHAVIOUR_INDEX = "behaviour-index";
    public static final String P_K_NN = "knn";
    public static final String P_ARCHIVE_GROWTH = "archive-growth";
    public static final String P_ARCHIVE_SIZE_LIMIT = "archive-size";
    public static final String P_ARCHIVE_MODE = "archive-mode";
    public static final String P_ARCHIVE_CRITERIA = "archive-criteria";
    public static final String P_KD_TREE = "kd-tree";
    public static final String P_SCORE_NAME = "score-name";
    protected ArchiveMode archiveMode;
    protected ArchiveCriteria archiveCriteria;
    protected int k;
    protected double archiveGrowth;
    protected int sizeLimit;
    protected boolean useKDTree;
    protected int behaviourIndex;
    protected String scoreName;
    protected List<ArchiveEntry> archive;
    
    public enum ArchiveMode {
        shared, multiple;
    }

    public enum ArchiveCriteria {
        none, random, novel
    }
    
    protected Random rnd;

    public NoveltyEvaluation(Arguments args) {
    	super(args);
    	this.k = args.getArgumentAsIntOrSetDefault(P_K_NN, 15);
    	this.archiveGrowth = args.getArgumentAsDoubleOrSetDefault(P_ARCHIVE_GROWTH, 0.025);
    	this.sizeLimit = args.getArgumentAsIntOrSetDefault(P_ARCHIVE_SIZE_LIMIT, 1500);
    	this.archiveMode = ArchiveMode.valueOf(args.getArgumentAsStringOrSetDefault(P_ARCHIVE_MODE, "shared"));
    	this.archiveCriteria = ArchiveCriteria.valueOf(args.getArgumentAsStringOrSetDefault(P_ARCHIVE_CRITERIA, "random"));
    	this.useKDTree = args.getArgumentIsDefined(P_KD_TREE) ? args.getFlagIsTrue(P_KD_TREE) : true;
    	this.behaviourIndex = args.getArgumentAsIntOrSetDefault(P_BEHAVIOUR_INDEX, 1);
    	this.scoreName = args.getArgumentAsStringOrSetDefault(P_SCORE_NAME, "novelty");
    	
        this.archive = new ArrayList<ArchiveEntry>();
        this.rnd = new Random((long)args.getArgumentAsDouble("seed"));
    }
    
    public int getBehaviourIndex() {
        return behaviourIndex;
    }

    @Override
    public void processPopulation(Population pop) {
        // calculate novelty
        setNoveltyScores(pop);
        // update archive
        updateArchive(pop);
    }

    protected void setNoveltyScores(Population pop) {
        // Set the Chromosomes pool that will be used to compute novelty
        List<MOChromosome> pool = new ArrayList<MOChromosome>();
        // Archive
        for (ArchiveEntry e : archive) {
            pool.add(e.getChromosome());
        }
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

    public interface KNNDistanceCalculator {
        public void setPool(List<MOChromosome> pool);
        public double getDistance(MOChromosome target, int k);
        public List<MOChromosome> getNeighbours(MOChromosome target, int k);
    }

    public class BruteForceCalculator implements KNNDistanceCalculator {

        private List<MOChromosome> pool;

        @Override
        public void setPool(List<MOChromosome> pool) {
            this.pool = pool;
        }
        
        @Override
        public List<MOChromosome> getNeighbours(final MOChromosome moc, int k) {
        	
        	ArrayList<MOChromosome> allInds = new ArrayList<MOChromosome>();
        	final HashMap<MOChromosome,Double> distances = new HashMap<MOChromosome,Double>();
        	
			for (MOChromosome m : pool) {
			    if (moc != m) {
			   	 	allInds.add(m);
			   	 	distances.put(m,distance(m,moc));
			    }
			}
			
			Collections.sort(allInds,new Comparator<MOChromosome>() {
				 public int compare(MOChromosome o1, MOChromosome o2) {
					 
					 if(distances.get(o1) == distances.get(o2))
						 return 0;
					 
			         return distances.get(o1) < distances.get(o2) ? -1 : 1;
				 };
			});
			
//			for(MOChromosome m : allInds)
//				System.out.println(distances.get(m));
//			System.out.println();
			
			ArrayList<MOChromosome> neighbours = new ArrayList<MOChromosome>();
			for (int i = 0; i < k && allInds.size() > i; i++) {
			   neighbours.add(allInds.get(i));
			}
			return neighbours;
        }

        @Override
        public double getDistance(MOChromosome moc, int k) {
        	
            ArrayList<Double> distances = new ArrayList<Double>();
            for (MOChromosome m : pool) {
                if (moc != m) {
                    distances.add(distance(moc,m));
                }
            }
            Collections.sort(distances);
            double dist = 0;
            for (int i = 0; i < k; i++) {
                dist += distances.get(i);
            }
            return dist / k;
        }
    }

    public class KDTreeCalculator implements KNNDistanceCalculator {

        private KDTree<MOChromosome> tree;

        @Override
        public void setPool(List<MOChromosome> pool) {
            MOChromosome chr = (MOChromosome) pool.get(0);
            
            double[] vec = getBehaviorVector(chr);
            
            this.tree = new KDTree<>(vec.length);
            
            for(MOChromosome c : pool) {
            	
                double[] key = getBehaviorVector(c);
                
                try {
                    if(tree.search(key) == null) {
                        tree.insert(key, c);
                    }
                } catch (Exception ex) {
                	ex.printStackTrace();
                } 
            }
        }
        
        @Override
        public List<MOChromosome> getNeighbours(final MOChromosome target, int k) {
			
			try {
				
				double[] vec = getBehaviorVector(target);
				
                List<MOChromosome> nearest = tree.nearest(
                		vec, k, new Checker<MOChromosome>() {
                            @Override
                            public boolean usable(MOChromosome v) {
                                return v != target;
                            }
                        });
                return nearest;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
			return null;
        }

        @Override
        public double getDistance(final MOChromosome target, int k) {
            try {
            	
            	double[] vec = getBehaviorVector(target);
                
                List<MOChromosome> nearest = tree.nearest(
                        vec, k, new Checker<MOChromosome>() {
                            @Override
                            public boolean usable(MOChromosome v) {
                                return v != target;
                            }
                        });
                double dist = 0;
                for(MOChromosome c : nearest) {
                    dist += distance(target,c);
                }
                return dist / k;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Double.NaN;
        }
    }
    
    protected double[] getBehaviorVector(MOChromosome c) {
    	ExpandedFitness e = (ExpandedFitness) c.getEvaluationResult();
    	BehaviourResult b = (BehaviourResult) e.getCorrespondingEvaluation(behaviourIndex);
    	return (double[])b.value();
    }

    protected double distance(MOChromosome c1, MOChromosome c2) {
    	
    	ExpandedFitness exp1 = (ExpandedFitness) c1.getEvaluationResult();
    	ExpandedFitness exp2 = (ExpandedFitness) c2.getEvaluationResult();
    	
    	BehaviourResult br1 = (BehaviourResult) exp1.getCorrespondingEvaluation(behaviourIndex);
    	BehaviourResult br2 = (BehaviourResult) exp2.getCorrespondingEvaluation(behaviourIndex);
    	
        return br1.distanceTo(br2);
    }

    protected void updateArchive(Population pop) {
        if (archiveCriteria != ArchiveCriteria.none) {
            Chromosome[] popInds = pop.getChromosomes();
            LinkedHashSet<Chromosome> toAdd = new LinkedHashSet<Chromosome>();
            if (archiveCriteria == ArchiveCriteria.random) {
                while (toAdd.size() < archiveGrowth * popInds.length) {
                    int index = rnd.nextInt(popInds.length);
                    toAdd.add(popInds[index]);
                }
            } else if (archiveCriteria == ArchiveCriteria.novel) {
                Chromosome[] copy = Arrays.copyOf(popInds, popInds.length);
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
                for (int j = 0; j < archiveGrowth * popInds.length; j++) {
                    toAdd.add(copy[j]);
                }
            }
            for (Chromosome ind : toAdd) {
            	MOChromosome moc = (MOChromosome)ind;
                ArchiveEntry ar = new ArchiveEntry(
                		pop, 
                        moc, 
                        ((ExpandedFitness) moc.getEvaluationResult()).getFitness()
                   );
                if (archive.size() == sizeLimit) {
                    int index = rnd.nextInt(archive.size());
                    archive.set(index, ar);
                } else {
                    archive.add(ar);
                }
            }
        }
    }

    public List<ArchiveEntry> getArchive() {
        return archive;
    }

    public static class ArchiveEntry implements Serializable {

        protected MOChromosome moc;
        protected int generation;
        protected double fitness;

        protected ArchiveEntry(Population pop, MOChromosome moc, double fitness) {
            this.moc = moc;
            this.fitness = fitness;
            this.generation = pop.getNumberOfCurrentGeneration();
        }

        public MOChromosome getChromosome() {
            return moc;
        }

        public int getGeneration() {
            return generation;
        }

        public double getFitness() {
            return fitness;
        }
        
        @Override
        public boolean equals(Object obj) {
        	if(obj instanceof MOChromosome) {
        		return this.moc.equals(obj); 
        	} else if(obj instanceof ArchiveEntry) {
        		return ((ArchiveEntry)obj).moc.equals(this.moc);
        	} else
        		return false;
        }
    }
}