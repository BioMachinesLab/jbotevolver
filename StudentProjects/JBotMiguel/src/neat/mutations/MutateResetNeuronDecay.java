package neat.mutations;

import java.io.Serializable;
import java.util.Random;

import neat.continuous.NEATContinuousNeuronGene;

import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;

/**
 * Mutate weight links by reseting the weight to an entirely new value. The
 * weight range will come from the trainer.
 * 
 * -----------------------------------------------------------------------------
 * http://www.cs.ucf.edu/~kstanley/ Encog's NEAT implementation was drawn from
 * the following three Journal Articles. For more complete BibTeX sources, see
 * NEATNetwork.java.
 * 
 * Evolving Neural Networks Through Augmenting Topologies
 * 
 * Generating Large-Scale Neural Networks Through Discovering Geometric
 * Regularities
 * 
 * Automatic feature selection in neuroevolution
 */
public class MutateResetNeuronDecay implements MutateDecayWeight,Serializable {

	/**
	 * The trainer being used.
	 */
	private EvolutionaryAlgorithm trainer;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EvolutionaryAlgorithm getTrainer() {
		return this.trainer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final EvolutionaryAlgorithm theTrainer) {
		this.trainer = theTrainer;
	}

	@Override
	public void mutateWeight(Random rnd, NEATContinuousNeuronGene neuronGene, double weightRange) {
		neuronGene.setDecay(RangeRandomizer.randomize(rnd, -weightRange, weightRange));
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(this.getClass().getSimpleName());
		result.append("]");
		return result.toString();
	}

}

