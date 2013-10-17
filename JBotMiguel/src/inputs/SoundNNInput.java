package inputs;

import sensors.SoundSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class SoundNNInput extends NNInput {
	
	private SoundSensor sensor;
	
	public SoundNNInput(Sensor s) {
		super(s);
		sensor = (SoundSensor)s;
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
