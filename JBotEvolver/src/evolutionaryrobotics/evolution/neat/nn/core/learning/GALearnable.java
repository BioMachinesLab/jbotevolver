package evolutionaryrobotics.evolution.neat.nn.core.learning;

import evolutionaryrobotics.evolution.neat.nn.core.Learnable;
import evolutionaryrobotics.evolution.neat.nn.core.LearningEnvironment;
import evolutionaryrobotics.evolution.neat.nn.core.NeuralNet;

/**
 * @author MSimmerson
 *
 * Describes the learning environment for the NEAT networks
 */
public class GALearnable implements Learnable {
	private LearningEnvironment env;

	public GALearnable(LearningEnvironment env) {
		this.env = env;
	}
	/**
	 * @see org.neat4j.ailibrary.nn.core.Learnable#teach(org.neat4j.ailibrary.nn.core.NeuralNet)
	 */
	public void teach(NeuralNet net) {
		// does nothing
		throw new UnsupportedOperationException("teach operation not supported in NEAT");
	}
	
	/**
	 * @see org.neat4j.ailibrary.nn.core.Learnable#learningEnvironment()
	 */
	public LearningEnvironment learningEnvironment() {
		return (this.env);
	}

}
