package neat.continuous;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATMutation;

public class NEATMutateNeuronDecays extends NEATMutation implements Serializable {
	/**
	 * The method used to select the links to mutate.
	 */
	private final SelectContinuousNeurons neuronSelection;

	/**
	 * The method used to mutate the selected links.
	 */
	private final MutateDecayWeight weightMutation;

	public NEATMutateNeuronDecays(final SelectContinuousNeurons theNeuronsSelection, final MutateDecayWeight theWeightMutation) {
		this.neuronSelection = theNeuronsSelection;
		this.weightMutation = theWeightMutation;
	}

	/**
	 * @return The method used to select neurons for mutation.
	 */
	public SelectContinuousNeurons getNeuronsSelection() {
		return this.neuronSelection;
	}

	/**
	 * @return The method used to mutate the weights.
	 */
	public MutateDecayWeight getWeightMutation() {
		return this.weightMutation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performOperation(final Random rnd, final Genome[] parents,
			final int parentIndex, final Genome[] offspring, final int offspringIndex) {
		final NEATGenome target = obtainGenome(parents, parentIndex, offspring, offspringIndex);
		final double weightRange = ((NEATPopulation)getOwner().getPopulation()).getWeightRange();
		final List<NEATContinuousNeuronGene> list = this.neuronSelection.selectNeurons(rnd, target);
		for (final NEATContinuousNeuronGene gene : list) {
			this.weightMutation.mutateWeight(rnd, gene, weightRange);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(this.getClass().getSimpleName());
		result.append(":sel=");
		result.append(this.neuronSelection.toString());
		result.append(",mutate=");
		result.append(this.weightMutation.toString());
		result.append("]");
		return result.toString();
	}
}