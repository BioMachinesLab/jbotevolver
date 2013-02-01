package evolutionaryrobotics.neuralnetworks.outputs;

public class SimpleNNOutput implements NNOutput{
	
	private double[] values;
	
	public SimpleNNOutput(int numberOfOuputs) {
		values = new double[numberOfOuputs];
	}

	@Override
	public int getNumberOfOutputValues() {
		return values.length;
	}

	@Override
	public void setValue(int index, double value) {
		values[index] = value;
	}

	@Override
	public void apply() {}

}
