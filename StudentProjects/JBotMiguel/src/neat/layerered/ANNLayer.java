package neat.layerered;

import java.io.Serializable;
import java.util.ArrayList;

import neat.layerered.continuous.ANNNeuronContinuous;

public class ANNLayer implements Serializable {

	private static final long serialVersionUID = -4535259753222438787L;
	protected int id;
	protected ArrayList<ANNNeuron> layerNeurons;
	
	public ANNLayer(int id, ArrayList<ANNNeuron> layerNeurons) {
		this.id = id;
		this.layerNeurons = layerNeurons;
	}
	
	public int getLayerId(){
		return this.id;
	}
	
	public void step(){
		for(ANNNeuron n : layerNeurons){
			n.step();
		}
	}
	
	public double[] getNeuronActivations(){
		double[] values = new double[layerNeurons.size()];
		for(int i = 0; i < values.length; i++){
			values[i] = layerNeurons.get(i).getActivationValue();
		}
		return values;
	}

	public void reset() {
		for(ANNNeuron n : this.layerNeurons)
			n.reset();
	}

	public ArrayList<ANNNeuron> getNeurons() {
		return this.layerNeurons;
	}
	
	public boolean isHiddenLayer() {
		for(ANNNeuron n : this.layerNeurons) {
			if(!(n.getType() == ANNNeuron.HIDDEN_NEURON))
				return false;
		}
		return true;
	}
}
