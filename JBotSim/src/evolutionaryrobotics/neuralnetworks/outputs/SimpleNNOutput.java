package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.util.Arguments;

public class SimpleNNOutput extends NNOutput {
	
	private double[] values;
	
	public SimpleNNOutput(Arguments args) {
		super(null,args);
		values = new double[args.getArgumentAsInt("numberofoutputs")];
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