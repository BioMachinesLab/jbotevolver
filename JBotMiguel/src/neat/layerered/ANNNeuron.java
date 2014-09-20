package neat.layerered;

import java.io.Serializable;
import java.util.ArrayList;

import org.encog.engine.network.activation.ActivationFunction;

import evolutionaryrobotics.neuralnetworks.inputs.SysoutNNInput;

public class ANNNeuron implements Comparable<ANNNeuron>,Serializable {

	protected ActivationFunction activationFunction;
	protected double activation = 0;
	protected long id;
	protected ArrayList<ANNNeuron> incomingNeurons;
	protected ArrayList<ANNSynapse> incomingSynapses;
	protected int depth = -1;
	protected int type;
	
	public static final int INPUT_NEURON = 1;
	public static final int HIDDEN_NEURON = 2;
	public static final int OUTPUT_NEURON = 3;
	public static final int BIAS_NEURON = 4;


	public ANNNeuron(long id, int type, ActivationFunction function){
		this.id = id;
		this.type = type;
		this.activationFunction = function;
		this.incomingNeurons = new ArrayList<ANNNeuron>();
		this.incomingSynapses = new ArrayList<ANNSynapse>();
	}

	public void step() {
		if(this.type == ANNNeuron.INPUT_NEURON 
				|| this.type == ANNNeuron.BIAS_NEURON)
			return;
		
		//a neuron that performs a weighted sum of the inputs
		double currentActivation = 0;
		for(int i = 0; i < this.incomingSynapses.size(); i++){
			currentActivation += incomingSynapses.get(i).getWeight() 
					* incomingNeurons.get(i).getActivationValue();
		}
		//activate
		double[] value = new double[]{currentActivation};
		activationFunction.activationFunction(value, 0, 1);
		this.activation = value[0];
	}

	public double getActivationValue() {
		return activation;
	}

	//only used for setting the "activation value" of the input neurons.
	public void setActivationValue(double newActivationValue){
		if(this.type == ANNNeuron.INPUT_NEURON 
				|| this.type == ANNNeuron.BIAS_NEURON){
			this.activation = newActivationValue;
		}
		else {
			try {
				throw new Exception("invalid operation");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void reset(){
		this.activation = 0;
	}
	
	public long getId(){
		return id;
	}
	
	public int getType(){
		return this.type;
	}

	public void addIncomingNeuron(ANNNeuron n){
		this.incomingNeurons.add(n);
	}

	public void addIncomingSynapse(ANNSynapse s){
		this.incomingSynapses.add(s);
	}

	public int getNeuronDepth() {
		return this.depth;
	}

	public void setNeuronDepth(int depth) {
		this.depth = depth;
	}

	public ArrayList<ANNNeuron> getIncomingNeurons() {
		return this.incomingNeurons;
	}

	public ArrayList<ANNSynapse> getIncomingConnections() {
		return this.incomingSynapses;
	}
	
	public int compareTo(ANNNeuron o) {
		return (int)(this.getId() - o.getId());
	}

	public ANNNeuron shallowCopy() {
		ANNNeuron copy = new ANNNeuron(this.id, this.type, this.activationFunction.clone());
		copy.setNeuronDepth(depth);
		
		return copy;
	}
}