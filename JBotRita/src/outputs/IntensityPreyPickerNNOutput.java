
package outputs;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuator.IntensityPreyPickerActuator;

public class IntensityPreyPickerNNOutput extends NNOutput{
	protected IntensityPreyPickerActuator preyPicker;
	protected boolean pickBoolean;
	
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
			pickBoolean=true;
		else{
			pickBoolean=false;
		}
		
	}

	@Override
	public void apply() {	
		preyPicker.pick(pickBoolean);
	}
}