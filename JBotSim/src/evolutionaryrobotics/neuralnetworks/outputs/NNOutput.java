package evolutionaryrobotics.neuralnetworks.outputs;

import java.io.Serializable;

public abstract class NNOutput implements Serializable{
	public abstract int getNumberOfOutputValues();
	public abstract void setValue(int index, double value);
	public abstract void apply();
}