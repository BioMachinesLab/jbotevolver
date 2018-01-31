package outputs;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuator.LimitIntensityPreyPickerActuator;

public class LimitIntensityPreyPickerNNOutput extends IntensityPreyPickerNNOutput{
	
	public LimitIntensityPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (LimitIntensityPreyPickerActuator)preyPicker;
	}

}
