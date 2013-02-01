package evolutionaryrobotics.evaluationfunctions;

import java.io.Serializable;

import simulation.Simulator;

public abstract class EvaluationFunction implements Serializable,Updateable {
	protected Simulator simulator;

	public EvaluationFunction(Simulator simulator) {
		super();
		this.simulator = simulator;
	}

	public abstract double getFitness();
}