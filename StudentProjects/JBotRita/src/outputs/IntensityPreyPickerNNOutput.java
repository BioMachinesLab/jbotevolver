package outputs;

import actuator.IntensityPreyPickerActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.PreyPickerActuator;
import simulation.util.Arguments;

public class IntensityPreyPickerNNOutput  extends NNOutput {
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
		if (value < 0.5) 
			
			preyPicker.dropPrey();
		
	}

	@Override
	public void apply() {	
		
	}
}
