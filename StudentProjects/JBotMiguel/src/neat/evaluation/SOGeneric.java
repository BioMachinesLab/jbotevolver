package neat.evaluation;

import org.encog.ml.MLMethod;
import evaluationfunctions.SingleANNClutteredMaze;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.Simulator;
import simulation.util.Arguments;

public class SOGeneric extends SingleObjective {

	protected EvaluationFunction eval;

	public SOGeneric(Arguments args) {
		super(args);
		Arguments realArgs = new Arguments(args.getCompleteArgumentString());
		realArgs.setArgument("classname", args.getArgumentAsString("realclassname"));
		eval = EvaluationFunction.getEvaluationFunction(realArgs);
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
