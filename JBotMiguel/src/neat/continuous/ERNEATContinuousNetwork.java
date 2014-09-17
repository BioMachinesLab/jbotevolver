package neat.continuous;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import neat.ERNEATNetwork;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.NEATNeuronType;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.util.EngineArray;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class ERNEATContinuousNetwork extends ERNEATNetwork {

	private static final long serialVersionUID = 1L;

	public ERNEATContinuousNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		super(inputs, outputs, arguments);
	}
	
	public ERNEATContinuousNetwork(NEATContinuousNetwork network){
		super(network);
		
	}

	@Override
	//TODO protected
	public double[] propagateInputs(double[] input) {
		double[] result = ((NEATContinuousNetwork)network).compute(input);
		inputNeuronStates = input;
		outputNeuronStates = result;
		return result;
	}
	
	@Override
	public void reset() {
		((NEATContinuousNetwork)network).resetStates();
	}

	public NEATNetwork getNEATNetwork() {
		return network;
	}

	@Override
	public void setNEATNetwork(NEATNetwork newNetwork) {
		super.setNEATNetwork(newNetwork);
	}

	public void controlStep(double time) {
		super.controlStep(time);
	}
	
	@Override
	public double[] getWeights() {
		return ERNEATContinuousNetwork.getWeights(this.getNEATNetwork());
	}
	
	@Override
	public void setWeights(double[] weights) {
		network = getNetworkByWeights(weights);
	}
	
	public static double[] getWeights(NEATNetwork net) {
		
		NEATContinuousNetwork network = (NEATContinuousNetwork)net;
		
		int inputs = network.getInputCount();
		int outputs = network.getOutputCount();
		int nNeurons = network.getNumberOfNeurons();
		int nLinks = network.getLinks().length;
		int nActivations = network.getActivationFunctions().length;
		
		int pos = 0;
		
		double[] weights = new double[5+5*nLinks+3*nLinks];
		weights[pos++] = inputs;
		weights[pos++] = outputs;
		weights[pos++] = nNeurons;
		weights[pos++] = nLinks;
		weights[pos++] = nActivations;
		
		for(int i = 0 ; i < nNeurons ; i++) {
			Neuron neuron = network.getNeurons()[i];

			weights[pos++] = neuron.getType();
			weights[pos++] = neuron.getId();
			weights[pos++] = neuron.getInnovationId();
			
			double decay = 0;
			double bias = 0;
			if(neuron.isDecayNeuron()) {
				decay = neuron.getDecay();
				bias = neuron.getBias();
			}
			
			weights[pos++] = decay;
			weights[pos++] = bias;
		}
		
		for(int i = 0 ; i < nLinks ; i++) {
			NEATLink link = network.getLinks()[i];

			weights[pos++] = link.getFromNeuron();
			weights[pos++] = link.getToNeuron();
			weights[pos++] = link.getWeight();
		}
		
		for(int i = 0 ; i < nActivations ; i++) {
			//If it breaks here, it's because we don't always have SteepenedSigmoids!!
			ActivationSteepenedSigmoid link = (ActivationSteepenedSigmoid)network.getActivationFunctions()[i];
		}
		
		return weights;
	}

	public static NEATContinuousNetwork getNetworkByWeights(double[] weights) {
try {
		int pos = 0;
		
		int inputs = (int)weights[pos++];
		int outputs = (int)weights[pos++];
		int nNeurons = (int)weights[pos++];
		int nLinks = (int)weights[pos++];
		int nActivations = (int)weights[pos++];
		
		ArrayList<NEATLink> links = new ArrayList<NEATLink>();
		ActivationFunction[] activations = new ActivationFunction[nActivations];
		List<NEATNeuronGene> neurons = new ArrayList<NEATNeuronGene>(nNeurons);
		
		for(int i = 0 ; i < nNeurons ; i++) {

			double type = weights[pos++];
			double neuronId = weights[pos++];
			double innovationId = weights[pos++];
			double decay = weights[pos++];
			double bias = weights[pos++];
			
			if(decay != 0) {
				neurons.add(new NEATContinuousNeuronGene(NEATNeuronType.values()[(int)type],
						new ActivationSteepenedSigmoid(), (long)neuronId, (long)innovationId, decay,bias));
			} else {
				neurons.add(new NEATNeuronGene(NEATNeuronType.values()[(int)type],
						new ActivationSteepenedSigmoid(), (long)neuronId, (long)innovationId));
			}
			
		}
		
		for(int i = 0 ; i < nLinks ; i++) {

			int from = (int) weights[pos++];
			int to = (int) weights[pos++];
			double weight = weights[pos++];
			
			links.add(new NEATLink(from, to, weight));
		}
		
		for(int i = 0 ; i < nActivations ; i++) {
			activations[i]=new ActivationSteepenedSigmoid();
		}
		
		return new NEATContinuousNetwork(inputs, outputs, links, activations, neurons);
} catch(Exception e) {
	e.printStackTrace();
}
return null;
	}
}
