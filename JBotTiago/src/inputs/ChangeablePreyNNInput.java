package inputs;

import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.SimpleLightTypeNNInput;


public class ChangeablePreyNNInput extends SimpleLightTypeNNInput {

	public ChangeablePreyNNInput(Sensor lightSensor) {
		super(lightSensor);
	}

}
