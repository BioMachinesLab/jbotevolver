package inputs;

import sensors.RoomTimeSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class RoomTimeNNInput extends NNInput {
	
	private RoomTimeSensor sensor;
	
	public RoomTimeNNInput(Sensor s) {
		super(s);
		sensor = (RoomTimeSensor)s;
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