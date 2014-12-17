package neat.tauneat;

import java.io.Serializable;
import java.util.List;

import org.encog.engine.network.activation.ActivationFunction;
import org.encog.ml.MLError;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.neat.NEATLink;
import org.encog.neural.neat.NEATPopulation;
import org.encog.util.EngineArray;
import org.encog.util.simple.EncogUtility;

public class TauNEATNetwork implements MLRegression, MLError, Serializable {

	/**
	 * The serial ID.
	 */
	private static final long serialVersionUID = 3660295468309926508L;

	/**
	 * The neuron links.
	 */
	private final TauNEATLink[] links;

	/**
	 * The activation functions.
	 */
	private final ActivationFunction[] activationFunctions;

	/**
	 * The pre-activation values, used to feed the neurons.
	 */
	private final double[] preActivation;

	/**
	 * The post-activation values, used as the output from the neurons.
	 */
	private final double[] postActivation;

	/**
	 * The index to the starting location of the output neurons.
	 */
	private final int outputIndex;

	/**
	 * The input count.
	 */
	private int inputCount;

	/**
	 * The output count.
	 */
	private int outputCount;

	/**
	 * The number of activation cycles to use.
	 */
	private int activationCycles = NEATPopulation.DEFAULT_CYCLES;

	/**
	 * True, if the network has relaxed and values no longer changing. Used when
	 * activationCycles is set to zero for auto.
	 */
	private boolean hasRelaxed = false;

	/**
	 * The amount of change allowed before the network is considered to have
	 * relaxed.
	 */
	private double relaxationThreshold;

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
	public TauNEATNetwork(final int inputNeuronCount, final int outputNeuronCount,
			final List<TauNEATLink> connectionArray,
			final ActivationFunction[] theActivationFunctions) {
		this.links = new TauNEATLink[connectionArray.size()];
		for (int i = 0; i < connectionArray.size(); i++) {
			this.links[i] = connectionArray.get(i);
		}

		this.activationFunctions = theActivationFunctions;
		final int neuronCount = this.activationFunctions.length;

		this.preActivation = new double[neuronCount];
		this.postActivation = new double[neuronCount];
		
		this.inputCount = inputNeuronCount;
		this.outputIndex = inputNeuronCount + 1;
		this.outputCount = outputNeuronCount;

		// bias
		this.postActivation[0] = 1.0;
	}

	/**
	 * Calculate the error for this neural network.
	 * 
	 * @param data
	 *            The training set.
	 * @return The error percentage.
	 */
	@Override
	public double calculateError(final MLDataSet data) {
		return EncogUtility.calculateRegressionError(this, data);
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
		
		final MLData result = new BasicMLData(this.outputCount);

		// clear from previous
		EngineArray.fill(this.preActivation, 0.0);
		EngineArray.fill(this.postActivation, 0.0);
		this.postActivation[0] = 1.0;

		// copy input
		EngineArray.arrayCopy(input.getData(), 0, this.postActivation, 1,
				this.inputCount);

		// iterate through the network activationCycles times
		for (int i = 0; i < this.activationCycles; ++i) {
			internalCompute();
		}

		// copy output
		EngineArray.arrayCopy(this.postActivation, this.outputIndex,
				result.getData(), 0, this.outputCount);
		
		updateLinks();

		return result;
	}
	
	/**
	 * Perform one activation cycle.
	 */
	private void internalCompute() {
		for (int j = 0; j < this.links.length; j++) {
			this.preActivation[this.links[j].getToNeuron()] += this.links[j].getCurrentValue() * this.links[j].getWeight();
		}

		for (int j = this.outputIndex; j < this.preActivation.length; j++) {
			this.postActivation[j] = this.preActivation[j];
			this.activationFunctions[j].activationFunction(this.postActivation,
					j, 1);
			this.preActivation[j] = 0.0F;
		}
	}
	
	private void updateLinks() {
		for(TauNEATLink link : links) {
			double currentValue = postActivation[link.getFromNeuron()];
			link.setNewValue(currentValue);
		}
	}

	/**
	 * @return The number of activation cycles to use.
	 */
	public int getActivationCycles() {
		return this.activationCycles;
	}

	/**
	 * @return The activation functions.
	 */
	public ActivationFunction[] getActivationFunctions() {
		return this.activationFunctions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInputCount() {
		return this.inputCount;
	}

	/**
	 * @return The links in the neural network.
	 */
	public NEATLink[] getLinks() {
		return this.links;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getOutputCount() {
		return this.outputCount;
	}

	/**
	 * @return The starting location of the output neurons.
	 */
	public int getOutputIndex() {
		return this.outputIndex;
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

	/**
	 * @return The amount of change allowed before the network is considered to
	 *         have relaxed.
	 */
	public double getRelaxationThreshold() {
		return this.relaxationThreshold;
	}

	/**
	 * @return True, if the network has relaxed and values no longer changing.
	 *         Used when activationCycles is set to zero for auto.
	 */
	public boolean isHasRelaxed() {
		return this.hasRelaxed;
	}

	/**
	 * Set the number of activation cycles to use.
	 * 
	 * @param activationCycles
	 *            The number of activation cycles.
	 */
	public void setActivationCycles(final int activationCycles) {
		this.activationCycles = activationCycles;
	}

	/**
	 * Set true, if the network has relaxed and values no longer changing. Used
	 * when activationCycles is set to zero for auto.
	 * 
	 * @param hasRelaxed
	 *            True if the network has relaxed.
	 */
	public void setHasRelaxed(final boolean hasRelaxed) {
		this.hasRelaxed = hasRelaxed;
	}

	/**
	 * The amount of change allowed before the network is considered to have
	 * relaxed.
	 * 
	 * @param relaxationThreshold
	 *            The relaxation threshold.
	 */
	public void setRelaxationThreshold(final double relaxationThreshold) {
		this.relaxationThreshold = relaxationThreshold;
	}

}
