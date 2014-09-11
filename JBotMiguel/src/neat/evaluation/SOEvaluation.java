package neat.evaluation;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.encog.ml.CalculateScore;

import evolutionaryrobotics.JBotEvolver;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;
import simulation.util.Arguments;
import taskexecutor.TaskExecutor;

public abstract class SOEvaluation<E> extends NEATEvaluation implements CalculateScore, CalculateScoreAsynchronous,Serializable {

	public SOEvaluation(Arguments args) {
		super(args);
	}
}
