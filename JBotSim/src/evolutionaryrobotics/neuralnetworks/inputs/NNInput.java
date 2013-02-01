package evolutionaryrobotics.neuralnetworks.inputs;

import java.io.Serializable;

public abstract class NNInput implements Serializable {
	public abstract int getNumberOfInputValues();
	public abstract double getValue(int index);
}
