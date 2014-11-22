package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class WaypointNNInput extends NNInput{
	
	public WaypointNNInput(Sensor s) {
		super(s);
	}

	@Override
	public int getNumberOfInputValues() {
		return 2;
	}

	@Override
	public double getValue(int index) {
		return sensor.getSensorReading(index);
	}

}
