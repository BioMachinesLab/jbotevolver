package inputs;

import sensors.SharedSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class SharedNNInput extends NNInput {

	private SharedSensor sensor;
	
	public SharedNNInput(Sensor s) {
		super(s);
		sensor = (SharedSensor) s;
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
