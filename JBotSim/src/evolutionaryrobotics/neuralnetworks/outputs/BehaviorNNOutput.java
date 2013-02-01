package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.BehaviorActuator;
import simulation.util.Arguments;

public class BehaviorNNOutput implements NNOutput {
	
	private BehaviorActuator actuator;
	
	public BehaviorNNOutput(Actuator b, Arguments arg) {
		this.actuator = (BehaviorActuator)b;
	}

	@Override
	public int getNumberOfOutputValues() {
		return actuator.getNumberOfOutputs();
	}

	@Override
	public void setValue(int index, double value) {
		actuator.setValue(index, value);
	}

	@Override
	public void apply() {
		// TODO Auto-generated method stub
	}
}