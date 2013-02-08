package evolutionaryrobotics.evolutionarystrategies;

import java.util.Collection;
import java.util.List;

public interface ESInterface <E>{
	
	List<E> selectParents(List<E> population);
	
	List<E> applyStrategy(List<E> population);
	
	Collection<E> breed(E parent);
	
	E mutate(E parent);
	
}
