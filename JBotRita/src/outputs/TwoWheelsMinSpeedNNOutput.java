package outputs;

import actuator.TwoWheelsMinSpeedActuator;
import simulation.robot.actuators.Actuator;
import simulation.robot.actuators.TwoWheelActuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class TwoWheelsMinSpeedNNOutput  extends NNOutput{
	
	private TwoWheelsMinSpeedActuator twoWheelActuator;
	private double leftSpeed = 0;
	private double rightSpeed = 0;
	
	public TwoWheelsMinSpeedNNOutput(Actuator twoWheelActuator, Arguments args) {
		super(twoWheelActuator,args);
		this.twoWheelActuator  = (TwoWheelsMinSpeedActuator)twoWheelActuator;
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
