package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class FixedNNOutput extends NNOutput {

	private NNOutput nnOutput;
	private double value;

	public FixedNNOutput(Simulator simulator, Robot robot, Arguments args) {
		super(null,args);
		value = args.getArgumentAsDouble("value");
		
		String newName = args.getArgumentAt(0);
		Arguments newArguments = new Arguments(args.getArgumentAsString(newName));
		
		nnOutput = NNOutput.createOutput(simulator, robot, newName, newArguments);
	}

	@Override
	public int getNumberOfOutputValues() {
		return nnOutput.getNumberOfOutputValues();
	}

	@Override
	public void setValue(int index, double value) {
		nnOutput.setValue(index, this.value);
	}

	@Override
	public void apply() {
		nnOutput.apply();
	}
}