package novelty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import multiobjective.MOChromosome;
import simulation.util.Arguments;
import edu.wlu.cs.levy.CG.Checker;
import edu.wlu.cs.levy.CG.KDTree;
import evolution.PostEvaluator;
import evolutionaryrobotics.JBotEvolver;
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
        List<BehaviourResult> pool = new ArrayList<BehaviourResult>();
        // Archive
        for (ArchiveEntry e : archive) {
            pool.add(e.getBehaviour());
        }
        // Current population
        for (Chromosome ind : pop.getChromosomes()) {
        	MOChromosome moc = (MOChromosome)ind;
            ExpandedFitness indFit = (ExpandedFitness) moc.getEvaluationResult();
            pool.add((BehaviourResult) indFit.getCorrespondingEvaluation(behaviourIndex));
        }
        KNNDistanceCalculator calc = useKDTree && pool.size() >= k * 2 ?
                new KDTreeCalculator() :
                new BruteForceCalculator();
        calc.setPool(pool);

        // Calculate novelty for each Chromosome
        for (Chromosome ind : pop.getChromosomes()) {
        	MOChromosome moc = (MOChromosome)ind;
            ExpandedFitness expandedFit = (ExpandedFitness) moc.getEvaluationResult();
            BehaviourResult br = (BehaviourResult) expandedFit.getCorrespondingEvaluation(behaviourIndex);
            double novScore = calc.getDistance(br, k);
            expandedFit.setScore(scoreName, novScore);
            expandedFit.setFitness(novScore);
        }
    }

    public interface KNNDistanceCalculator {
        public void setPool(List<BehaviourResult> pool);
        public double getDistance(BehaviourResult target, int k);

    }

    public class BruteForceCalculator implements KNNDistanceCalculator {

        private List<BehaviourResult> pool;

        @Override
        public void setPool(List<BehaviourResult> pool) {
            this.pool = pool;
        }

        @Override
        public double getDistance(BehaviourResult target, int k) {
            ArrayList<Double> distances = new ArrayList<>();
            for (BehaviourResult br : pool) {
                if (target != br) {
                    distances.add(distance(target,br));
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

        private KDTree<VectorBehaviourResult> tree;

        @Override
        public void setPool(List<BehaviourResult> pool) {
            VectorBehaviourResult vbr = (VectorBehaviourResult) pool.get(0);
            this.tree = new KDTree<>(vbr.getBehaviour().length);
            for(BehaviourResult br : pool) {
                vbr = (VectorBehaviourResult) br;
                double[] key = vbr.getBehaviour();
                try {
                    if(tree.search(key) == null) {
                        tree.insert(key, vbr);
                    }
                } catch (Exception ex) {
                	ex.printStackTrace();
                } 
            }
        }

        @Override
        public double getDistance(final BehaviourResult target, int k) {
            try {
                VectorBehaviourResult vbr = (VectorBehaviourResult) target;
                List<VectorBehaviourResult> nearest = tree.nearest(
                        vbr.getBehaviour(), k, new Checker<VectorBehaviourResult>() {
                            @Override
                            public boolean usable(VectorBehaviourResult v) {
                                return v != target;
                            }
                        });
                double dist = 0;
                for(VectorBehaviourResult v : nearest) {
                    dist += distance(target,v);
                }
                return dist / k;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return Double.NaN;
        }
    }

    protected double distance(BehaviourResult br1, BehaviourResult br2) {
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
                        (BehaviourResult) ((ExpandedFitness) moc.getEvaluationResult()).getCorrespondingEvaluation(behaviourIndex), 
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

        protected BehaviourResult behaviour;
        protected int generation;
        protected double fitness;

        protected ArchiveEntry(Population pop, BehaviourResult behaviour, double fitness) {
            this.behaviour = behaviour;
            this.fitness = fitness;
            this.generation = pop.getNumberOfCurrentGeneration();
        }

        public BehaviourResult getBehaviour() {
            return behaviour;
        }

        public int getGeneration() {
            return generation;
        }

        public double getFitness() {
            return fitness;
        }
    }
}