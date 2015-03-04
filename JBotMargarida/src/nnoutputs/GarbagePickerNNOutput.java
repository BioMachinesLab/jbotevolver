package nnoutputs;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import actuators.GarbagePickerActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class GarbagePickerNNOutput extends NNOutput {
	private GarbagePickerActuator garbagePicker;
	
	public GarbagePickerNNOutput(Actuator garbagePicker, Arguments args) {
		super(garbagePicker,args);
		this.garbagePicker = (GarbagePickerActuator)garbagePicker;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		if (value > 0.5) {
			garbagePicker.down();
		} else {
			garbagePicker.up();
		}
	}

	@Override
	public void apply() {		}
}