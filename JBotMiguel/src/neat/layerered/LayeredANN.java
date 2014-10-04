package neat.layerered;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import org.encog.ml.MLError;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.NeuralNetworkError;
import org.encog.util.simple.EncogUtility;

public class LayeredANN implements MLRegression, MLError, Serializable  {

	private static final long serialVersionUID = 3043031321598201086L ;
	protected ANNLayer[] layers;
	protected int inputCount, outputCount;

	public LayeredANN(ANNLayer[] layers) {
		this.layers = layers;
		//-1 because of the bias neuron (final position of the layer)
		this.inputCount = layers[0].getNeurons().size() - 1;
		this.outputCount = layers[layers.length - 1].getNeurons().size();
		
		if(layers[0].getNeurons().get(inputCount).getType() != ANNNeuron.BIAS_NEURON) {
			throw new NeuralNetworkError("Bias neuron is not in the correct place!");
		}
	}

	@Override
	public int getInputCount() {
		return this.inputCount;
	}

	@Override
	public int getOutputCount() {
		return this.outputCount;
	}

	@Override
	public double calculateError(final MLDataSet data) {
		return EncogUtility.calculateRegressionError(this, data);
	}

	public double[] compute(final double[] inputValues){

		ArrayList<ANNNeuron> inputNeurons = layers[0].getNeurons();

		//inputs		
		int biasIndex, biasOffset;
		biasIndex = this.inputCount;
		biasOffset = 0;

		try{
			for(int i = 0; i < inputValues.length; i++){
				ANNNeuron n = inputNeurons.get(i + biasOffset);
				if(n.getType() == ANNNeuron.HIDDEN_NEURON) {
					System.out.println(n.getNeuronDepth()+" "+Integer.MAX_VALUE);
					throw new NeuralNetworkError("Hidden neuron in the input layer!");
				}
				n.setActivationValue(inputValues[i]);
			}
			//bias
			inputNeurons.get(biasIndex).setActivationValue(1.0);
		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println("c1 - n");
		}
		catch(IndexOutOfBoundsException e){
			System.out.println("c1 - n2");
			e.printStackTrace();
		}
		//loop
		for(ANNLayer layer : layers)
			layer.step();

		//return output
		double[] outputValues = layers[layers.length - 1].getNeuronActivations();
		
		return outputValues;
	}

	@Override
	public MLData compute(final MLData input) {
		double[] inputValues = input.getData();

		double[] outputValues = this.compute(inputValues);

		final MLData result = new BasicMLData(this.outputCount);
		result.setData(outputValues);

		return result;
	}

	public int getNumberOfLayers() {
		return this.layers.length;
	}

	public ANNLayer getLayer(int index){
		return this.layers[index];
	}

	public ArrayList<ANNSynapse> getAllSynapses() {
		ArrayList<ANNSynapse> synapses = new ArrayList<ANNSynapse>();
		for(ANNLayer l : this.layers){
			for(ANNNeuron n : l.getNeurons()){
				synapses.addAll(n.getIncomingConnections());
			}
		}
		return synapses;
	}

	public ArrayList<ANNNeuron> getAllNeurons() {
		ArrayList<ANNNeuron> neurons = new ArrayList<ANNNeuron>();
		for(ANNLayer l : this.layers){
			neurons.addAll(l.getNeurons());
		}
		return neurons;
	}

	public void reset() {
		for(ANNLayer l : this.layers){
			for(ANNNeuron n : l.getNeurons()) {
				n.reset();
			}
		}
	}
	
	public LayeredANN copy(){
		//first, copy all neurons and synapses, then put them in the right place
		HashMap<Long, ANNNeuron> copyNeurons = new HashMap<Long, ANNNeuron>();
		HashMap<Long, ANNSynapse> copySynapse = new HashMap<Long, ANNSynapse>();

		for(ANNNeuron neuron : getAllNeurons()){
			copyNeurons.put(neuron.getId(), neuron.shallowCopy());
		}

		for(ANNSynapse synapse : getAllSynapses()){
			copySynapse.put(synapse.getInnovationNumber(), synapse.copy());
		}

		ANNLayer[] copyLayers = new ANNLayer[getNumberOfLayers()];

		for(int i = 0; i < getNumberOfLayers(); i++){
			ANNLayer originalLayer = getLayer(i);

			copyLayers[i] = recreateLayer(originalLayer, copyNeurons, copySynapse);
		}


		return new LayeredANN(copyLayers);
	}

	private ANNLayer recreateLayer(ANNLayer originalLayer, HashMap<Long, ANNNeuron> copyNeurons,
			HashMap<Long, ANNSynapse> copySynapses) {
		ArrayList<ANNNeuron> originalLayerNeurons = originalLayer.getNeurons();
		ArrayList<ANNNeuron> copyLayerNeurons = new ArrayList<ANNNeuron>(originalLayerNeurons.size());

		//start assembling the pieces
		for(ANNNeuron neuron : originalLayerNeurons){
			//find the right *copy neuron*
			ANNNeuron copyNeuron = copyNeurons.get(neuron.getId());
	
			//Search for and add the incoming neurons and incoming connections
			//for each incoming synapse, there is a corresponding incoming neuron
			for(int i = 0; i < neuron.getIncomingConnections().size(); i++){
				ANNSynapse originalIncSynapse = neuron.getIncomingConnections().get(i);
				ANNNeuron originalIncNeuron = neuron.getIncomingNeurons().get(i);
		
				//add to the copy neuron
				copyNeuron.addIncomingNeuron(copyNeurons.get(originalIncNeuron.getId()));
				copyNeuron.addIncomingSynapse(copySynapses.get(originalIncSynapse.getInnovationNumber()));
			}
			copyLayerNeurons.add(copyNeuron);
		}
		return new ANNLayer(originalLayer.getLayerId(), copyLayerNeurons);
	}
}