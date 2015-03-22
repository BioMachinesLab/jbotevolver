package neat.parameters;

import neat.continuous.NEATContinuousNeuronGene;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATNeuronGene;

public class ParameterNeuronGene extends NEATContinuousNeuronGene {
	
	private double parameter;
	
	public ParameterNeuronGene(final NEATNeuronType type, ActivationFunction theActivationFunction, final long id, final long innovationID, double decay, double bias, double parameter) {
		super(type,theActivationFunction,id,innovationID,decay,bias);
		this.parameter = parameter;
	}
	
	public ParameterNeuronGene(ParameterNeuronGene other) {
		super(other);
		this.parameter = other.parameter;
	}
	
	@Override
	public void copy(final NEATNeuronGene gene) {
		final NEATNeuronGene other = gene;
		super.copy(other);
		if(other instanceof ParameterNeuronGene) {
			final ParameterNeuronGene otherContinuous = (ParameterNeuronGene)gene;
			this.parameter = otherContinuous.parameter;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[ParameterNeuronGene: id=");
		result.append(this.getId());
		result.append(", type=");
		result.append(this.getNeuronType());
		result.append(", decay=");
		result.append(this.getDecay());
		result.append(", bias=");
		result.append(this.getBias());
		result.append(", parameter=");
		result.append(this.getParameter());
		result.append("]");
		return result.toString();
	}
	
	public double getParameter() {
		return parameter;
	}
	
	public void setParameter(double parameter) {
		this.parameter = parameter;
	}
}