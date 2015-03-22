package neat.mutations;

import java.util.Random;
import neat.continuous.NEATContinuousNeuronGene;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATInnovation;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.opp.NEATMutateAddNode;

public class NEATMutateAddContinuousNode extends NEATMutateAddNode{
	
	protected static double decayRange = 2.0;
	
	public void performOperation(final Random rnd, final Genome[] parents, final int parentIndex, final Genome[] offspring, final int offspringIndex) {
		final NEATGenome target = obtainGenome(parents, parentIndex, offspring, offspringIndex);
		int countTrysToFindOldLink = getOwner().getMaxTries();

		final NEATPopulation pop = ((NEATPopulation) target.getPopulation());

		// the link to split
		NEATLinkGene splitLink = null;

		final int sizeBias = ((NEATGenome)parents[0]).getInputCount() + ((NEATGenome)parents[0]).getOutputCount() + 10;
		// if there are not at least
		int upperLimit;
		if (target.getLinksChromosome().size() < sizeBias) {
			// choose a link, use the square root to prefer the older links
			upperLimit = target.getLinksChromosome().size() - 1 - (int) Math.sqrt(target.getLinksChromosome().size());
		} else {
			upperLimit = target.getLinksChromosome().size() - 1;
		}
		
		do{
		
			while ((countTrysToFindOldLink--) > 0) {
				
				final int i = (int) RangeRandomizer.randomize(rnd, 0, upperLimit);
				final NEATLinkGene link = target.getLinksChromosome().get(i);
	
				// get the from neuron
				final long fromNeuron = link.getFromNeuronID();
				if ((link.isEnabled()) && (target.getNeuronsChromosome().get(getElementPos(target, fromNeuron)).getNeuronType() != NEATNeuronType.Bias)) {
					splitLink = link;
					break;
				}
			}
			
			if(splitLink == null) {
				upperLimit++;
				countTrysToFindOldLink = getOwner().getMaxTries();
			}
		} while(upperLimit <= target.getNumGenes() - 1 && splitLink == null);
			
		if (splitLink == null) {
			return;
		}
		splitLink.setEnabled(false);

		final long from = splitLink.getFromNeuronID();
		final long to = splitLink.getToNeuronID();

		final NEATInnovation innovation = ((NEATPopulation)getOwner().getPopulation()).getInnovations().findInnovationSplit(from, to);

		// add the splitting neuron
		final ActivationFunction af = ((NEATPopulation)getOwner().getPopulation()).getActivationFunctions().pick(new Random(rnd.nextLong()));
		
		double decay = rnd.nextDouble()*(2*decayRange) - decayRange;
		double bias = rnd.nextDouble()*(2*decayRange) - decayRange;

		target.getNeuronsChromosome().add(new NEATContinuousNeuronGene(NEATNeuronType.Hidden, af, innovation.getNeuronID(), innovation.getInnovationID(),decay,bias));
		// add the other two sides of the link
		createLink(target, from, innovation.getNeuronID(), splitLink.getWeight());
		createLink(target, innovation.getNeuronID(), to, 1);
		target.sortGenes();
	}

}
