package neat.continuous;

import java.util.List;
import java.util.Random;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.training.NEATGenome;
import org.encog.neural.neat.training.NEATLinkGene;
import org.encog.neural.neat.training.NEATNeuronGene;

public interface SelectContinuousNeurons {
	
	/**
	 * @return The trainer being used.
	 */
	EvolutionaryAlgorithm getTrainer();

	/**
	 * Setup the selector.
	 * @param theTrainer The trainer.
	 */
	void init(EvolutionaryAlgorithm theTrainer);

	/**
	 * Select links from the specified genome.
	 * @param rnd A random number generator.
	 * @param genome The genome to select from.
	 * @return A List of link genomes.
	 */
	List<NEATContinuousNeuronGene> selectNeurons(Random rnd, NEATGenome genome);

}
