package evaluationfunctions;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class DummyEvaluationFunction extends EvaluationFunction{

	public DummyEvaluationFunction(Arguments args) {
		super(args);
	}

	@Override
	public void update(Simulator simulator) {
		
	}
	
	public void setFitness(double f) {
		this.fitness = f;
	}

}
