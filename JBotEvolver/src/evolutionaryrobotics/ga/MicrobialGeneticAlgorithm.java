package evolutionaryrobotics.ga;

import java.util.ArrayList;
import java.util.List;

import simulation.util.SimRandom;
import evolutionaryrobotics.neuralnetworks.Chromosome;

public class MicrobialGeneticAlgorithm <E extends Chromosome> implements GeneticInterface<E>{

	protected final short SELECTION_SIZE = 2, LOCALITY = 5;
	protected SimRandom random;
	protected double mutationProb, recombinationRate;

	public MicrobialGeneticAlgorithm(SimRandom random, double mutationProb, double recombinationRate){
		this.random = random;
		this.mutationProb = mutationProb;
		this.recombinationRate = recombinationRate;
	}

	@Override
	public List<E> performSelection(List<E> elements){
		List<E> selected = new ArrayList<E>(SELECTION_SIZE);
		int firstIndex = random.nextInt(elements.size());
		selected.add(elements.get(firstIndex));

		while(selected.size() < SELECTION_SIZE){	
			int secondIndex = (firstIndex + random.nextInt(LOCALITY*2) - LOCALITY) % elements.size();
			if(secondIndex != firstIndex && secondIndex >= 0)
				selected.add(elements.get(secondIndex));
		}
		return selected;
	}

	@Override
	public E recombine(E e1, E e2) {
		//the tournament determines the winner.
		E winner = binaryTournament(e1, e2);
		E loser = winner == e1 ? e2 : e1;
		double[] winnerAlleles = winner.getAlleles(), loserAlleles = loser.getAlleles();
		for(int i = 0; i < winnerAlleles.length; i++){
			if(random.nextDouble() < recombinationRate){
				loserAlleles[i] = winnerAlleles[i];
			}
		}
		return loser;
	}

	protected E binaryTournament(E e1, E e2) {
		if(e1.getFitness() >= e2.getFitness()){
			return e1;
		}
		else
			return e2;
	}

	@Override
	public void mutate(E element) {
		double[] alleles = element.getAlleles();
		for (int j = 0; j < alleles.length; j++) {         
			double allele = alleles[j];
			if (random.nextDouble() < mutationProb) {
				allele = allele + random.nextGaussian();
				if (allele < -10)
					allele = -10;
				if (allele > 10)
					allele = 10;
				alleles[j] = allele; 		          
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> performEvolution(int newPopulationSize, List<E> currentPopulation){
		//System.out.println("POP SIZE @ AMG:" + currentPopulation.getAll().size());
		ArrayList<E> newpopulation = new ArrayList<E>(newPopulationSize);
		for(int i = 0; i < newPopulationSize; i++){
			List<E> selected = performSelection(currentPopulation);
			E recombined = null;
			try {
				recombined = recombine((E)selected.get(0).clone(), (E)selected.get(1).clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			mutate(recombined);
			newpopulation.add(recombined);
		}
		return newpopulation;
	}

}
