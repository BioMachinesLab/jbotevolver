package novelty;

public class FitnessResult implements EvaluationResult{
	
	protected double f;
	
	public FitnessResult(double f) {
		this.f = f;
	}
	
	public FitnessResult() {}
	
	public double getFitness() {
		return f;
	}
	
	public void setFitness(double f) {
		this.f = f;
	}

	@Override
	public Object value() {
		return f;
	}

	@Override
	public EvaluationResult mergeEvaluations(EvaluationResult[] results) {
		double r = 0;
		
		for(EvaluationResult e : results) {
			r+=(double)e.value();
		}
		r/=results.length;
		
		return new FitnessResult(r);
	}
	
}
