package multiobjective;

import novelty.EvaluationResult;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public abstract class GenericEvaluationFunction extends EvaluationFunction{

	public GenericEvaluationFunction(Arguments args) {
		super(args);
	}

	public abstract EvaluationResult getEvaluationResult();

}
