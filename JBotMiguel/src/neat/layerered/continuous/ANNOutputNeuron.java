package neat.layerered.continuous;

import org.encog.engine.network.activation.ActivationFunction;

import neat.layerered.ANNNeuron;

public class ANNOutputNeuron extends ANNNeuron {
	
	protected double state = 0;

	public ANNOutputNeuron(long id, int type, ActivationFunction function) {
		super(id, type, function);
	}
	
	public void step() {
		if(this.type == ANNNeuron.INPUT_NEURON || this.type == ANNNeuron.BIAS_NEURON)
			return;
		//a neuron that performs a weighted sum of the inputs
		double currentActivation = 0;
		
		for(int i = 0; i < this.incomingSynapses.size(); i++){
			currentActivation += incomingSynapses.get(i).getWeight() 
					* incomingNeurons.get(i).getActivationValue();
		}
		
		currentActivation+=state;
		
		//activate
		double[] value = new double[]{currentActivation};
		activationFunction.activationFunction(value, 0, 1);
		this.activation = value[0];

		state = activation;
	}
	
	@Override
	public ANNNeuron shallowCopy() {
		ANNNeuron copy = new ANNOutputNeuron(this.id, this.type, this.activationFunction.clone());
		copy.setNeuronDepth(depth);
		
		return copy;
	}
	@Override
	public void reset() {
		super.reset();
		this.state = 0;
	}

}
