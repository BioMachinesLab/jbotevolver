package neat;

import java.util.ArrayList;
import java.util.Vector;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.ml.data.MLData;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.util.EngineArray;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.NeuralNetwork;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;


public class ERNEATNetwork extends NeuralNetwork {

	private static final long serialVersionUID = 1L;
	protected NEATNetwork network;

	public ERNEATNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		this.create(inputs, outputs);
	}
	
	public ERNEATNetwork(NEATNetwork network){
		this.network = network;
		
		inputNeuronStates  = new double[network.getInputCount()];
		outputNeuronStates = new double[network.getOutputCount()];
		
		reset();
	}

	@Override
	public void create(Vector<NNInput> inputs, Vector<NNOutput> outputs) {
		super.create(inputs, outputs);
	}
	
	@Override
	public void setWeights(double[] weights) {
		network = getNetworkByWeights(weights);
	}

	@Override
	protected double[] propagateInputs(double[] input) {
		
		double[] result = new double[network.getOutputCount()];

		double[] preActivation = network.getPreActivation();
		preActivation[0] = 1.0;
		double[] postActivation = network.getPostActivation();
		NEATLink[] links = network.getLinks();
		// copy input
		EngineArray.arrayCopy(input, 0, postActivation, 1, network.getInputCount());

		// 1 activation cycles
		for (int j = 0; j < links.length; j++) {
			preActivation[links[j].getToNeuron()] += postActivation[links[j].getFromNeuron()] * links[j].getWeight();
		}
		
		for (int j = network.getOutputIndex(); j < preActivation.length; j++) {
			postActivation[j] = preActivation[j];
			network.getActivationFunctions()[j].activationFunction(postActivation, j, 1);
			preActivation[j] = 0.0F;
		}

		// copy output
		EngineArray.arrayCopy(postActivation, network.getOutputIndex(), result, 0, network.getOutputCount());

		return result;
	}


	@Override
	public void reset() {
		network.getPostActivation()[0] = 1.0;
	}

	public NEATNetwork getNEATNetwork() {
		return network;
	}

	public void setNEATNetwork(NEATNetwork newNetwork) {
		this.network = newNetwork;
	}

	public void controlStep(double time) {
		super.controlStep(time);
	}
	
	@Override
	public double[] getWeights() {
		return ERNEATNetwork.getWeights(this.getNEATNetwork());
	}
	
	public static double[] getWeights(NEATNetwork network) {
		int inputs = network.getInputCount();
		int outputs = network.getOutputCount();
		int nLinks = network.getLinks().length;
		int nActivations = network.getActivationFunctions().length;
		
		double[] weights = new double[4+3*nLinks];
		weights[0] = inputs;
		weights[1] = outputs;
		weights[2] = nLinks;
		weights[3] = nActivations;
		
		for(int i = 0 ; i < nLinks ; i++) {
			int pos = 4+3*i;
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

	public static NEATNetwork getNetworkByWeights(double[] weights) {
		
		int inputs = (int)weights[0];
		int outputs = (int)weights[1];
		int nLinks = (int)weights[2];
		int nActivations = (int)weights[3];
		
		ArrayList<NEATLink> links = new ArrayList<NEATLink>();
		ActivationFunction[] activations = new ActivationFunction[nActivations];
		
		for(int i = 0 ; i < nLinks ; i++) {
			int pos = 4+3*i;

			int from = (int) weights[pos++];
			int to = (int) weights[pos++];
			double weight = weights[pos++];
			
			links.add(new NEATLink(from, to, weight));
		}
		
		for(int i = 0 ; i < nActivations ; i++) {
			activations[i]=new ActivationSteepenedSigmoid();
		}
		
		return new NEATNetwork(inputs, outputs, links, activations);
	}
}
