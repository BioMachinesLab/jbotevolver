package neat.continuous;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATNeuronGene;

public class SelectContinuousProportion implements SelectContinuousNeurons,Serializable {
	
	/**
	 * The portion of the links to mutate. 0.0 for none, 1.0 for all.
	 */
	private double proportion;
	
	/**
	 * The trainer.
	 */
	private EvolutionaryAlgorithm trainer;
	
	/**
	 * Select based on proportion.
	 * @param theProportion The proportion to select from.
	 */
	public SelectContinuousProportion(double theProportion) {
		this.proportion = theProportion;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(EvolutionaryAlgorithm theTrainer) {
		this.trainer = theTrainer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<NEATContinuousNeuronGene> selectNeurons(Random rnd, NEATGenome genome) {
		List<NEATContinuousNeuronGene> result = new ArrayList<NEATContinuousNeuronGene>();
		
		boolean mutated = false;
		
		for (final NEATNeuronGene neuronGene : genome.getNeuronsChromosome()) {
			if(neuronGene instanceof NEATContinuousNeuronGene && rnd.nextDouble() < this.proportion) {
				mutated = true;
				result.add((NEATContinuousNeuronGene)neuronGene);	
			}
		}
		
		if( !mutated ) {
			
			for(final NEATNeuronGene neuronGene : genome.getNeuronsChromosome()) {
				if(neuronGene instanceof NEATContinuousNeuronGene) {
					result.add((NEATContinuousNeuronGene)neuronGene);
					break;
				}
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EvolutionaryAlgorithm getTrainer() {
		return trainer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[");
		result.append(this.getClass().getSimpleName());
		result.append(":proportion=");
		result.append(this.proportion);
		result.append("]");
		return result.toString();
	}
	
	
}

