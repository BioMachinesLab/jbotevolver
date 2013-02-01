package evolutionaryrobotics.evaluationfunctions;

import java.io.Serializable;

import simulation.Simulator;
import simulation.Updatable;

public abstract class EvaluationFunction implements Serializable,Updatable {
	protected Simulator simulator;

	public EvaluationFunction(Simulator simulator) {
		this.simulator = simulator;
	}

	public abstract double getFitness();
}