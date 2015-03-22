package neat.evaluation;

import org.encog.ml.CalculateScore;
import org.encog.ml.MLMethod;

import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

import simulation.util.Arguments;

public abstract class SingleObjective extends EvaluationFunction implements CalculateScore {

	private int id;
	private String objectiveKey;

	public SingleObjective(Arguments args) {
		super(args);
	}

	@Override
	public abstract double calculateScore(MLMethod arg0);

	@Override
	public boolean requireSingleThreaded() {
		return false;
	}

	@Override
	public boolean shouldMinimize() {
		return false;
	}

	public abstract boolean requiresSimulation();

	public abstract void computeObjective(MLMethod method);

	public void setId(int id) {
		this.id = id;
	}

	public int getId(){
		return id;
	}
	
	public String getKey(){
		return this.objectiveKey;
	}

	public void setScore(double d) {
		this.fitness = d;
	}

	public abstract EvaluationFunction getEvaluationFunction();

	public void setKey(String key) {
		this.objectiveKey = key;
	}
}
