package evaluationfunctions.flocking.metrics;

import simulation.Simulator;
import simulation.util.Arguments;
import evolutionaryrobotics.evaluationfunctions.EvaluationFunction;

public class splitProbability  extends EvaluationFunction {

	protected Simulator simulator;
	public splitProbability(Arguments args) {
		super(args);	
	}
	
	@Override
	public double getFitness() {
		int Ng=1; //argumento
		if(Ng<2){
			return 0;
		}else{
			return 1;
		}
	}
	

	@Override
	public void update(Simulator simulator) {	
		
	}
	

}