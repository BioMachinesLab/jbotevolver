package evolutionaryrobotics.neuralnetworks.outputs;

import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.TwoWheelActuator;

public class TwoWheelNNOutput implements NNOutput {
	private TwoWheelActuator twoWheelActuator;
	private double leftSpeed = 0;
	private double rightSpeed = 0;
	
	public TwoWheelNNOutput(Actuator twoWheelActuator) {
		this.twoWheelActuator  = (TwoWheelActuator)twoWheelActuator;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 2;
	}

	@Override
	public void setValue(int output, double value) {
		if (output == 0)
			leftSpeed = value;
		else
			rightSpeed = value;
	}

	@Override
	public void apply() {
		twoWheelActuator.setLeftWheelSpeed(leftSpeed);
		twoWheelActuator.setRightWheelSpeed(rightSpeed);
	}
}