package novelty.results;

import java.util.Arrays;

import net.jafama.FastMath;
import novelty.EvaluationResult;

public class VectorBehaviourExtraResult extends VectorBehaviourResult {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3563909434469833876L;
	protected double extraValue;

    public VectorBehaviourExtraResult(double extra, double... bs) {
        super(bs);
        this.extraValue = extra;
    }
    
    public VectorBehaviourExtraResult(int dist, double extra, double... bs) {
        super(dist, bs);
        this.extraValue = extra;
    }


    @Override
    public VectorBehaviourExtraResult mergeEvaluations(EvaluationResult[] results) {
        double[] merged = new double[behaviour.length];
        double mergedExtraValue = 0;
        Arrays.fill(merged, 0f);
        for (int i = 0; i < merged.length; i++) {
            for (EvaluationResult r : results) {
                merged[i] += ((double[]) r.value())[i];
            }
            merged[i] /= results.length;
        }
        
        for (EvaluationResult r : results) {
        	mergedExtraValue+= ((VectorBehaviourExtraResult)r).getExtraValue()/results.length;
        }
        
        return new VectorBehaviourExtraResult(mergedExtraValue,merged);
    }
    
    public double getExtraValue() {
		return extraValue;
	}
    
}