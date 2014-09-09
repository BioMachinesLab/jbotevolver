package neat.continuous;

import java.io.Serializable;
import java.util.Random;

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
		final double delta = rnd.nextGaussian() * this.sigma;
		double w = neuronGene.getDecay() + delta;
		w = NEATPopulation.clampWeight(w, weightRange);
		neuronGene.setDecay(w);
	}
}