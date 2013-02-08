package evolutionaryrobotics.evolutionarystrategies;

import java.util.List;

import simulation.util.SimRandom;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class MuPlusLambdaES <E extends Chromosome> extends MuLambdaES<E>{

	public MuPlusLambdaES(SimRandom random, short mu, short lambda,
			double mutationRate) {
		super(random, mu, lambda, mutationRate);
	}

	@Override
	public List<E> applyStrategy(List<E> population){
		List<E> newPop = selectParents(population);
		for(int i = 0; i < mu; i++){
			//breed each parent
			newPop.addAll(breed(newPop.get(i)));
		}			
		return newPop;
	}
}
