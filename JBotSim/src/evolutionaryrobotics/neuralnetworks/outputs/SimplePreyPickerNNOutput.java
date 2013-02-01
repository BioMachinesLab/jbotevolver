package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.SimplePreyPickerActuator;
import simulation.util.Arguments;

public class SimplePreyPickerNNOutput implements NNOutput {
	private SimplePreyPickerActuator preyPicker;
	
	public SimplePreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		this.preyPicker = (SimplePreyPickerActuator)preyPicker;
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
			preyPicker.drop();
		}
	}

	@Override
	public void apply() {		
	}
}