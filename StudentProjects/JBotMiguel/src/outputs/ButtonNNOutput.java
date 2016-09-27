package outputs;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuators.ButtonActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class ButtonNNOutput extends NNOutput {
	
	private ButtonActuator ba;
	
	public ButtonNNOutput(Actuator actuator, Arguments args){
		super(actuator, args);
		ba = (ButtonActuator)actuator;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		ba.setActivation(value);		
	}

	@Override
	public void apply() {	}

}
