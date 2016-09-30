package outputs;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import actuator.ShareWithActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class ShareWithNNOutput extends NNOutput {

	private ShareWithActuator shareWithActuator;
	private double shareWith = 0;
	
	public ShareWithNNOutput(Actuator actuator, Arguments args) {
		super(actuator, args);
		shareWithActuator = (ShareWithActuator)actuator;
	}

	@Override
	public int getNumberOfOutputValues() {
		return shareWithActuator.getNumberOfOutputs();
	}

	@Override
	public void setValue(int index, double value) {
		shareWith = value;
	}

	@Override
	public void apply() {
		shareWithActuator.setShareWith(shareWith);
	}

}
