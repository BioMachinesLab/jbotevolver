package neat.layerered.continuous;

import neat.layerered.ANNNeuron;
import net.jafama.FastMath;

import org.encog.engine.network.activation.ActivationFunction;

public class ANNNeuronContinuous extends ANNNeuron {

	protected double decay;
	protected double bias;
	protected double state = 0;
	protected double timeStep = 0.2;
	protected double tau      = 2.5;
	protected double calculatedDecay;
	
	public ANNNeuronContinuous(long id, int type, ActivationFunction function, double decay, double bias){
		super(id,type,function);
		this.decay = decay;
		this.bias = bias;
		calculatedDecay = FastMath.powQuick(10, (-1.0 + (tau * (decay + 10.0) / 20)));
		calculatedDecay*=timeStep/calculatedDecay;
	}

	public void step() {
		
		if(this.type == ANNNeuron.INPUT_NEURON || this.type == ANNNeuron.BIAS_NEURON)
			return;
		double currentActivation = 0;
		
		currentActivation = -state;
		
		for(int i = 0; i < this.incomingSynapses.size(); i++){
			currentActivation += incomingSynapses.get(i).getWeight() * incomingNeurons.get(i).getActivationValue();
		}
		currentActivation = state + currentActivation*calculatedDecay;
		
		state = currentActivation;
		
		currentActivation+=bias;
		
		//activate
		double[] value = new double[]{currentActivation};
		activationFunction.activationFunction(value, 0, 1);
		this.activation = value[0];
	}

	public double getActivationValue() {
		return activation;
	}

	@Override
	public ANNNeuronContinuous shallowCopy() {
		ANNNeuronContinuous copy = new ANNNeuronContinuous(this.id, this.type, this.activationFunction.clone(), this.decay, this.bias);
		copy.setNeuronDepth(depth);
		
		return copy;
	}
	
	public double getDecay() {
		return decay;
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
