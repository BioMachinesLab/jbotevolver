package outputs;

import actuator.Jump_PreyPickerActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;


public class Jump_PreyPickerNNOutput extends NNOutput {
	private Jump_PreyPickerActuator preyPicker;
	
	public Jump_PreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (Jump_PreyPickerActuator) preyPicker;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		if (value > 0.5) {
			preyPicker.pick();
		} else {
			preyPicker.drop();
		}
	}

	@Override
	public void apply() {}
}