package neat.mutations;

import java.io.Serializable;
import java.util.Random;

import neat.continuous.NEATContinuousNeuronGene;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATPopulation;

public class MutatePerturbNeuronDecay implements MutateDecayWeight,Serializable {
	
	private EvolutionaryAlgorithm owner;
	private double sigma;
	
	public MutatePerturbNeuronDecay(double sigma) {
		this.sigma = sigma;
	}

	@Override
	public void init(EvolutionaryAlgorithm theOwner) {
		this.owner = theOwner;
	}

	@Override
	public EvolutionaryAlgorithm getTrainer() {
		return owner;
	}

	@Override
	public void mutateWeight(final Random rnd, final NEATContinuousNeuronGene neuronGene, final double weightRange) {
		double w = neuronGene.getDecay() + rnd.nextGaussian() * this.sigma;
		double b = neuronGene.getBias() + rnd.nextGaussian() * this.sigma;
		w = NEATPopulation.clampWeight(w, weightRange);
		b = NEATPopulation.clampWeight(b, weightRange);
		neuronGene.setDecay(w);
		neuronGene.setBias(b);
	}
}