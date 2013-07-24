package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.WallSensor;

public class WallNNInput extends NNInput {

	private WallSensor wallSensor;

	public WallNNInput(Sensor wallSensor) {
		super(wallSensor);
		this.wallSensor = (WallSensor) wallSensor;
	}

	@Override
	public int getNumberOfInputValues() {
		return wallSensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return wallSensor.getSensorReading(index);
	}
}