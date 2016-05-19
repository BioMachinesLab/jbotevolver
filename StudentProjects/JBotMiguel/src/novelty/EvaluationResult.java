package novelty;

import java.io.Serializable;

public interface EvaluationResult extends Serializable {
    
    public Object value();
    
    public EvaluationResult mergeEvaluations(EvaluationResult[] results);
    
}