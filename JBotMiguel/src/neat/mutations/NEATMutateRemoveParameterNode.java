package neat.mutations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import org.encog.ml.ea.genome.Genome;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.neural.neat.training.opp.NEATMutateAddNode;

public class NEATMutateRemoveParameterNode extends NEATMutateAddNode implements Serializable {

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
		long neuronId = target.getNeuronsChromosome().get(index).getId(); 
		
		Iterator<NEATLinkGene> i = target.getLinksChromosome().iterator();
		
		while(i.hasNext()) {
			NEATLinkGene g = i.next();
			if(g.getToNeuronID() == neuronId) {
				//TODO maybe I should just disable the links, but the amount of dead links will be huge
				g.setEnabled(false);
//				i.remove();
			}
		}
		
		target.getNeuronsChromosome().remove(index);
		
		target.sortGenes();
	}
}