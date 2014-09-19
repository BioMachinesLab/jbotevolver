package neat.continuous;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATNeuronGene;

public class NEATContinuousNeuronGene extends NEATNeuronGene {
	
	private double decay;
	private double bias;
	
	public NEATContinuousNeuronGene(final NEATNeuronType type, ActivationFunction theActivationFunction, final long id, final long innovationID, double decay, double bias) {
		super(type,theActivationFunction,id,innovationID);
		this.decay = decay;
		this.bias = bias;
	}
	
	public NEATContinuousNeuronGene(NEATContinuousNeuronGene other) {
		super(other);
		this.decay = other.decay;
		this.bias = other.bias;
	}
	
	public double getDecay() {
		return decay;
	}
	
	public void setDecay(double decay) {
		this.decay = decay;
	}
	
	public double getBias() {
		return bias;
	}
	
	public void setBias(double bias) {
		this.bias = bias;
	}
	
	@Override
	public void copy(final NEATNeuronGene gene) {
		final NEATNeuronGene other = gene;
		super.copy(other);
		if(other instanceof NEATContinuousNeuronGene) {
			final NEATContinuousNeuronGene otherContinuous = (NEATContinuousNeuronGene)gene;
			this.decay = otherContinuous.decay;
			this.bias = otherContinuous.bias;
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
		result.append(", bias=");
		result.append(this.getBias());
		result.append("]");
		return result.toString();
	}

}
