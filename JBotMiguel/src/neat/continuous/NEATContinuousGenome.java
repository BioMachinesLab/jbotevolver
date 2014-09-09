package neat.continuous;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.encog.EncogError;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.util.Format;

public class NEATContinuousGenome extends NEATGenome implements Cloneable, Serializable {

	/**
	 * Serial id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The number of inputs.
	 */
	private int inputCount;

	/**
	 * The list that holds the links.
	 */
	private final List<NEATLinkGene> linksList = new ArrayList<NEATLinkGene>();

	/**
	 * THe network depth.
	 */
	private int networkDepth;

	/**
	 * The list that holds the neurons.
	 */
	private final List<NEATNeuronGene> neuronsList = new ArrayList<NEATNeuronGene>();

	/**
	 * The number of outputs.
	 */
	private int outputCount;

	/**
	 * Construct a genome by copying another.
	 * 
	 * @param other
	 *            The other genome.
	 */
	public NEATContinuousGenome(final NEATContinuousGenome other) {
		this.networkDepth = other.networkDepth;
		this.setPopulation(other.getPopulation());
		setScore(other.getScore());
		setAdjustedScore(other.getAdjustedScore());
		this.inputCount = other.inputCount;
		this.outputCount = other.outputCount;
		this.setSpecies(other.getSpecies());

		// copy neurons
		for (final NEATNeuronGene oldGene : other.getNeuronsChromosome()) {
			final NEATNeuronGene newGene = new NEATNeuronGene(oldGene);
			this.neuronsList.add(newGene);
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

	/**
	 * Create a NEAT gnome. Neuron genes will be added by reference, links will
	 * be copied.
	 * 
	 * @param neurons
	 *            The neurons to create.
	 * @param links
	 *            The links to create.
	 * @param inputCount
	 *            The input count.
	 * @param outputCount
	 *            The output count.
	 */
	public NEATContinuousGenome(final List<NEATNeuronGene> neurons,
			final List<NEATLinkGene> links, final int inputCount,
			final int outputCount) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;

		for (NEATLinkGene gene : links) {
			this.linksList.add(new NEATLinkGene(gene));
		}

		this.neuronsList.addAll(neurons);
	}

	/**
	 * Create a new genome with the specified connection density. This
	 * constructor is typically used to create the initial population.
	 * @param rnd Random number generator.
	 * @param pop The population.
	 * @param inputCount The input count.
	 * @param outputCount The output count.
	 * @param connectionDensity The connection density.
	 */
	public NEATContinuousGenome(final Random rnd, final NEATPopulation pop,
			final int inputCount, final int outputCount,
			double connectionDensity) {
		setAdjustedScore(0);
		this.inputCount = inputCount;
		this.outputCount = outputCount;

		// get the activation function
		ActivationFunction af = pop.getActivationFunctions().pickFirst();

		// first bias
		int innovationID = 0;
		NEATNeuronGene biasGene = new NEATNeuronGene(NEATNeuronType.Bias, af,
				inputCount, innovationID++);
		this.neuronsList.add(biasGene);

		// then inputs

		for (int i = 0; i < inputCount; i++) {
			NEATNeuronGene gene = new NEATNeuronGene(NEATNeuronType.Input, af,
					i, innovationID++);
			this.neuronsList.add(gene);
		}

		// then outputs

		for (int i = 0; i < outputCount; i++) {
			NEATNeuronGene gene = new NEATNeuronGene(NEATNeuronType.Output, af,
					i + inputCount + 1, innovationID++);
			this.neuronsList.add(gene);
		}

		// and now links
		for (int i = 0; i < inputCount + 1; i++) {
			for (int j = 0; j < outputCount; j++) {
				// make sure we have at least one connection
				if (this.linksList.size() < 1
						|| rnd.nextDouble() < connectionDensity) {
					long fromID = this.neuronsList.get(i).getId();
					long toID = this.neuronsList.get(inputCount + j + 1)
							.getId();
					double w = RangeRandomizer.randomize(rnd,
							-pop.getWeightRange(), pop.getWeightRange());
					NEATLinkGene gene = new NEATLinkGene(fromID, toID, true,
							innovationID++, w);
					this.linksList.add(gene);
				}
			}
		}

	}

	public NEATContinuousGenome() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return The number of input neurons.
	 */
	public int getInputCount() {
		return this.inputCount;
	}

	/**
	 * @return The network depth.
	 */
	public int getNetworkDepth() {
		return this.networkDepth;
	}

	/**
	 * @return The number of genes in the links chromosome.
	 */
	public int getNumGenes() {
		return this.linksList.size();
	}

	/**
	 * @return The output count.
	 */
	public int getOutputCount() {
		return this.outputCount;
	}

	/**
	 * @param networkDepth
	 *            the networkDepth to set
	 */
	public void setNetworkDepth(final int networkDepth) {
		this.networkDepth = networkDepth;
	}

	/**
	 * Sort the genes.
	 */
	public void sortGenes() {
		Collections.sort(this.linksList);
	}

	/**
	 * @return the linksChromosome
	 */
	public List<NEATLinkGene> getLinksChromosome() {
		return this.linksList;
	}

	/**
	 * @return the neuronsChromosome
	 */
	public List<NEATNeuronGene> getNeuronsChromosome() {
		return this.neuronsList;
	}

	/**
	 * @param inputCount
	 *            the inputCount to set
	 */
	public void setInputCount(int inputCount) {
		this.inputCount = inputCount;
	}

	/**
	 * @param outputCount
	 *            the outputCount to set
	 */
	public void setOutputCount(int outputCount) {
		this.outputCount = outputCount;
	}

	/**
	 * Validate the structure of this genome.
	 */
	public void validate() {

		// make sure that the bias neuron is where it should be
		NEATNeuronGene g = this.neuronsList.get(0);
		if (g.getNeuronType() != NEATNeuronType.Bias) {
			throw new EncogError("NEAT Neuron Gene 0 should be a bias gene.");
		}

		// make sure all input neurons are at the beginning
		for (int i = 1; i <= this.inputCount; i++) {
			NEATNeuronGene gene = this.neuronsList.get(i);
			if (gene.getNeuronType() != NEATNeuronType.Input) {
				throw new EncogError("NEAT Neuron Gene " + i
						+ " should be an input gene.");
			}
		}

		// make sure that there are no double links
		Map<String, NEATLinkGene> map = new HashMap<String, NEATLinkGene>();
		for (NEATLinkGene nlg : this.linksList) {
			String key = nlg.getFromNeuronID() + "->" + nlg.getToNeuronID();
			if (map.containsKey(key)) {
				throw new EncogError("Double link found: " + key);
			}
			map.put(key, nlg);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void copy(Genome source) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Find the neuron with the specified nodeID.
	 * 
	 * @param nodeID
	 *            The nodeID to look for.
	 * @return The neuron, if found, otherwise null.
	 */
	public NEATNeuronGene findNeuron(long nodeID) {
		for (NEATNeuronGene gene : this.neuronsList) {
			if (gene.getId() == nodeID)
				return gene;
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(this.getClass().getSimpleName());
		result.append(",score=");
		result.append(Format.formatDouble(this.getScore(), 2));
		result.append(",adjusted score=");
		result.append(Format.formatDouble(this.getAdjustedScore(), 2));
		result.append(",birth generation=");
		result.append(this.getBirthGeneration());
		result.append(",neurons=");
		result.append(this.neuronsList.size());
		result.append(",links=");
		result.append(this.linksList.size());
		result.append("]");
		return result.toString();
	}
}
