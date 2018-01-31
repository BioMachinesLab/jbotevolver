package outputs;

import actuator.RotationActuator;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;
import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;

public class RotationNNOutput extends NNOutput {
	private RotationActuator rotation;

	private double speed = 0;
	
	public RotationNNOutput(Actuator rotation, Arguments args) {
		super(rotation,args);
		this.rotation  = (RotationActuator)rotation;
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return 1;
	}

	@Override
	public void setValue(int output, double value) {		
			speed = value;		
	}

	@Override
	public void apply() {
		rotation.setLeftWheelSpeed(speed);
		rotation.setRightWheelSpeed(speed);
	}
}
