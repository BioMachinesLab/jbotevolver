package outputs;

import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.robot.sensors.PreyCarriedSensor;
import simulation.util.Arguments;
import actuator.IntensityPreyPickerActuator;
import actuator.Jump_LimitIntensityPreyPickerActuator;

public class Jump_LimitIntensityPreyPickerNNOutput extends NNOutput{
	private Jump_LimitIntensityPreyPickerActuator preyPicker;
	
	public Jump_LimitIntensityPreyPickerNNOutput(Actuator preyPicker, Arguments args) {
		super(preyPicker,args);
		this.preyPicker = (Jump_LimitIntensityPreyPickerActuator)preyPicker;
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
