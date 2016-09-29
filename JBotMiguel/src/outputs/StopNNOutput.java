package outputs;

import actuators.StopActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class StopNNOutput extends NNOutput {
	
	private StopActuator sa;
	
	public StopNNOutput(Actuator actuator, Arguments args){
		super(actuator, args);
		sa = (StopActuator)actuator;
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
