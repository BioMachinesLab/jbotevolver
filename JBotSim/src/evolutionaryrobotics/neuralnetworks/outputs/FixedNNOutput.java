package evolutionaryrobotics.neuralnetworks.outputs;

public class FixedNNOutput implements NNOutput {

	NNOutput nnOutput;
	double   value;

	public FixedNNOutput(NNOutput nnOutput, double value) {
		super();
		this.value    = value;
		this.nnOutput = nnOutput;
	}

//	@Override
	public int getNumberOfOutputValues() {
		return nnOutput.getNumberOfOutputValues();
	}

//	@Override
	public void setValue(int index, double value) {
		nnOutput.setValue(index, this.value);
	}

	@Override
	public void apply() {
		nnOutput.apply();
	}
}
