package neat.parameters;

import neat.layerered.ANNNeuron;
import neat.layerered.continuous.ANNOutputNeuron;
import org.encog.engine.network.activation.ActivationFunction;

public class ParameterNeuron extends ANNOutputNeuron {
	
	private double parameter;
	
	public ParameterNeuron(long id, int type, ActivationFunction function, double bias, double parameter) {
		super(id, type, function, bias);
		this.parameter = parameter;
	}
	
	@Override
	public ANNNeuron shallowCopy() {
		ANNNeuron copy = new ParameterNeuron(this.id, this.type, this.activationFunction.clone(), this.bias, parameter);
		copy.setNeuronDepth(depth);
		return copy;
	}
	
	public double getParameter() {
		return parameter;
	}

}
