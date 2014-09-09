package neat.continuous;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.FactorNEATGenome;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

public class FactorNEATContinuousGenome extends FactorNEATGenome implements Serializable{
	
	@Override
	public NEATGenome factor() {
		return new NEATContinuousGenome();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Genome factor(final Genome other) {
		return new NEATContinuousGenome((NEATContinuousGenome) other);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NEATGenome factor(final List<NEATNeuronGene> neurons,
			final List<NEATLinkGene> links, final int inputCount,
			final int outputCount) {
		return new NEATContinuousGenome(neurons, links, inputCount, outputCount);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NEATGenome factor(final Random rnd, final NEATPopulation pop,
			final int inputCount, final int outputCount,
			final double connectionDensity) {
		return new NEATContinuousGenome(rnd, pop, inputCount, outputCount,
				connectionDensity);
	}

}
