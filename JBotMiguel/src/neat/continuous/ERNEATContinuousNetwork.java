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
	private double[] states;
	private double[] currentStates;

	public ERNEATContinuousNetwork(Vector<NNInput> inputs, Vector<NNOutput> outputs, Arguments arguments){
		super(inputs, outputs, arguments);
	}
	
	public ERNEATContinuousNetwork(NEATContinuousNetwork network){
		super(network);
		states = network.getStates();
		currentStates = new double[states.length];
	}

	@Override
	//TODO protected
	public double[] propagateInputs(double[] input) {
		
		double[] result = new double[network.getOutputCount()];
		
		preActivation = network.getPreActivation();
		postActivation = network.getPostActivation();
		
		EngineArray.fill(this.preActivation, 0.0);
		EngineArray.fill(this.postActivation, 0.0);
		this.postActivation[0] = 1.0;
		
		NEATLink[] links = network.getLinks();
		// copy input
		EngineArray.arrayCopy(input, 0, postActivation, 1, network.getInputCount());
		
		loadStates();
		
		for(int i = 0 ; i < 10 ; i++) {
			internalCompute(links);
		}
		
		saveStates();
		
		// copy output
		EngineArray.arrayCopy(postActivation, network.getOutputIndex(), result, 0, network.getOutputCount());

		System.out.println();
		System.out.println();
		
		return result;
	}
	
	private void internalCompute(NEATLink[] links) {
		
		System.out.println("\n\n");
		
		double[] decays = ((NEATContinuousNetwork)network).getDecays();
		
		for(int i = 0 ; i < decays.length ; i++) {
			if(decays[i] != 0) {
				preActivation[i] = -states[i];
			}
		}
		
		for (int j = 0; j < links.length; j++) {
			this.preActivation[links[j].getToNeuron()] += this.postActivation[links[j].getFromNeuron()] * links[j].getWeight();
		}
		
		//TODO debug
		System.out.println("PreActivation");
		for (int j = 0; j < preActivation.length; j++) {
			System.out.print(preActivation[j]+" ");
		}
		
		
		System.out.println();
		System.out.println("decay");
		//apply decay to current value
		for(int j = 0 ; j < preActivation.length; j++) {
			double decay = ((NEATContinuousNetwork)network).getDecays()[j];
			if(decay != 0) {
				decay = ((NEATContinuousNetwork)network).getTimeStep()/decay;
				System.out.print(decay+" ");
				if(decay != 0) {
					preActivation[j] = states[j] + preActivation[j]*decay; 
				}
			}
		}

		//activate the neurons
		System.out.println("\nPostActivation");
		for (int j = network.getOutputIndex(); j < this.preActivation.length; j++) {
			this.postActivation[j] = this.preActivation[j];
			network.getActivationFunctions()[j].activationFunction(this.postActivation, j, 1);
			System.out.print(postActivation[j]+" ");
			//save neuron states for use in the next iteration
			currentStates[j] = preActivation[j];
			this.preActivation[j] = 0.0F;
		}
	}
	
	private void loadStates() {
		for(int i = network.getInputCount() ; i < ((NEATContinuousNetwork)network).getDecays().length ; i++) {
			if(((NEATContinuousNetwork)network).getDecays()[i] != 0) {
				preActivation[i] = states[i];
			}
		}
	}
	
	private void saveStates() {
		for(int i = 0 ; i < ((NEATContinuousNetwork)network).getDecays().length ; i++) {
			if(((NEATContinuousNetwork)network).getDecays()[i] != 0) {
				states[i] = currentStates[i];
				currentStates[i] = 0;
			}
		}
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
		this.network = (NEATContinuousNetwork)newNetwork;
		states = ((NEATContinuousNetwork)network).getStates();
	}

	public void controlStep(double time) {
		super.controlStep(time);
	}
	
	@Override
	public double[] getWeights() {
		return ERNEATContinuousNetwork.getWeights(this.getNEATNetwork());
	}
	
	public static double[] getWeights(NEATContinuousNetwork network) {
		int inputs = network.getInputCount();
		int outputs = network.getOutputCount();
		int nNeurons = network.getNumberOfNeurons();
		int nLinks = network.getLinks().length;
		int nActivations = network.getActivationFunctions().length;
		
		double[] weights = new double[4+3*nLinks];
		weights[0] = inputs;
		weights[1] = outputs;
		weights[2] = nNeurons;
		weights[3] = nLinks;
		weights[4] = nActivations;
		
		for(int i = 0 ; i < nNeurons ; i++) {
			int pos = 5 + 4*i;
			Neuron neuron = network.getNeurons()[i];

			weights[pos++] = neuron.getType();
			weights[pos++] = neuron.getId();
			weights[pos++] = neuron.getInnovationId();
			
			double decay = 0;
			if(neuron.isDecayNeuron())
				decay = neuron.getDecay();
			
			weights[pos++] = decay;
		}
		
		for(int i = 0 ; i < nLinks ; i++) {
			int pos = 5 + nNeurons*4 + 3*i;
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
		
		int inputs = (int)weights[0];
		int outputs = (int)weights[1];
		int nNeurons = (int)weights[2];
		int nLinks = (int)weights[3];
		int nActivations = (int)weights[4];
		
		ArrayList<NEATLink> links = new ArrayList<NEATLink>();
		ActivationFunction[] activations = new ActivationFunction[nActivations];
		List<NEATNeuronGene> neurons = new ArrayList<NEATNeuronGene>(nNeurons);
		
		for(int i = 0 ; i < nNeurons ; i++) {
			int pos = 5 + 4*i;

			double type = weights[pos++];
			double neuronId = weights[pos++];
			double innovationId = weights[pos++];
			double decay = weights[pos++];
			
			if(decay != 0) {
				neurons.add(new NEATContinuousNeuronGene(NEATNeuronType.values()[(int)type],
						new ActivationSteepenedSigmoid(), (long)neuronId, (long)innovationId, decay));
			} else {
				neurons.add(new NEATNeuronGene(NEATNeuronType.values()[(int)type],
						new ActivationSteepenedSigmoid(), (long)neuronId, (long)innovationId));
			}
			
		}
		
		for(int i = 0 ; i < nLinks ; i++) {
			int pos = 5 + nNeurons*4 + 3*i;

			int from = (int) weights[pos++];
			int to = (int) weights[pos++];
			double weight = weights[pos++];
			
			links.add(new NEATLink(from, to, weight));
		}
		
		for(int i = 0 ; i < nActivations ; i++) {
			activations[i]=new ActivationSteepenedSigmoid();
		}
		
		return new NEATContinuousNetwork(inputs, outputs, links, activations, neurons);
	}
}
