package neat.mutations;
/*
 * Encog(tm) Core v3.2 - Java Version
 * http://www.heatonresearch.com/encog/
 * https://github.com/encog/encog-java-core
 
 * Copyright 2008-2013 Heaton Research, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *   
 * For more information on Heaton Research copyrights, licenses 
 * and trademarks visit:
 * http://www.heatonresearch.com/copyright
 */
import java.io.Serializable;
import java.util.Random;

import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATMutateAddLink;

 
public class NEATMutateAddLinkJBot extends NEATMutateAddLink implements Serializable {

	public void performOperation(Random rnd, Genome[] parents, int parentIndex,
			Genome[] offspring, int offspringIndex) {
		int countTrysToAddLink = this.getOwner().getMaxTries();

		NEATGenome target = this.obtainGenome(parents, parentIndex, offspring,
				offspringIndex);

		// the link will be between these two neurons
		long neuron1ID = -1;
		long neuron2ID = -1;

		// try to add a link
		while ((countTrysToAddLink--) > 0) {
			final NEATNeuronGene neuron1 = chooseRandomNeuron(target, true, rnd);
			final NEATNeuronGene neuron2 = chooseRandomNeuron(target, false, rnd);

			if (neuron1 == null || neuron2 == null) {
				return;
			}

			// do not duplicate
			// do not go to a bias neuron
			// do not go from an output neuron
			// do not go to an input neuron
			if (!isDuplicateLink(target, neuron1.getId(), neuron2.getId())
					&& (neuron2.getNeuronType() != NEATNeuronType.Bias)
					&& (neuron2.getNeuronType() != NEATNeuronType.Input)) {

				if ( ((NEATPopulation)getOwner().getPopulation()).getActivationCycles() != 1
						|| neuron1.getNeuronType() != NEATNeuronType.Output) {
					neuron1ID = neuron1.getId();
					neuron2ID = neuron2.getId();
					break;
				}
			}
		}

		// did we fail to find a link
		if ((neuron1ID < 0) || (neuron2ID < 0)) {
			return;
		}

		double r = ((NEATPopulation) target.getPopulation()).getWeightRange();
		createLink(target, neuron1ID, neuron2ID,
				RangeRandomizer.randomize(rnd, -r, r));
		target.sortGenes();
	}
	
	public NEATNeuronGene chooseRandomNeuron(final NEATGenome target, final boolean choosingFrom, Random rnd) {
		int start;

		if (choosingFrom) {
			start = 0;
		} else {
			start = target.getInputCount() + 1;
		}

		// if this network will not "cycle" then output neurons cannot be source
		// neurons
		if (!choosingFrom) {
			final int ac = ((NEATPopulation) target.getPopulation())
					.getActivationCycles();
			if (ac == 1) {
				start += target.getOutputCount();
			}
		}

		final int end = target.getNeuronsChromosome().size() - 1;

		// no neurons to pick!
		if (start > end) {
			return null;
		}

		/**
		 * Changed here to use our own randomizer
		 * @miguelduarte42
		 */
		final int neuronPos = (int) RangeRandomizer.randomize(rnd, start, end + 1);
		final NEATNeuronGene neuronGene = target.getNeuronsChromosome().get(
				neuronPos);
		return neuronGene;

	}

}
