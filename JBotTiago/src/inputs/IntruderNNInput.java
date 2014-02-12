package inputs;

import sensors.IntruderSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class IntruderNNInput extends NNInput {

	private IntruderSensor sensor;
	
	public IntruderNNInput(Sensor s) {
		super(s);
		sensor = (IntruderSensor) s;
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
