package evolutionaryrobotics.neuralnetworks.outputs;

public class SysoutNNOutput extends NNOutput {

	private NNOutput nnOutput;

	public SysoutNNOutput(NNOutput nnOutput) {
		super(null,null);
		this.nnOutput = nnOutput;
	}

	@Override
	public int getNumberOfOutputValues() {
		return nnOutput.getNumberOfOutputValues();
	}

	@Override
	public void setValue(int index, double value) {
		if (index == nnOutput.getNumberOfOutputValues()-1) {
			System.out.println(nnOutput + "["+index+"]: "+value);
		}			
		nnOutput.setValue(index, value);
	}

	@Override
	public void apply() {}
}