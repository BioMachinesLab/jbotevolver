package novelty;

public interface BehaviourResult extends EvaluationResult{

    public double distanceTo(BehaviourResult other);
    
}