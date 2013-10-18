package outputs;

import actuator.ChangeSensorActuator;
import actuator.FakeActuator;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class FakeNNOutput extends NNOutput {
	
	private FakeActuator fakeActuator;
	private double value;

	public FakeNNOutput(Actuator actuator, Arguments args) {
		super(actuator, args);
		fakeActuator = (FakeActuator)actuator;
	}

	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int index, double value) {
		this.value = value;
	}

	@Override
	public void apply() {
		fakeActuator.setValue(value);
	}

}
