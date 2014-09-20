package neat.layerered;

import java.io.Serializable;
import java.util.ArrayList;

import org.encog.ml.MLError;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.util.simple.EncogUtility;

public class LayeredANN implements MLRegression, MLError, Serializable  {

	protected ANNLayer[] layers;
	protected int inputCount, outputCount;

	public LayeredANN(ANNLayer[] layers) {
		this.layers = layers;
		//-1 because of the bias neuron (final position of the layer)
		this.inputCount = layers[0].getNeurons().size() - 1;
		this.outputCount = layers[layers.length - 1].getNeurons().size();
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
				inputNeurons.get(i + biasOffset).setActivationValue(inputValues[i]);
			}
			//bias
			inputNeurons.get(biasIndex).setActivationValue(1.0);
		}
		catch(ArrayIndexOutOfBoundsException e){
			System.out.println("c1 - n");
		}
		catch(IndexOutOfBoundsException e){
			System.out.println("c1 - n2");
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

}
