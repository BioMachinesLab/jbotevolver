package nnoutputs;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuators.IntensityPreyPickerActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class IntensityPreyPickerNNOutput extends NNOutput {
	private IntensityPreyPickerActuator preyPicker;
	
	public IntensityPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (IntensityPreyPickerActuator) preyPicker;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 0;
	}

	@Override
	public void setValue(int index, double value) {
	}

	@Override
	public void apply() {		}
}