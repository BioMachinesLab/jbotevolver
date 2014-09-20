package neat.layerered;

import java.util.ArrayList;
import java.util.Vector;

import neat.WrapperNetwork;

import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.ml.MLMethod;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class LayeredNeuralNetwork extends WrapperNetwork {
	
	protected LayeredANN network;
	
	public LayeredNeuralNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		this.create(inputs, outputs);
	}
	
	public LayeredNeuralNetwork(LayeredANN network){
		this.network = network;
		inputNeuronStates  = new double[network.getInputCount()];
		outputNeuronStates = new double[network.getOutputCount()];
		
		reset();
	}
	
	@Override
	public void setWeights(double[] weights) {
		network = getNetworkByWeights(weights);
	}
	
	@Override
	protected double[] propagateInputs(double[] inputValues) {
		return network.compute(inputValues);
	}

	@Override
	public void reset() {
		if(network != null)
			network.reset();
	}
	
	public LayeredANN getNetwork() {
		return network;
	}
	
	public void setNetwork(MLMethod network) {
		this.network = (LayeredANN)network;
	}
	
	@Override
	public double[] getWeights() {
		return LayeredNeuralNetwork.getWeights(this.getNetwork());
	}
	
	public static double[] getWeights(LayeredANN network) {
		int nNeurons = network.getAllNeurons().size();
		int nSynapses = network.getAllSynapses().size();
		
		double[] weights = new double[2 + 2*nNeurons + 4*nSynapses];
		int pos = 0;
		weights[pos++] = nNeurons;
		weights[pos++] = nSynapses;
		
		for(int i = 0 ; i < nNeurons ; i++) {
			ANNNeuron neuron = network.getAllNeurons().get(i);
			weights[pos++] = neuron.getId();
			weights[pos++] = neuron.getType();
		}
		
		for(int i = 0 ; i < nSynapses ; i++) {
			ANNSynapse synapse = network.getAllSynapses().get(i);
			
			weights[pos++] = synapse.getInnovationNumber();
			weights[pos++] = synapse.getWeight();
			weights[pos++] = synapse.getFromNeuron();
			weights[pos++] = synapse.getToNeuron();
		}
		
		return weights;
	}

	public static LayeredANN getNetworkByWeights(double[] weights) {
		int pos = 0;
		
		int nNeurons = (int)weights[pos++];
		int nSynapses = (int)weights[pos++];
		
		ArrayList<ANNSynapse> synapses = new ArrayList<ANNSynapse>();
		ArrayList<ANNNeuron> neurons = new ArrayList<ANNNeuron>();
		
		for(int i = 0 ; i < nNeurons ; i++) {
			
			long id = (long)weights[pos++];
			int type = (int)weights[pos++];
			
			ANNNeuron neuron = new ANNNeuron(id, type, new ActivationSteepenedSigmoid());
			
			neurons.add(neuron);
		}
		
		for(int i = 0 ; i < nSynapses ; i++) {
			
			long innovationNumber = (long)weights[pos++];
			double weight = weights[pos++];
			int from = (int)weights[pos++];
			int to = (int)weights[pos++];
			
			ANNSynapse synapse = new ANNSynapse(innovationNumber, weight, from, to);
			
			synapses.add(synapse);
		}
		
		return new LayeredNEATCODEC().createNetFromStructure(synapses,neurons);
	}
}