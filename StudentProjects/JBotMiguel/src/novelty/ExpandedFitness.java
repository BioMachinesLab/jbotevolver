package novelty;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 *
 * @author jorge
 */
public class ExpandedFitness extends FitnessResult {

    public static final String AUTO_FITNESS_PREFIX = "eval.";
    public static final String P_FITNESS_EVAL_INDEX = "fitness-index";
    public static final String FITNESS_SCORE = "fitness";
    protected int fitnessIndex = 0;

    protected EvaluationResult[] evalResults;
    protected LinkedHashMap<String,Double> scores;
    
    public ExpandedFitness() {
    	super();
	}
    
    public EvaluationResult[] getEvaluationResults() {
        return evalResults;
    }
    
    public void setEvaluationResults(EvaluationResult[] br) {
        this.evalResults = br;
        double fit = getFitnessScoreAux();
        this.setFitness(fit);
        scores = new LinkedHashMap<String,Double>();
        scores.put(FITNESS_SCORE, fit);
        
        // Automatically add all fitness results as scores
        for(int i = 0 ; i < br.length ; i++) {
            EvaluationResult e = br[i];
            if(e instanceof FitnessResult) {
                FitnessResult fr = (FitnessResult) e;
                scores.put(AUTO_FITNESS_PREFIX + i, fr.getFitness());
            }
        }
    }
    
    public EvaluationResult getCorrespondingEvaluation(int index) {
        EvaluationResult er = evalResults[index];
        return er;
    }   
    
    public void setScore(String name, double score) {
    	scores.put(name, score);
    }
    
    public double getScore(String score) {
        return scores.get(score);
    }
    
    public void setFitnessIndex(int index) {
        this.fitnessIndex = index;
    }
    
    @Override
    public double getFitness() {
        return scores.get(FITNESS_SCORE);
    }
    
    private double getFitnessScoreAux() {
        EvaluationResult er = getCorrespondingEvaluation(fitnessIndex);
        return (double) er.value();
    }

    /**
     * Requires fitness value to be set in each of the fitnesses
     *
     * @param state
     * @param fitnesses
     */
    public static ExpandedFitness setToMeanOf(ExpandedFitness[] fitnesses) {
    	
    	ExpandedFitness result = new ExpandedFitness();
    	
        if (fitnesses.length == 1) {
            // Just one, pass the information
        	result.setEvaluationResults(((ExpandedFitness) fitnesses[0]).evalResults);
        } else {
            // Merge evaluation results
            EvaluationResult[] evalFunctions = new EvaluationResult[((ExpandedFitness) fitnesses[0]).evalResults.length];
            for (int i = 0; i < evalFunctions.length; i++) { // for each evaluation function
                EvaluationResult[] evalTrials = new EvaluationResult[fitnesses.length];
                for (int j = 0; j < fitnesses.length; j++) { // for each trial
                    evalTrials[j] = ((ExpandedFitness) fitnesses[j]).evalResults[i];
                }
                evalFunctions[i] = (EvaluationResult) evalTrials[0].mergeEvaluations(evalTrials);
            }
            result.setEvaluationResults(evalFunctions);
            
            // Context corresponding to the individual with the median fitness score
            FitnessResult[] sortedFits = Arrays.copyOf(fitnesses, fitnesses.length);
            Arrays.sort(sortedFits, new Comparator<FitnessResult>() {
                @Override
                public int compare(FitnessResult o1, FitnessResult o2) {
                    return Double.compare(((ExpandedFitness) o2).getFitness(), 
                            ((ExpandedFitness) o1).getFitness());
                }
            });
        }
        
        return result;
    }
}