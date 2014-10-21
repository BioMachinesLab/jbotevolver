package neat;

import java.util.ArrayList;
import java.util.Vector;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationSteepenedSigmoid;
import org.encog.ml.MLMethod;
import org.encog.ml.ea.codec.GeneticCODEC;
import org.encog.ml.ea.genome.GenomeFactory;
import org.encog.neural.neat.FactorNEATGenome;
import org.encog.neural.neat.NEATCODEC;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.util.EngineArray;

import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;


public class ERNEATNetwork extends WrapperNetwork{

	private static final long serialVersionUID = 1L;
	protected NEATNetwork network;
	protected double[] preActivation;
	protected double[] postActivation;

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

		preActivation = network.getPreActivation();
		postActivation = network.getPostActivation();
		
		// clear from previous
		EngineArray.fill(preActivation, 0.0);
		EngineArray.fill(postActivation, 0.0);

		//bias neuron is always the first
		postActivation[0] = 1.0;
		
		// copy input
		EngineArray.arrayCopy(input, 0, postActivation, 1, network.getInputCount());

		for(int j = 0 ; j < 4 ; j++) {
			internalCompute(network.getLinks());
		}

		// copy output
		EngineArray.arrayCopy(postActivation, network.getOutputIndex(), result, 0, network.getOutputCount());
		EngineArray.arrayCopy(postActivation, network.getOutputIndex(), outputNeuronStates, 0, network.getOutputCount());

		return result;
	}
	
	private void internalCompute(NEATLink[] links) {
		
		for(int i = 0 ; i < preActivation.length ;i++) {
			this.preActivation[i] = 0.0F;
		}
		
		for (int j = 0; j < links.length; j++) {
			this.preActivation[links[j].getToNeuron()] += this.postActivation[links[j]
					.getFromNeuron()] * links[j].getWeight();
		}

		for (int j = network.getOutputIndex(); j < this.preActivation.length; j++) {
			this.postActivation[j] = this.preActivation[j];
			network.getActivationFunctions()[j].activationFunction(this.postActivation,
					j, 1);
			//bring down the pre activations to the 5th decimal place
			//in order to prevent excessive loops of the internal computation
			//function due to tiny variations caused by rounding issues
		}
	}


	@Override
	public void reset() {
		
	}

	public NEATNetwork getNetwork() {
		return network;
	}

	@Override
	public void setNetwork(MLMethod newNetwork) {
		this.network = (NEATNetwork)newNetwork;
	}

	public void controlStep(double time) {
		super.controlStep(time);
	}
	
	@Override
	public double[] getWeights() {
		return ERNEATNetwork.getWeights(this.getNetwork());
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
	
	@Override
	public GeneticCODEC getCODEC() {
		return new NEATCODEC();
	}
	
	@Override
	public GenomeFactory getGenomeFactory() {
		return new FactorNEATGenome();
	}
}
