package neat.evaluation;

import org.encog.ml.MLMethod;

public interface CalculateScoreAsynchronous {
	
	void submitEvaluation(MLMethod method, int evalId);
	EvaluationResult getEvaluationResult();
}
