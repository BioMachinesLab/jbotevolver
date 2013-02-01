package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.MultiPreyPickerActuator;
import simulation.util.Arguments;

public class MultiPreyPickerNNOutput implements NNOutput {
	private MultiPreyPickerActuator preyPicker;
	
	public MultiPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		this.preyPicker = (MultiPreyPickerActuator)preyPicker;
	}

//	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

//	@Override
	public void setValue(int index, double value) {
		if (value > 0.5) {
			preyPicker.pick();
		} else {
			preyPicker.off();
		}
	}

	@Override
	public void apply() {		
	}
}