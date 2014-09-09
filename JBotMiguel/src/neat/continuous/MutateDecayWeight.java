package neat.continuous;

import java.util.Random;
import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.training.NEATNeuronGene;

public interface MutateDecayWeight {
	
	EvolutionaryAlgorithm getTrainer();

	void init(EvolutionaryAlgorithm theTrainer);

	void mutateWeight(Random rnd, NEATContinuousNeuronGene neuronGene, double weightRange);
}
