package evolutionaryrobotics.neuralnetworks.inputs;


public class SysoutNNInput extends NNInput {

	NNInput nnInput;
	public SysoutNNInput(NNInput nnInput) {
		this.nnInput = nnInput;
	}
	
	public int getNumberOfInputValues() {
		return nnInput.getNumberOfInputValues();
	}

//	@Override
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
