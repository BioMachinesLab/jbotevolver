package neat.mutations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import neat.parameters.ParameterNeuronGene;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATMutateAddNode;

public class NEATMutateChangeParameterNode extends NEATMutateAddNode implements Serializable {
	
	private double strength = 1;
	
	public NEATMutateChangeParameterNode(double strength) {
		this.strength = strength;
	}
	
	public NEATMutateChangeParameterNode() {
	}

	public void performOperation(final Random rnd, final Genome[] parents, final int parentIndex, final Genome[] offspring, final int offspringIndex) {
		final NEATGenome target = obtainGenome(parents, parentIndex, offspring, offspringIndex);
		
		ArrayList<Integer> choices = new ArrayList<Integer>();
		
		for(int i = 0 ; i < target.getNeuronsChromosome().size() ; i++) {
			NEATNeuronGene g = target.getNeuronsChromosome().get(i);
			if(g.getNeuronType() == NEATNeuronType.Output) {
				choices.add(i);
			}
		}
		
		int index = rnd.nextInt(choices.size());
		ParameterNeuronGene gene = (ParameterNeuronGene)target.getNeuronsChromosome().get(choices.get(index)); 
		
		double param = gene.getParameter();
		
		gene.setParameter(param+rnd.nextGaussian()*strength);
		
		target.sortGenes();
	}
}