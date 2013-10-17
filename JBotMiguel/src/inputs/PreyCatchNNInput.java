package inputs;

import sensors.PreyCatchSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class PreyCatchNNInput extends NNInput {
	
	private PreyCatchSensor sensor;
	
	public PreyCatchNNInput(Sensor s) {
		super(s);
		sensor = (PreyCatchSensor)s;
	}

	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return sensor.getSensorReading(index);
	}
}