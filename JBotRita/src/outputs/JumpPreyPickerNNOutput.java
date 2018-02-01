package outputs;

import actuator.JumpPreyPickerActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;


public class JumpPreyPickerNNOutput extends NNOutput {
	private JumpPreyPickerActuator preyPicker;
	
	public JumpPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (JumpPreyPickerActuator) preyPicker;
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