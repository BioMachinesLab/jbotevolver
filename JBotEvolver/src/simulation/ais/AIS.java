package simulation.ais;

import java.util.Collection;
import java.util.Map;

import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.ListedPopulation;

public interface AIS <E, T extends Chromosome>{

	void applyClonalSelection(ListedPopulation <T> antibodies, Collection<T> antigenes);
	
	Collection<T> clone(T antibody, int cloneFactor);
		
	void mutate(Collection<T> clonesAntibodies, E affinity);
	
	void replaceLowest(Map<T,E> map);
}
