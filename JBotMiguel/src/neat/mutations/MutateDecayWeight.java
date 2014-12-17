package neat.mutations;

import java.util.Random;

import neat.continuous.NEATContinuousNeuronGene;

import org.encog.ml.ea.train.EvolutionaryAlgorithm;
import org.encog.neural.neat.training.NEATNeuronGene;

public interface MutateDecayWeight {
	
	EvolutionaryAlgorithm getTrainer();

	void init(EvolutionaryAlgorithm theTrainer);

	void mutateWeight(Random rnd, NEATContinuousNeuronGene neuronGene, double weightRange);
}
