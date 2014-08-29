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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.encog.ml.ea.genome.Genome;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.NEATGenomeFactory;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATCrossover;

public class NEATCrossoverJBot extends NEATCrossover {

	/**
	 * The owning object.
	 */
	private EvolutionaryAlgorithm owner;

	

	/**
	 * Choose a parent to favor.
	 * 
	 * @param mom
	 *            The mother.
	 * @param dad
	 *            The father.
	 * @return The parent to favor.
	 */
	private NEATGenome favorParent(final NEATGenome mom, final NEATGenome dad, Random rnd) {

		// first determine who is more fit, the mother or the father?
		// see if mom and dad are the same fitness
		if (mom.getScore() == dad.getScore()) {
			// are mom and dad the same fitness
			if (mom.getNumGenes() == dad.getNumGenes()) {
				// if mom and dad are the same fitness and have the same number
				// of genes,
				// then randomly pick mom or dad as the most fit.
				if (rnd.nextFloat() > 0.5f) {
					return mom;
				} else {
					return dad;
				}
			}
			// mom and dad are the same fitness, but different number of genes
			// favor the parent with fewer genes
			else {
				if (mom.getNumGenes() < dad.getNumGenes()) {
					return mom;
				} else {
					return dad;
				}
			}
		} else {
			// mom and dad have different scores, so choose the better score.
			// important to note, better score COULD BE the larger or smaller
			// score.
			if (this.owner.getSelectionComparator().compare(mom, dad) < 0) {
				return mom;
			}

			else {
				return dad;
			}
		}

	}

	/**
	 * Init this operator. This allows the EA to be defined.
	 */
	@Override
	public void init(final EvolutionaryAlgorithm theOwner) {
		//TODO
		super.init(theOwner);
		this.owner = theOwner;
	}

	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void performOperation(final Random rnd, final Genome[] parents,
			final int parentIndex, final Genome[] offspring,
			final int offspringIndex) {

		final NEATGenome mom = (NEATGenome) parents[parentIndex + 0];
		final NEATGenome dad = (NEATGenome) parents[parentIndex + 1];

		final NEATGenome best = favorParent(mom, dad, rnd);
		final NEATGenome notBest = (best == mom) ? mom : dad;

		final List<NEATLinkGene> selectedLinks = new ArrayList<NEATLinkGene>();
		final List<NEATNeuronGene> selectedNeurons = new ArrayList<NEATNeuronGene>();

		int curMom = 0; // current gene index from mom
		int curDad = 0; // current gene index from dad
		NEATLinkGene selectedGene = null;

		// add in the input and bias, they should always be here
		final int alwaysCount = ((NEATGenome)parents[0]).getInputCount()
				+ ((NEATGenome)parents[0]).getOutputCount() + 1;
		for (int i = 0; i < alwaysCount; i++) {
			addNeuronID(i, selectedNeurons, best, notBest);
		}

		while ((curMom < mom.getNumGenes()) || (curDad < dad.getNumGenes())) {
			NEATLinkGene momGene = null; // the mom gene object
			NEATLinkGene dadGene = null; // the dad gene object
			long momInnovation = -1;
			long dadInnovation = -1;

			// grab the actual objects from mom and dad for the specified
			// indexes
			// if there are none, then null
			if (curMom < mom.getNumGenes()) {
				momGene = mom.getLinksChromosome().get(curMom);
				momInnovation = momGene.getInnovationId();
			}

			if (curDad < dad.getNumGenes()) {
				dadGene = dad.getLinksChromosome().get(curDad);
				dadInnovation = dadGene.getInnovationId();
			}

			// now select a gene for mom or dad. This gene is for the baby
			if ((momGene == null) && (dadGene != null)) {
				if (best == dad) {
					selectedGene = dadGene;
				}
				curDad++;
			} else if ((dadGene == null) && (momGene != null)) {
				if (best == mom) {
					selectedGene = momGene;
				}
				curMom++;
			} else if (momInnovation < dadInnovation) {
				if (best == mom) {
					selectedGene = momGene;
				}
				curMom++;
			} else if (dadInnovation < momInnovation) {
				if (best == dad) {
					selectedGene = dadGene;
				}
				curDad++;
			} else if (dadInnovation == momInnovation) {
				if (rnd.nextFloat() < 0.5f) {
					selectedGene = momGene;
				}

				else {
					selectedGene = dadGene;
				}
				curMom++;
				curDad++;
			}

			if (selectedGene != null) {
				if (selectedLinks.size() == 0) {
					selectedLinks.add(selectedGene);
				} else {
					if (selectedLinks.get(selectedLinks.size() - 1)
							.getInnovationId() != selectedGene
							.getInnovationId()) {
						selectedLinks.add(selectedGene);
					}
				}

				// Check if we already have the nodes referred to in
				// SelectedGene.
				// If not, they need to be added.
				addNeuronID(selectedGene.getFromNeuronID(), selectedNeurons,
						best, notBest);
				addNeuronID(selectedGene.getToNeuronID(), selectedNeurons,
						best, notBest);
			}

		}

		// now create the required nodes. First sort them into order
		Collections.sort(selectedNeurons);

		// finally, create the genome
		final NEATGenomeFactory factory = (NEATGenomeFactory) this.owner
				.getPopulation().getGenomeFactory();
		final NEATGenome babyGenome = factory.factor(selectedNeurons,
				selectedLinks, mom.getInputCount(), mom.getOutputCount());
		babyGenome.setBirthGeneration(this.owner.getIteration());
		babyGenome.setPopulation(this.owner.getPopulation());
		babyGenome.sortGenes();

		offspring[offspringIndex] = babyGenome;
	}

}

