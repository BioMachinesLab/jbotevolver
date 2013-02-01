package simulation.ais;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import simulation.util.MathUtils;
import evolutionaryrobotics.neuralnetworks.Chromosome;
import evolutionaryrobotics.populations.ListedPopulation;

public class ClonalSelection <E extends Chromosome> implements AIS<Double, E>{

	protected static final double DECAY = 3.0;
	protected static final int NUMBER_ANTIBODIES = 10;
	protected static final int SELECTION_SIZE = 5;
	protected static final int REPLACEMENT_SIZE = 1;
	protected static final int CLONES_PER_INDIVIDUAL = 10;
	protected int MEMORY_SET_SIZE;
	protected LinkedList<E> antibodies, memorySet;
	protected ListedPopulation<E> population;
	
	public ClonalSelection(int memorySetSize, ListedPopulation<E> population){
		MEMORY_SET_SIZE = memorySetSize;
		antibodies = new LinkedList<E>();
		memorySet = new LinkedList<E>();
		for(int i = 0; i < NUMBER_ANTIBODIES; i++)
			antibodies.add(population.createNewIndividual());
		this.population = population;
	}

	@Override
	public void applyClonalSelection(ListedPopulation<E> pop, Collection<E> antigenes){
		//this.population = pop;
		updateAntibodies();
		for(E gene : antigenes){
			//get the affinity values between the gene and each of the antibodies
			HashMap<E, Double> affinitiesMap = getAffinities(gene, antibodies);
			//select the AgeChromosomes with highest affinity
			HashMap<E, Double> selectedMap = selectHighestAffinity(affinitiesMap);
			//clones the AgeChromosomes with highest affinity
			HashMap<E, ArrayList<E>> clonesMap = getClones(selectedMap);
			//maturity process, hypermutation (affects arraylists in "clonesMap").
			mutateClones(affinitiesMap, clonesMap);
			//pass all the mutants to the same structure, intermediate passage
			Collection<ArrayList<E>> values = clonesMap.values();

			LinkedList<E> mutants = new LinkedList<E>();
			for(ArrayList<E> valuesList : values){
				mutants.addAll(valuesList);
			}
			//mutants to affinity, calculate their affinity
			HashMap<E, Double> mutantsMap = getAffinities(gene, mutants);

			//add the best mutants to the memory set.
			int toPick = Math.round(MEMORY_SET_SIZE/antigenes.size());
			addCurrentBestMutants(mutantsMap, toPick);

			//remove some and add newly initialized AgeChromosomes
			replaceLowest(affinitiesMap);
		}

	}

	@Override
	public void replaceLowest(Map affinitiesMap) {
		List<Object> sortedByValue = MathUtils.sortKeysByValue(affinitiesMap);
		for(int i = 0; i < sortedByValue.size(); i++){
			E c = (E) sortedByValue.get(i);
			this.antibodies.remove(c);
			antibodies.add(population.createNewIndividual());
		}
	}

	private void addCurrentBestMutants(Map mutantsMap, int numberOfMutantsToPick) {
		List<Object> sortedKeysByValue = MathUtils.sortKeysByValue(mutantsMap);
		//top values in the end.
		int lastIndex = sortedKeysByValue.size() - 1;
		for(int i = 0; i < numberOfMutantsToPick; i++){
			E c = (E) sortedKeysByValue.get(lastIndex - i);
			memorySet.add(c);
		}

	}

	private void mutateClones(HashMap<E, Double> affinitiesMap,
			HashMap<E, ArrayList<E>> clonesMap) {
		//iterate over the selected ones, get the affinity value and mutate the clones.
		Iterator<Entry<E, ArrayList<E>>> iterator = clonesMap.entrySet().iterator();
		while(iterator.hasNext()){
			Entry<E, ArrayList<E>> entry = iterator.next();
			E c = entry.getKey();

			double mutationRate = getMutationRate(affinitiesMap.get(c));
			ArrayList<E> currentClones = clonesMap.get(c);
			for(E clone : currentClones){
				mutateIndividual(clone, mutationRate);
			}
		}
	}

	private HashMap<E, ArrayList<E>> getClones(HashMap<E, Double> selectedMap) {

		double totalAffinity = 0;
		Collection<Double> affinities = selectedMap.values();
		for(Double d : affinities)
			totalAffinity += d.doubleValue();

				HashMap<E, ArrayList<E>> clonesMap = new HashMap<E, ArrayList<E>>();
				Iterator<E> keys = selectedMap.keySet().iterator();
				while(keys.hasNext()){
					E c = keys.next();
					//number of clones proportional to the affinity value.
					int numberCurrentClones = (int) Math.round(selectedMap.get(c) * CLONES_PER_INDIVIDUAL / totalAffinity);
					ArrayList<E> currentClones = clone(c, numberCurrentClones);
					clonesMap.put(c, currentClones);
				}

				return clonesMap;
	}

	private HashMap<E, Double> selectHighestAffinity(Map map) {
		List<Object> sortedByValue = MathUtils.sortKeysByValue(map);
		
		HashMap<E, Double> highestMap = new HashMap<E, Double>(SELECTION_SIZE);
		int lastIndex = sortedByValue.size() - 1;
		//the highest are in the end.
		for(int i = 0; i < SELECTION_SIZE; i++){
			E c = (E) sortedByValue.get(lastIndex - i);
			Double affinity = (Double) map.get(c);
			highestMap.put(c, affinity);
		}
		return highestMap;
	}

	private HashMap<E, Double> getAffinities(E nextGene, Collection<E> list ) {
		double[] affinities = new double[list.size()];
		double maxValue = Math.sqrt(Math.pow(20, 2) * nextGene.getAlleles().length), minValue = 0;
		int pos = 0;
		for(E ch : list){
			//affinities[pos] = (1 + MathUtils.calculateCosineSimilarity(ch.getAlleles(), nextGene.getAlleles()))/2;
			affinities[pos] = Math.abs(MathUtils.calculateCosineSimilarity(ch.getAlleles(), nextGene.getAlleles()));
			
			//affinities[pos] = MathUtils.calculateSquaredDifferences(ch.getAlleles(), nextGene.getAlleles());
			pos++;
		}
		//MathUtils.normalizeAndInvertValues(affinities, maxValue, minValue);

		HashMap<E, Double> result = new HashMap<E, Double>(affinities.length);
		pos = 0;
		for(E ch: list){
			result.put(ch, affinities[pos++]);
		}

		return result;
	}

	private void updateAntibodies() {
		for(E c : memorySet){
			antibodies.add(c);
		}
		memorySet.clear();
		//Iterator<E> it = population.iterator();
		/*while(it.hasNext()){
			antibodies.add(it.next());
		}*/
	}


	@Override
	public ArrayList<E> clone(E antibody, int cloningFactor) {
		ArrayList<E> clones = new ArrayList<E>(cloningFactor);
		for(int i = 0; i < cloningFactor; i++)
			try {
				clones.add((E) antibody.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		return clones;
	}

	@Override
	public void mutate(Collection <E> clones, 
			Double affinity) {
		double mutRate = getMutationRate(affinity);
		for(E c : clones){
			mutateIndividual(c, mutRate);
		}
	}

	private void mutateIndividual(E toMutate, double mutRate) {
		population.mutateWithRate(toMutate, mutRate);	
	}

	private double getMutationRate(double affinity){
		return Math.pow(Math.E, (-DECAY*affinity));
	}

	public Collection<E> getMemorySet() {
		return this.memorySet;
	}

	public void setMemorySetSize(int size) {
		this.MEMORY_SET_SIZE = size;	
	}

}
