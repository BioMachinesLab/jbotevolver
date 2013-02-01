package evolutionaryrobotics.neuralnetworks.inputs;

public class DummyNNInput extends NNInput{
	
	private double value;

	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return value;
	}
	
	public void setInput(double inputValue) {
		this.value = inputValue;
	}

}
