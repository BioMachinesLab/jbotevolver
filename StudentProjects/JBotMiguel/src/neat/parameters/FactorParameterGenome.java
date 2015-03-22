package neat.parameters;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import neat.continuous.FactorNEATContinuousGenome;
import neat.continuous.NEATContinuousGenome;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

public class FactorParameterGenome extends FactorNEATContinuousGenome implements Serializable{
	
	private static final long serialVersionUID = 856389357122003004L;
	protected int bootstrap;
	
	@Override
	public NEATGenome factor() {
		return new ParameterGenome();
	}
	
	public void setBootstrap(int bootstrap) {
		this.bootstrap = bootstrap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Genome factor(final Genome other) {
		return new ParameterGenome((ParameterGenome) other);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NEATGenome factor(final List<NEATNeuronGene> neurons,
			final List<NEATLinkGene> links, final int inputCount,
			final int outputCount) {
		return new ParameterGenome(neurons, links, inputCount, outputCount);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public NEATGenome factor(final Random rnd, final NEATPopulation pop,
			final int inputCount, final int outputCount,
			final double connectionDensity) {
		return new ParameterGenome(rnd, pop, inputCount, outputCount, connectionDensity, bootstrap);
	}

}