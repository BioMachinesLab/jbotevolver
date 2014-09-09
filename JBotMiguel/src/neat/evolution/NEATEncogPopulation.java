package neat.evolution;

import java.io.Serializable;
import java.util.Random;

import neat.continuous.FactorNEATContinuousGenome;
import neat.continuous.NEATContinuousCODEC;

import org.encog.ml.MLError;
import org.encog.ml.MLRegression;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.species.BasicSpecies;
import org.encog.neural.hyperneat.FactorHyperNEATGenome;
import org.encog.neural.hyperneat.HyperNEATCODEC;
import org.encog.neural.hyperneat.substrate.Substrate;
import org.encog.neural.neat.FactorNEATGenome;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATInnovationList;

/**
 * This class is necessary because the way the networks are initialized
 * breaks parallel evaluation of networks. They cannot all share the same
 * random number generator.
 * 
 * @author miguelduarte42
 */
public class NEATEncogPopulation extends NEATPopulation implements Serializable, MLError, MLRegression {
	
	private boolean continuous = false;;

	public NEATEncogPopulation() {

	}

	public NEATEncogPopulation(final int inputCount, final int outputCount, final int populationSize) {
		super(inputCount, outputCount, populationSize);
	}
	public NEATEncogPopulation(final int inputCount, final int outputCount, final int populationSize, boolean continuous) {
		this(inputCount, outputCount, populationSize);
		this.continuous = continuous;
	}

	public NEATEncogPopulation(final Substrate theSubstrate, final int populationSize) {
		super(theSubstrate,populationSize);
	}

	public void reset() {
		// create the genome factory
		if (isHyperNEAT()) {
			setCODEC(new HyperNEATCODEC());
			setGenomeFactory(new FactorHyperNEATGenome());
		} else if(continuous){
			setCODEC(new NEATContinuousCODEC());
			setGenomeFactory(new FactorNEATContinuousGenome());
		} else {
			setCODEC(new NEATCODEC());
			setGenomeFactory(new FactorNEATGenome());
		}

		// create the new genomes
		getSpecies().clear();

		// reset counters
		getGeneIDGenerate().setCurrentID(1);
		getInnovationIDGenerate().setCurrentID(1);

		final Random rnd = getRandomNumberFactory().factor();

		// create one default species
		final BasicSpecies defaultSpecies = new BasicSpecies();
		defaultSpecies.setPopulation(this);

		// create the initial population
		for (int i = 0; i < getPopulationSize(); i++) {
			final Genome genome = getGenomeFactory().factor(new Random(rnd.nextLong()), this,
					getInputCount(), getOutputCount(),
					getInitialConnectionDensity());
			defaultSpecies.add(genome);
		}
		defaultSpecies.setLeader(defaultSpecies.getMembers().get(0));
		getSpecies().add(defaultSpecies);

		// create initial innovations
		setInnovations(new NEATInnovationList(this));
	}

}
