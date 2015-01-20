package neat.parameters;

import java.util.ArrayList;
import java.util.Vector;

import neat.layerered.ANNNeuron;
import neat.layerered.ANNSynapse;
import neat.layerered.LayeredANN;
import neat.layerered.continuous.ANNNeuronContinuous;
import neat.layerered.continuous.LayeredContinuousNEATCODEC;
import neat.layerered.continuous.LayeredContinuousNeuralNetwork;

import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.GenomeFactory;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import evolutionaryrobotics.neuralnetworks.outputs.SimpleNNOutput;

public class ParameterNeuralNetwork extends LayeredContinuousNeuralNetwork {
	
	private static final long serialVersionUID = 4943572345259106717L;
	protected double[] outputStates;
	
	public ParameterNeuralNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		super(inputs, outputs, arguments);
	}
	
	public ParameterNeuralNetwork(LayeredANN network){
		super(network);
	}
	
	public void setNetwork(MLMethod network) {
		this.network = (LayeredANN)network;
		
		numberOfInputNeurons = 0;
		numberOfOutputNeurons = 0;
		
		Vector<NNOutput> outputs = new Vector<NNOutput>();
		
		outputs.add(new SimpleNNOutput(new Arguments("numberofoutputs="+this.network.getOutputCount())));
		this.create(inputs, outputs);
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
			
			if(neuron.getType() == ANNNeuron.HIDDEN_NEURON) {
				weights[pos++] =((ANNNeuronContinuous)neuron).getDecay();
				weights[pos++] =((ANNNeuronContinuous)neuron).getBias();
			} else if(neuron.getType() == ANNNeuron.OUTPUT_NEURON) {
				weights[pos++] =((ParameterNeuron)neuron).getBias();
				weights[pos++] =((ParameterNeuron)neuron).getParameter();
			}
			
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
			
			ANNNeuron neuron;
			
			if(type == ANNNeuron.HIDDEN_NEURON) {
				
				double decay = weights[pos++];
				double bias = weights[pos++];
				
				neuron = new ANNNeuronContinuous(id, type, new ActivationSteepenedSigmoid(), decay, bias);
			} else if(type == ANNNeuron.OUTPUT_NEURON){
				
				double bias = weights[pos++];
				double parameter = weights[pos++];
				neuron = new ParameterNeuron(id, type, new ActivationSteepenedSigmoid(), bias, parameter);
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
		return new ParameterNEATCODEC();
	}
	
	@Override
	public GenomeFactory getGenomeFactory() {
		return new FactorParameterGenome();
	}
}