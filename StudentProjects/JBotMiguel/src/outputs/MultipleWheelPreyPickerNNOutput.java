package outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;
import actuators.MultipleWheelPreyPickerActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class MultipleWheelPreyPickerNNOutput extends NNOutput {
	private MultipleWheelPreyPickerActuator preyPicker;
	
	public MultipleWheelPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (MultipleWheelPreyPickerActuator)preyPicker;
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
	public void apply() {		}
}