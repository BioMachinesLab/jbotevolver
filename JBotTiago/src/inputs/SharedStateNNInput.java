package inputs;

import sensors.SharedStateSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class SharedStateNNInput extends NNInput {

	private SharedStateSensor sensor;
	
	public SharedStateNNInput(Sensor s) {
		super(s);
		sensor = (SharedStateSensor) s;
	}

	@Override
	public int getNumberOfInputValues() {
		return sensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return sensor.getSensorReading(index);
	}

}
