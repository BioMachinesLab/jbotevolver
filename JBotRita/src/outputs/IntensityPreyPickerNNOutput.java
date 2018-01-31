
package outputs;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuator.IntensityPreyPickerActuator;

public class IntensityPreyPickerNNOutput extends NNOutput{
	private IntensityPreyPickerActuator preyPicker;
	
	public IntensityPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (IntensityPreyPickerActuator)preyPicker;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		if (value > 0.5) 
			preyPicker.pick(true);
		else{
			preyPicker.pick(false);
		}
		
	}

	@Override
	public void apply() {	
		
	}
}