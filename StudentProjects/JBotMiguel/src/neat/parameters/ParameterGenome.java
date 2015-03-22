package neat.parameters;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import neat.continuous.NEATContinuousGenome;
import neat.continuous.NEATContinuousNeuronGene;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

public class ParameterGenome extends NEATContinuousGenome implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	
	public ParameterGenome(final ParameterGenome other) {
		this.networkDepth = other.networkDepth;
		this.setPopulation(other.getPopulation());
		setScore(other.getScore());
		setAdjustedScore(other.getAdjustedScore());
		this.inputCount = other.inputCount;
		this.outputCount = other.outputCount;
		this.setSpecies(other.getSpecies());

		// copy neurons
		for (final NEATNeuronGene oldGene : other.getNeuronsChromosome()) {
			if(oldGene instanceof ParameterNeuronGene) {
				final ParameterNeuronGene newGene = new ParameterNeuronGene((ParameterNeuronGene)oldGene);
				this.neuronsList.add(newGene);
			} else if(oldGene instanceof NEATContinuousNeuronGene) {
				final NEATNeuronGene newGene = new NEATContinuousNeuronGene((NEATContinuousNeuronGene)oldGene);
				this.neuronsList.add(newGene);
			} else {
				final NEATNeuronGene newGene = new NEATNeuronGene(oldGene);
				this.neuronsList.add(newGene);
			}
		}

		// copy links
		for (final NEATLinkGene oldGene : other.getLinksChromosome()) {
			final NEATLinkGene newGene = new NEATLinkGene(
					oldGene.getFromNeuronID(), oldGene.getToNeuronID(),
					oldGene.isEnabled(), oldGene.getInnovationId(),
					oldGene.getWeight());
			this.linksList.add(newGene);
		}
	}
	
	public ParameterGenome(final List<NEATNeuronGene> neurons,
			final List<NEATLinkGene> links, final int inputCount,
			final int outputCount) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;

		for (NEATLinkGene gene : links) {
			this.linksList.add(new NEATLinkGene(gene));
		}
		
		for(NEATNeuronGene gene : neurons) {
		
			if(gene instanceof ParameterNeuronGene) {
				final ParameterNeuronGene newGene = new ParameterNeuronGene((ParameterNeuronGene)gene);
				this.neuronsList.add(newGene);
			} else if(gene instanceof NEATContinuousNeuronGene) {
				final NEATNeuronGene newGene = new NEATContinuousNeuronGene((NEATContinuousNeuronGene)gene);
				this.neuronsList.add(newGene);
			} else {
				final NEATNeuronGene newGene = new NEATNeuronGene(gene);
				this.neuronsList.add(newGene);
			}
		}
	}

	public ParameterGenome(final Random rnd, final NEATPopulation pop,
			final int inputCount, final int outputCount,
			double connectionDensity) {
		initNoBootstrap(rnd, pop, inputCount, outputCount, connectionDensity);
	}
	
	public ParameterGenome(final Random rnd, final NEATPopulation pop,
			final int inputCount, final int outputCount, double connectionDensity, int bootstrap) {
		init(rnd, pop, inputCount, outputCount, connectionDensity);
	}
	
	protected void init(Random rnd, NEATPopulation pop, int inputCount, int outputCount, double connectionDensity) {
		setAdjustedScore(0);
		setInputCount(inputCount);
		setOutputCount(outputCount);

		// get the activation function
		ActivationFunction af = pop.getActivationFunctions().pickFirst();

		// first bias
		int innovationID = 0;
		NEATNeuronGene biasGene = new NEATNeuronGene(NEATNeuronType.Bias, af,
				inputCount, innovationID++);
		this.getNeuronsChromosome().add(biasGene);

		// then inputs
		for (int i = 0; i < inputCount; i++) {
			NEATNeuronGene gene = new NEATNeuronGene(NEATNeuronType.Input, af,
					i, innovationID++);
			this.getNeuronsChromosome().add(gene);
		}

		// then outputs
		for (int i = 0; i < outputCount; i++) {
			double bias = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			double parameter = RangeRandomizer.randomize(rnd, -pop.getWeightRange(), pop.getWeightRange());
			ParameterNeuronGene gene = new ParameterNeuronGene(NEATNeuronType.Output, af,
					i + inputCount + 1, innovationID++, 0, bias, parameter);
			this.getNeuronsChromosome().add(gene);
		}

		// and now links
		for (int i = 0; i < inputCount + 1; i++) {
			for (int j = 0; j < outputCount; j++) {
				// make sure we have at least one connection
				if (this.getLinksChromosome().size() < 1
						|| rnd.nextDouble() < connectionDensity) {
					long fromID = this.getNeuronsChromosome().get(i).getId();
					long toID = this.getNeuronsChromosome().get(inputCount + j + 1)
							.getId();
					double w = RangeRandomizer.randomize(rnd,
							-pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene gene = new NEATLinkGene(fromID, toID, true,
							innovationID++, w);
					this.getLinksChromosome().add(gene);
				}
			}
		}
	}
	
	public ParameterGenome() {}

}
