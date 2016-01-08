package fourwheeledrobot;

import simulation.robot.actuators.Actuator;
import simulation.util.Arguments;
import evolutionaryrobotics.neuralnetworks.outputs.NNOutput;

public class MultipleWheelAxesNNOutput extends NNOutput {
	
	private MultipleWheelAxesActuator actuator;
	
	private double[] speed;
	private double[] rotation;
	
	public MultipleWheelAxesNNOutput(Actuator actuator, Arguments args) {
		super(actuator,args);
		this.actuator  = (MultipleWheelAxesActuator)actuator;
		speed = new double[this.actuator.getNumberOfSpeeds()];
		rotation = new double[this.actuator.getNumberOfRotations()];
	}
	
	@Override
	public int getNumberOfOutputValues() {
		return speed.length + rotation.length;
	}

	@Override
	public void setValue(int output, double value) {
		if(output < speed.length)
			speed[output] = value;
		else
			rotation[output - speed.length] = value;
	}
	
	@Override
	public void apply() {
		actuator.setWheelSpeed(speed);
		actuator.setRotation(rotation);
	}
}