package evolutionaryrobotics.neuralnetworks;

import java.io.Serializable;
import java.util.Random;
import java.util.Vector;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.Factory;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public abstract class NeuralNetwork implements Serializable{
	protected Vector<NNInput> inputs;
	protected Vector<NNOutput> outputs;

	protected boolean weightNoiseEnabled  = false;
	protected double  weightNoiseAmount   = 0.0;
	protected Random  weightNoiseRandom   = null;
	
	protected double[] weights;

	protected double[] inputNeuronStates;
	protected double[] outputNeuronStates;

	protected int numberOfInputNeurons    = 0;
	protected int numberOfOutputNeurons   = 0;
	protected int genomeLength = -1;
	
	protected boolean printValues = false;

	public void create(Vector<NNInput> inputs, Vector<NNOutput> outputs) {
		this.inputs = inputs;
		this.outputs = outputs;

		for (NNInput i : inputs) {
			numberOfInputNeurons += i.getNumberOfInputValues();
		}

		for (NNOutput i : outputs) {
			numberOfOutputNeurons += i.getNumberOfOutputValues();
		}

		inputNeuronStates  = new double[numberOfInputNeurons];
		outputNeuronStates = new double[numberOfOutputNeurons];
	}

	public int getNumberOfInputNeurons() {
		return numberOfInputNeurons;
	}

	public int getNumberOfOutputNeurons() {
		return numberOfOutputNeurons;
	}

	public void setWeights(double[] weights) {
		if (weights.length != genomeLength) {
			throw new IllegalArgumentException("Found " + weights.length + " weights, but need " + genomeLength + " for " + this.getClass() + " with " + numberOfInputNeurons + " input neurons and " + numberOfOutputNeurons + " output neurons.\nNNInputs: " + inputs + "\nOutputs: " + outputs);
		}
		
		this.weights = weights.clone();

		if (weightNoiseEnabled) {
			for (int i = 0; i < this.weights.length; i++)
				this.weights[i] += weightNoiseAmount * (2.0 * (weightNoiseRandom.nextDouble() - 0.5));
		}
	}

	public void controlStep(double time) {
		int currentInputValue = 0;
		//		boolean difZero=false;
		for (NNInput i : inputs) {
			for (int j = 0; j < i.getNumberOfInputValues(); j++) {
				inputNeuronStates[currentInputValue++] = i.getValue(j);				
				//				if (i.getValue(j)!=0)
				//					difZero=true;
			}
		}

		//		if (!difZero){
		//			System.out.println("ERROR - all zeros in the inputs");
		//		}
		outputNeuronStates = propagateInputs(inputNeuronStates);
		int currentOutputValue = 0;
		
		for (NNOutput o : outputs) {
			for (int j = 0; j < o.getNumberOfOutputValues(); j++) {
				o.setValue(j, outputNeuronStates[currentOutputValue++]);				
			}
			o.apply();
		}
	}

	protected abstract double[] propagateInputs(double[] inputValues);

	protected void setRequiredNumberOfWeights(int numberOfWeights) {
		this.genomeLength = numberOfWeights;		
	}

	public int getGenomeLength() {
		return genomeLength;
	}

	public void enableWeightNoise(double noiseAmount, Random random) {
		weightNoiseAmount  = noiseAmount;
		weightNoiseEnabled = true;
		weightNoiseRandom  = random;
	}
	
	public Vector<NNInput> getInputs() {
		return inputs;
	}
	
	public Vector<NNOutput> getOutputs() {
		return outputs;
	}
	
	public double[] getInputNeuronStates() {
		return inputNeuronStates;
	}
	
	public double[] getOutputNeuronStates() {
		return outputNeuronStates;
	}
	
	public void setInputs(Vector<NNInput> inputs) {
		this.inputs = inputs;
	}
	
	public double[] getWeights() {
		return weights;
	}
	
	public abstract void reset();
	
	public static NeuralNetwork getNeuralNetwork(Simulator simulator, Robot robot, Arguments arguments) {
		
		Vector<NNInput> inputs = NNInput.getNNInputs(simulator, robot, arguments);
		Vector<NNOutput> outputs = NNOutput.getNNOutputs(simulator, robot, arguments);
		
		if (!arguments.getArgumentIsDefined("classname"))
			throw new RuntimeException("Neural Network 'classname' not defined: "+ arguments.toString());
		
		return (NeuralNetwork)Factory.getInstance(arguments.getArgumentAsString("classname"),inputs,outputs,arguments);
	}
}