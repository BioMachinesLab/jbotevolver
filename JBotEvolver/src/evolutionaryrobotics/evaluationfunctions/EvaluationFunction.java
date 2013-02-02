package evolutionaryrobotics.evaluationfunctions;

import java.io.Serializable;

import simulation.Simulator;
import simulation.Updatable;
import simulation.util.Arguments;

public abstract class EvaluationFunction implements Serializable, Updatable {
	protected Simulator simulator;
	protected double fitness;

	public EvaluationFunction(Simulator simulator, Arguments args) {
		this.simulator = simulator;
	}

	public double getFitness() {
		return fitness;
	}
}