package neat.evaluation;

import java.io.Serializable;
import org.encog.ml.CalculateScore;
import simulation.util.Arguments;

public abstract class MOEvaluation<E> extends NEATEvaluation implements CalculateScore, Serializable {

	protected int numberOfObjectives;
	
	public MOEvaluation(Arguments args) {
		super(args);
	}
	
	public int getNumberOfObjectives() {
		return numberOfObjectives;
	}
	
	public abstract String getObjectiveKey(int id);
	
}
