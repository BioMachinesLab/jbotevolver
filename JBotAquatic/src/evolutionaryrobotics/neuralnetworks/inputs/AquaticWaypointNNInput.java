package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensor.WaypointSensor;
import simulation.robot.sensors.Sensor;

public class AquaticWaypointNNInput extends NNInput {

	public AquaticWaypointNNInput(Sensor s) {
		super(s);
	}

	@Override
	public int getNumberOfInputValues() {
		return 2;
	}

	@Override
	public double getValue(int index) {
		if (index == 0) {
			return sensor.getSensorReading(index) / 360.0 + 0.5;
		} else {
			double range = ((WaypointSensor) sensor).getRange();
			double distance = sensor.getSensorReading(index);

			if (distance > range)
				return 0;
			else
				return (range - distance) / range;

		}
	}
}
