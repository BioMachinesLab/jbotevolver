package simulation.ga;

import java.util.List;

public interface GeneticInterface <E>{
	
	List<E> performSelection(List<E> population);

	E recombine(E e1, E e2);
	
	void mutate(E element);
	
	List<E> performEvolution(int newPopulationSize, List<E> currentPopulation);
}
