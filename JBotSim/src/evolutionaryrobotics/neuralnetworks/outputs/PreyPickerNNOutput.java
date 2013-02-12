package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;

public class PreyPickerNNOutput extends NNOutput {
	private PreyPickerActuator preyPicker;
	
	public PreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (PreyPickerActuator)preyPicker;
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