package sensors;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class OpenDoorNNInput extends NNInput {
	
	private OpenDoorSensor sensor;

	public OpenDoorNNInput(Sensor s) {
		super(s);
		sensor = (OpenDoorSensor)s;
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