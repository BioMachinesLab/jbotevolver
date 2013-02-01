package evolutionaryrobotics.populations;

import java.util.ArrayList;
import java.util.Iterator;

import evolutionaryrobotics.neuralnetworks.Chromosome;

import simulation.util.SimRandom;

public abstract class ListedPopulation <E extends Chromosome>implements Cloneable{

	protected ArrayList<E> population;
	protected int populationSize, currentId = 0, chromosomeLength;

	protected SimRandom random;

	public ListedPopulation(SimRandom random, int populationSize, int chromosomeLength){
		population = new ArrayList<E>();
		this.random = random;
		this.populationSize = populationSize;
		this.chromosomeLength = chromosomeLength;
	}

	public void initializeRandomPopulation(){
		for(int i = 0; i < populationSize; i++){
			E c = createNewIndividual();
			c.setFitness(0);
			population.add(c);
		}
	}

	public abstract E createNewIndividual();

	public int createdSoFar(){
		return currentId;
	}

	public E getRandomIndividual() {
		return population.get(random.nextInt(population.size()));
	}

	public ArrayList<E> getAll() {
		return population;
	}

	@SuppressWarnings("unchecked")
	public ListedPopulation<E> clone() throws CloneNotSupportedException{
		ListedPopulation<E> pop = (ListedPopulation<E>) super.clone();
		pop.population = new ArrayList<E>();
		for(E c : population)
			pop.population.add((E) c.clone());

		return pop;
	}

	public abstract void mutateWithRate(E toMutate, double mutRate);

	public Iterator<E> iterator() {
		return this.population.iterator();
	}

	public void setPopulation(ArrayList<E> generated) {
		this.population = generated;
	}

	public int size() {
		return population.size();
	}

	public E get(int index) {
		return population.get(index);
	}

	public void addElement(E a) {
		population.add(a);
	}

	public void remove(E a) {
		population.remove(a);
	}

}
