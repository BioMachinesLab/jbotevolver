package evolutionaryrobotics.neuralnetworks.outputs;

import java.io.Serializable;

public interface NNOutput extends Serializable{
	public int getNumberOfOutputValues();
	public void setValue(int index, double value);
	public void apply();
}
