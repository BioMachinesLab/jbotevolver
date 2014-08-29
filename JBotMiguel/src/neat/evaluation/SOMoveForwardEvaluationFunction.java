package neat.evaluation;

import org.encog.ml.MLMethod;

import evaluationfunctions.MoveForwardEvaluationFunction;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

public class SOMoveForwardEvaluationFunction extends SingleObjective {

	protected MoveForwardEvaluationFunction eval;

	public SOMoveForwardEvaluationFunction(Arguments args) {
		super(args);
		eval = new MoveForwardEvaluationFunction(args);
	}

	@Override
	public void update(Simulator simulator) {
		eval.update(simulator);
	}

	@Override
	public double getFitness(){
		return eval.getFitness();
	}

	@Override
	public double calculateScore(MLMethod method) {
		double fitness = this.fitness;
		return fitness;
	}

	@Override
	public boolean requiresSimulation() {
		return true;
	}

	@Override
	public void computeObjective(MLMethod method) {}

	@Override
	public EvaluationFunction getEvaluationFunction() {
		return eval;
	}



}
