package neat.layerered.continuous;

import neat.layerered.ANNNeuron;
import net.jafama.FastMath;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.neural.NeuralNetworkError;

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
		calculatedDecay = FastMath.powQuick(10, (-1.0 + (tau * (decay + 10.0) / 20.0)));
		calculatedDecay = timeStep/calculatedDecay;
	}

	public void step() {
		
//		print("Hidden Neuron "+id);
		
		if(this.type == ANNNeuron.INPUT_NEURON || this.type == ANNNeuron.BIAS_NEURON || this.type == ANNNeuron.OUTPUT_NEURON)
			throw new NeuralNetworkError("This is an hidden neuron, not a different type of neuron!");
		
		double deltaState = -state;
		
		for(int i = 0; i < this.incomingSynapses.size(); i++){
			deltaState += incomingSynapses.get(i).getWeight() * incomingNeurons.get(i).getActivationValue();
//			print("Incoming weight ("+incomingSynapses.get(i).getWeight()+") with activation ("+incomingNeurons.get(i).getActivationValue()+") from neuron "+incomingNeurons.get(i).getId());
		}
		state+= deltaState*calculatedDecay;
		
		//activate
//		double[] value = new double[]{state + bias};
//		activationFunction.activationFunction(value, 0, 1);
//		this.activation = value[0];
		
		activation = ((1.0)/(FastMath.expQuick(-(state + bias)) + 1.0 ));
		
//		print("# activation: "+activation+" ; state: "+state);
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
