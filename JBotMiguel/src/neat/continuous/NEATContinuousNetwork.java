package neat.continuous;

import java.io.Serializable;
import java.io.ObjectInputStream.GetField;
import java.util.Arrays;
import java.util.List;

import net.jafama.FastMath;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.MLError;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATNetwork;
import org.encog.neural.neat.training.NEATNeuronGene;
import org.encog.util.EngineArray;

public class NEATContinuousNetwork extends NEATNetwork implements MLRegression, MLError, Serializable {

	/**
	 * The serial ID.
	 */
	private static final long serialVersionUID = 3660295468309926508L;

	/**
	 * The pre-activation values, used to feed the neurons.
	 */
	private final double[] preActivation;

	/**
	 * The post-activation values, used as the output from the neurons.
	 */
	private final double[] postActivation;

	private double[] decays;
	private double[] bias;
	
	private double[] states;
	private double[] currentStates;
	
	private double[] previousPreActivations;
	
	private double timeStep = 0.2;
	private double tau      = 2.5;
	private int numberOfNeurons = 0;
	
	//This is necessary because we cannot store the NeuronGene directly.
	//In Conillon, we need to serialize the classes, and the enum in NeuronGene
	//stops this, since enums cannot be serialized
	private Neuron[] backupNeurons;

	public NEATContinuousNetwork(final int inputNeuronCount, final int outputNeuronCount,
			final List<NEATLink> connectionArray, final ActivationFunction[] theActivationFunctions, List<NEATNeuronGene> neurons) {
		super(inputNeuronCount,outputNeuronCount,connectionArray,theActivationFunctions);

		final int neuronCount = theActivationFunctions.length;
		
		this.preActivation = new double[neuronCount];
		this.postActivation = new double[neuronCount];
		this.decays = new double[neuronCount];
		this.states = new double[neuronCount];
		this.currentStates = new double[neuronCount];
		this.numberOfNeurons = neuronCount;
		this.backupNeurons = new Neuron[neuronCount];
		this.states = new double[neuronCount];
		this.bias = new double[neuronCount];
		this.previousPreActivations = new double[neuronCount];
		
		int index = 0;
		
		try{
			
		for(NEATNeuronGene gene : neurons) {
			
			if(gene instanceof NEATContinuousNeuronGene) {
				NEATContinuousNeuronGene continuousGene = (NEATContinuousNeuronGene)gene;
				
				double decay = FastMath.powQuick(10, (-1.0 + (tau * ((continuousGene).getDecay() + 10.0) / 20)));
				decays[index] = decay;
				bias[index] = continuousGene.getBias();
				
				backupNeurons[index] = new Neuron(gene.getId(), gene.getNeuronType().ordinal(), decay, bias[index], gene.getInnovationId());
			} else {
				backupNeurons[index] = new Neuron(gene.getId(), gene.getNeuronType().ordinal(), gene.getInnovationId());
			}
			index++;
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Compute the output from this synapse.
	 * 
	 * @param input
	 *            The input to this synapse.
	 * @return The output from this synapse.
	 */
	@Override
	public MLData compute(final MLData input) {
		final MLData result = new BasicMLData(this.getOutputCount());

		// clear from previous
		EngineArray.fill(this.preActivation, 0.0);
		EngineArray.fill(this.postActivation, 0.0);
		this.postActivation[0] = 1.0;

		// copy input
		EngineArray.arrayCopy(input.getData(), 0, this.postActivation, 1, this.getInputCount());
		
		boolean keepComputing;
		int i = 0;
		//Keep computing all the internal connections until
		//every contributions taken into account
		do{
			i++;
			internalCompute();
			keepComputing = false;
			
			for(int j = 0 ; j < getOutputCount() ; j++) {
				if(preActivation[getOutputIndex()+j] != previousPreActivations[getOutputIndex()+j]) {
					keepComputing = true;
					break;
				}
			}
			
			//avoid possible infinite loops if the
			//states never stabilize due to rounding issues
			if(i > 1000)
				break;
			
			previousPreActivations = preActivation.clone();
		} while(keepComputing);

		saveStates();
		
		// copy output
		EngineArray.arrayCopy(this.postActivation, this.getOutputIndex(), result.getData(), 0, this.getOutputCount());
		
		return result;
	}
	
	public double[] compute(double[] input) {
		
		MLData data = new BasicMLData(input);
		return compute(data).getData();
	}
	
	private void saveStates() {
		for(int i = getOutputIndex() ; i < getDecays().length ; i++) {
			states[i] = currentStates[i];
			currentStates[i] = 0;
		}
	}
	
	/**
	 * Perform one activation cycle.
	 */
	private void internalCompute() {
		double[] decays = getDecays();
		
		for(int i = 0 ; i < decays.length ; i++) {
			this.preActivation[i] = 0.0F;
			if(decays[i] != 0) {
				preActivation[i] = -states[i];
			}
		}
		int contributions = 0;
		for (int j = 0; j < getLinks().length; j++) {
			if(this.postActivation[getLinks()[j].getFromNeuron()] * getLinks()[j].getWeight() != 0) {
				contributions++;
			}
			this.preActivation[getLinks()[j].getToNeuron()] += this.postActivation[getLinks()[j].getFromNeuron()] * getLinks()[j].getWeight();
		}
//		System.out.println(contributions);
		
		for(int j = getOutputIndex() ; j < preActivation.length; j++) {
			double decay = getDecays()[j];
			if(decay != 0) {
				decay = getTimeStep()/decay;
				//apply decay
				preActivation[j] = states[j] + preActivation[j]*decay;
				//save neuron state
				currentStates[j] = preActivation[j];
				//add bias for activation
				preActivation[j]+= bias[j];
			} else {
				preActivation[j] = states[j] + preActivation[j];
			}
		}
		
		//activate the neurons
		for (int j = getOutputIndex(); j < this.preActivation.length; j++) {
			this.postActivation[j] = this.preActivation[j];
			
			getActivationFunctions()[j].activationFunction(this.postActivation, j, 1);
			
			//bring down the pre activations to the 5th decimal place
			//in order to prevent excessive loops of the internal computation
			//function due to tiny variations caused by rounding issues
			preActivation[j] = ((int)(preActivation[j ]*100000.0))/100000.0;
			
			if(j-getOutputIndex() < getOutputCount()){
				currentStates[j] = postActivation[j];
			}
		}
	}

	/**
	 * @return The post-activation values, used as the output from the neurons.
	 */
	public double[] getPostActivation() {
		return this.postActivation;
	}
	
	/**
	 * @return The pre-activation values, used to feed the neurons.
	 */
	public double[] getPreActivation() {
		return this.preActivation;
	}
	
	public double[] getDecays() {
		return decays;
	}
	
	public double getTau() {
		return tau;
	}
	
	public double getTimeStep() {
		return timeStep;
	}
	
	public double[] getStates() {
		return states;
	}
	
	public void resetStates() {
		for(int i = 0 ; i < states.length ; i++)
			states[i] = 0;
	}
	
	public int getNumberOfNeurons() {
		return numberOfNeurons;
	}
	
	public Neuron[] getNeurons() {
		return backupNeurons;
	}
	
}