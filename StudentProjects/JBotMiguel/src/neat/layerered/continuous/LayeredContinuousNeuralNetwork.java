package neat.layerered.continuous;

import java.util.ArrayList;
import java.util.Vector;

import neat.continuous.FactorNEATContinuousGenome;
import neat.layerered.ANNNeuron;
import neat.layerered.ANNSynapse;
import neat.layerered.LayeredANN;
import neat.layerered.LayeredNeuralNetwork;

import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.GenomeFactory;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class LayeredContinuousNeuralNetwork extends LayeredNeuralNetwork {
	
	private static final long serialVersionUID = 4943572345259106717L;
	protected double[] outputStates;
	
	public LayeredContinuousNeuralNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		super(inputs, outputs, arguments);
	}
	
	public LayeredContinuousNeuralNetwork(LayeredANN network){
		super(network);
	}
	
	@Override
	public void setWeights(double[] weights) {
		network = getNetworkByWeights(weights);
	}
	
	@Override
	public double[] getWeights() {
		return getWeights(this.getNetwork());
	}
	
	public static double[] getWeights(LayeredANN network) {
		int nNeurons = network.getAllNeurons().size();
		int nSynapses = network.getAllSynapses().size();
		
		double[] weights = new double[2 + 4*nNeurons + 4*nSynapses];
		int pos = 0;
		weights[pos++] = nNeurons;
		weights[pos++] = nSynapses;
		
		for(int i = 0 ; i < nNeurons ; i++) {
			ANNNeuron neuron = network.getAllNeurons().get(i);
			weights[pos++] = neuron.getId();
			weights[pos++] = neuron.getType();
			
			double decay = 0;
			double bias = 0;
			
			if(neuron.getType() == ANNNeuron.HIDDEN_NEURON) {
				decay =((ANNNeuronContinuous)neuron).getDecay();
				bias =((ANNNeuronContinuous)neuron).getBias();
			} else if(neuron.getType() == ANNNeuron.OUTPUT_NEURON) {
				bias =((ANNOutputNeuron)neuron).getBias();
			}
			
			weights[pos++] = decay;
			weights[pos++] = bias;
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
			double decay = weights[pos++];
			double bias = weights[pos++];
			
			ANNNeuron neuron;
			
			if(type == ANNNeuron.HIDDEN_NEURON) {
				neuron = new ANNNeuronContinuous(id, type, new ActivationSteepenedSigmoid(), decay, bias);
			} else if(type == ANNNeuron.OUTPUT_NEURON){
				neuron = new ANNOutputNeuron(id, type, new ActivationSteepenedSigmoid(), bias);
			} else { 
				neuron = new ANNNeuron(id, type, new ActivationSteepenedSigmoid());
			}
			
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
		
		
		return new LayeredContinuousNEATCODEC().createNetFromStructure(synapses,neurons);
	}
	
	@Override
	public GeneticCODEC getCODEC() {
		return new LayeredContinuousNEATCODEC();
	}
	
	@Override
	public GenomeFactory getGenomeFactory() {
		return new FactorNEATContinuousGenome();
	}
}