package neat.continuous;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATNeuronGene;

public class NEATContinuousNeuronGene extends NEATNeuronGene{
	
	private double decay;
	
	public NEATContinuousNeuronGene(final NEATNeuronType type, ActivationFunction theActivationFunction, final long id, final long innovationID, double decay) {
		super(type,theActivationFunction,id,innovationID);
		this.decay = decay;
	}
	
	public double getDecay() {
		return decay;
	}
	
	public void setDecay(double decay) {
		this.decay = decay;
	}
	
	@Override
	public void copy(final NEATNeuronGene gene) {
		final NEATNeuronGene other = gene;
		super.copy(other);
		if(other instanceof NEATContinuousNeuronGene) {
			final NEATContinuousNeuronGene otherContinuous = (NEATContinuousNeuronGene)gene;
			this.decay = otherContinuous.decay;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[NEATContinuousNeuronGene: id=");
		result.append(this.getId());
		result.append(", type=");
		result.append(this.getNeuronType());
		result.append(", decay=");
		result.append(this.getDecay());
		result.append("]");
		return result.toString();
	}

}
