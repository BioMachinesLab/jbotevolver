package outputs;

import roommaze.SoundActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class SoundNNOutput extends NNOutput {
	
	private SoundActuator sa;
	
	public SoundNNOutput(Actuator actuator, Arguments args){
		super(actuator, args);
		sa = (SoundActuator)actuator;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		sa.setActivation(value);		
	}

	@Override
	public void apply() {	}
}