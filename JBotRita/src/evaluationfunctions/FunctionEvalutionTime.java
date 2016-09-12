package evaluationfunctions;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class FunctionEvalutionTime extends EvaluationFunction {

	public FunctionEvalutionTime(Arguments args) {
		super(args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(Simulator simulator) {
		// TODO Auto-generated method stub
		fitness= simulator.getTime();
	}

}
