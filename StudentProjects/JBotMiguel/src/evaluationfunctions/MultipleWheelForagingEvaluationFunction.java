package evaluationfunctions;

import simulation.Simulator;
import simulation.util.Arguments;
import environments.ForageEnvironment;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class MultipleWheelForagingEvaluationFunction extends EvaluationFunction{
	
	protected int numberOfFoodForaged = 0;

	public MultipleWheelForagingEvaluationFunction(Arguments args) {
		super(args);	
	}

	//@Override
	public void update(Simulator simulator) {			
		numberOfFoodForaged = ((ForageEnvironment)(simulator.getEnvironment())).getNumberOfFoodSuccessfullyForaged();
	}
	
	@Override
	public double getFitness() {
		return Math.max(0,numberOfFoodForaged);
	}
}