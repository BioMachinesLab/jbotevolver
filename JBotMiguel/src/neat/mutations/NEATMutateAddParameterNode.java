package neat.mutations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import neat.parameters.ParameterNeuronGene;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.NEATPopulation;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATInnovation;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

public class NEATMutateAddParameterNode extends NEATMutateAddContinuousNode implements Serializable {
	
	protected static double CONNECTION_RANGE = 1;
	
	public void performOperation(final Random rnd, final Genome[] parents, final int parentIndex, final Genome[] offspring, final int offspringIndex) {
		final NEATGenome target = obtainGenome(parents, parentIndex, offspring, offspringIndex);
		
		int numberOfConnections = getTargetNumberOfConnections(target);

		final NEATPopulation pop = ((NEATPopulation) target.getPopulation());

		// add the output neuron
		final ActivationFunction af = ((NEATPopulation)getOwner().getPopulation()).getActivationFunctions().pick(new Random(rnd.nextLong()));
		
		double bias = rnd.nextDouble()*(2*decayRange) - decayRange;
		double parameter = rnd.nextDouble()*(2*pop.getWeightRange()) - pop.getWeightRange();
		
		final NEATInnovation innovation = ((NEATPopulation)getOwner().getPopulation()).getInnovations().findInnovation(pop.assignGeneID());
		target.getNeuronsChromosome().add(new ParameterNeuronGene(NEATNeuronType.Output, af, innovation.getNeuronID(), innovation.getInnovationID(), 0, bias, parameter));
		
		ArrayList<Long> choices = new ArrayList<Long>();
		
		for(NEATNeuronGene g : target.getNeuronsChromosome()) {
			if(g.getNeuronType() != NEATNeuronType.Output) {
				choices.add(g.getId());
			}
		}
		
		//add connections
		for(int i = 0 ; i < numberOfConnections ; i++)
			createConnection(target, innovation.getNeuronID(), choices, rnd);
		
		target.sortGenes();
	}
	
	protected int getTargetNumberOfConnections(NEATGenome genome) {
		
		//find the average number of connections in output neurons
		
		int total = 0;
		
		ArrayList<Long> outputNeurons = new ArrayList<Long>();
		
		for(NEATNeuronGene g : genome.getNeuronsChromosome()) {
			if(g.getNeuronType() == NEATNeuronType.Output)
				outputNeurons.add(g.getId());
		}
		
		for(NEATLinkGene g : genome.getLinksChromosome()) {
			for(Long l : outputNeurons) {
				if(g.getToNeuronID() == l.longValue()) {
					total++;
				}
			}
		}
		
		return Math.max(total/outputNeurons.size(), 1);
	}
	
	protected void createConnection(NEATGenome g, long outputNeuronId, ArrayList<Long> choices, Random rnd) {
		
		if(!choices.isEmpty()) {
			
			int index = rnd.nextInt(choices.size());
			long fromId = choices.get(index);
			choices.remove(index);
			
			createLink(g, fromId, outputNeuronId, rnd.nextDouble()*CONNECTION_RANGE);
		}
	}
}