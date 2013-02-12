package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SysoutNNInput extends NNInput {

	private NNInput nnInput;
	
	public SysoutNNInput(Simulator simulator, Robot robot, Arguments args) {
		super(null);
		String newName = args.getArgumentAt(0);
		Arguments newArgs = new Arguments(args.getArgumentAsString(newName));
		this.nnInput = NNInput.createInput(simulator, robot, newName, newArgs);
	}
	
	public SysoutNNInput(NNInput nnInput) {
		super(null);
		this.nnInput = nnInput;
	}
	
	public int getNumberOfInputValues() {
		return nnInput.getNumberOfInputValues();
	}

	@Override
	public double getValue(int index) {
		if (index == nnInput.getNumberOfInputValues()-1) {
			System.out.print(nnInput + ": ");
			for (int i = 0; i < nnInput.getNumberOfInputValues(); i++)
			{
				System.out.print("[" + i + "] = " + nnInput.getValue(i));
				if (i != nnInput.getNumberOfInputValues() - 1)
					System.out.print(", ");
				else 
					System.out.println("");
			}
		}			
		return nnInput.getValue(index);
	}
}