package neat.continuous;

import java.io.Serializable;
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
	private double[] states;
	private double timeStep = 0.2;
	private double tau      = 2.5;
	private List<NEATNeuronGene> neurons;

	/**
	 * Construct a NEAT network. The links that are passed in also define the
	 * neurons.
	 * 
	 * @param inputNeuronCount
	 *            The input neuron count.
	 * @param outputNeuronCount
	 *            The output neuron count.
	 * @param connectionArray
	 *            The links.
	 * @param theActivationFunctions
	 *            The activation functions.
	 */
	public NEATContinuousNetwork(final int inputNeuronCount, final int outputNeuronCount,
			final List<NEATLink> connectionArray, final ActivationFunction[] theActivationFunctions, List<NEATNeuronGene> neurons) {
		super(inputNeuronCount,outputNeuronCount,connectionArray,theActivationFunctions);

		final int neuronCount = theActivationFunctions.length;

		this.preActivation = new double[neuronCount];
		this.postActivation = new double[neuronCount];
		this.decays = new double[neuronCount];
		this.states = new double[neuronCount];
		
		this.neurons = neurons;

		// bias
		this.postActivation[0] = 1.0;
		
		try{
		
			int index = 0;
			
		for(NEATNeuronGene gene : neurons) {
			if(gene instanceof NEATContinuousNeuronGene) {
				NEATContinuousNeuronGene continuousGene = (NEATContinuousNeuronGene)gene;
				double decay = FastMath.powQuick(10, (-1.0 + (tau * ((continuousGene).getDecay()) + 10.0) / 20));
				decays[index] = decay;
			}
			index++;
		}
		}catch(Exception e)  {
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
		
		loadStates();
		calculateDecay();

		// iterate through the network activationCycles times
//		for (int i = 0; i < this.activationCycles; ++i) {
		internalCompute();
//		}

		saveStates();
		
		// copy output
		EngineArray.arrayCopy(this.postActivation, this.getOutputIndex(), result.getData(), 0, this.getOutputCount());
		
		return result;
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
	
	private void loadStates() {
		for(int i = 0 ; i < decays.length ; i++) {
			if(decays[i] != 0) {
				preActivation[i] = states[i];
			}
		}
	}
	
	private void saveStates() {
		for(int i = 0 ; i < decays.length ; i++) {
			if(decays[i] != 0) {
				//first postActivation is bias
				states[i] = postActivation[i+1];
			}
		}
	}

	private void calculateDecay() {
		for(int i = 0 ; i < decays.length ; i++) {
			if(decays[i] != 0) {
				preActivation[i]*= timeStep/decays[i];
			}
		}
	}
	
	/**
	 * Perform one activation cycle.
	 */
	private void internalCompute() {
		for (int j = 0; j < this.getLinks().length; j++) {
			this.preActivation[this.getLinks()[j].getToNeuron()] += this.postActivation[this.getLinks()[j]
					.getFromNeuron()] * this.getLinks()[j].getWeight();
		}

		for (int j = this.getOutputIndex(); j < this.preActivation.length; j++) {
			this.postActivation[j] = this.preActivation[j];
			this.getActivationFunctions()[j].activationFunction(this.postActivation,
					j, 1);
			this.preActivation[j] = 0.0F;
		}
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
	
	public List<NEATNeuronGene> getNeurons() {
		return neurons;
	}
}