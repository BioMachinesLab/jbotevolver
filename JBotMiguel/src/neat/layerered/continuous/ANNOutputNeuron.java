package neat.layerered.continuous;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.NeuralNetworkError;

import neat.layerered.ANNNeuron;

public class ANNOutputNeuron extends ANNNeuron {
	
	protected double state = 0;
	protected double bias = 0;

	public ANNOutputNeuron(long id, int type, ActivationFunction function, double bias) {
		super(id, type, function);
		this.bias = bias;
	}
	
	public void step() {
		if(this.type == ANNNeuron.INPUT_NEURON || this.type == ANNNeuron.BIAS_NEURON || this.type == ANNNeuron.HIDDEN_NEURON)
			throw new NeuralNetworkError("This is an output neuron, not a different type of neuron!");
		
		//a neuron that performs a weighted sum of the inputs
		
		for(int i = 0; i < this.incomingSynapses.size(); i++){
			state += incomingSynapses.get(i).getWeight() 
					* incomingNeurons.get(i).getActivationValue();
		}
		
		//activate
		double[] value = new double[]{state + bias};
		activationFunction.activationFunction(value, 0, 1);
		state = value[0];

		activation = state;
	}
	
	@Override
	public ANNNeuron shallowCopy() {
		ANNNeuron copy = new ANNOutputNeuron(this.id, this.type, this.activationFunction.clone(), this.bias);
		copy.setNeuronDepth(depth);
		
		return copy;
	}
	@Override
	public void reset() {
		super.reset();
		this.state = 0;
	}
	
	public double getBias() {
		return bias;
	}

}
