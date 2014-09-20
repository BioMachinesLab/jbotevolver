package neat;

import org.encog.ml.MLMethod;

import evolutionaryrobotics.neuralnetworks.NeuralNetwork;

public abstract class WrapperNetwork extends NeuralNetwork{
	
	public abstract void setNetwork(MLMethod network);

}
