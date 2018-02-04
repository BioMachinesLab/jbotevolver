package evaluationfunctions.flocking.metrics;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class EvalutionTime extends EvaluationFunction {

	public EvalutionTime(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		fitness= simulator.getTime();
	}

}
