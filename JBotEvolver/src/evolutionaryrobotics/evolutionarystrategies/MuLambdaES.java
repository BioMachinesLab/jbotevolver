package evolutionaryrobotics.evolutionarystrategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import simulation.util.SimRandom;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class MuLambdaES <E extends Chromosome> implements ESInterface<E>{

	protected short mu, lambda;
	protected SimRandom random;
	protected double mutationRate;

	public MuLambdaES(SimRandom random, short mu, short lambda, double mutationRate){
		this.random = random;
		this.mu = mu;
		this.lambda = lambda;
		this.mutationRate = mutationRate;
	}
	
	@Override
	public List<E> selectParents(List<E> population){
		List<E> newPop = new ArrayList<E>(mu*lambda);
		sortInDescendingOrder(population);
		//pick the parents
		for(int i = 0; i < mu; i++)
			newPop.add(population.get(i));
		return newPop;
	}

	public List<E> applyStrategy(List<E> population, short mu, short lambda){
		setLambda(lambda);
		setMu(mu);
		return applyStrategy(population);
	}
	
	@Override
	public List<E> applyStrategy(List<E> population){
		List<E> newPop = selectParents(population);
		for(int i = 0; i < mu; i++){
			//breed each parent
			newPop.addAll(breed(newPop.get(i)));
		}
		
		//and remove them from the list
		for(int i = 0; i < mu; i++)
			newPop.remove(0);
				
		return newPop;
	}

	@Override
	public Collection<E> breed(E parent) {
		List<E> children = new ArrayList<E>(lambda);
		for(int i = 0; i < lambda; i++)
			children.add(mutate(parent));
		
		return children;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E mutate(E parent) {
		double[] parentAlleles = parent.getAlleles();
    	double[] newAlleles = new double[parentAlleles.length];
    	//cannot instantiate generic types, must clone...
    	E child = null;
    	try {
			 child = (E) parent.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
    	for (int i = 0; i < newAlleles.length; i++) {        
            double allele = parentAlleles[i]; 
            if (random.nextDouble() < mutationRate) {
            	allele = allele + random.nextGaussian();
                if (allele < -10)
                	allele = -10;
                if (allele > 10)
                	allele = 10;
            }
            
            newAlleles[i] = allele;
        }
    	child.setAlleles(newAlleles);
    	
    	return child;
	}

	private void sortInDescendingOrder(List<E> population) {
		Collections.sort(population, new Comparator<E>(){
			@Override
			public int compare(E e1, E e2) {
				double v1 = e1.getFitness();
				double v2 = e2.getFitness();
				//larger values go first
				if(v1 > v2)
					return -1;
				else if( v1 == v2)
					return 0;
				return 1;
			}

		});
		
	}

	protected void setMu(short newMu){
		this.mu = newMu;
	}

	protected void setLambda(short newLambda){
		this.lambda = newLambda;
	}
	
	public void setMutationRate(double newRate){
		this.mutationRate = newRate;
	}

}
