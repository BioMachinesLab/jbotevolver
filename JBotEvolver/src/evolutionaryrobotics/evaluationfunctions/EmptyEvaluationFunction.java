package evolutionaryrobotics.evaluationfunctions;

import simulation.Simulator;
import simulation.util.Arguments;

public class EmptyEvaluationFunction extends EvaluationFunction {

	public EmptyEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
	}

}
