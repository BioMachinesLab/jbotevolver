
package outputs;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuator.Jump_IntensityPreyPickerActuator;

public class Jump_IntensityPreyPickerNNOutput extends NNOutput{
	private Jump_IntensityPreyPickerActuator preyPicker;
	
	public Jump_IntensityPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (Jump_IntensityPreyPickerActuator)preyPicker;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		if (value < 0.5) 
			preyPicker.dropPrey();
	}

	@Override
	public void apply() {	
		
	}
}