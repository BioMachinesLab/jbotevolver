package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.CompassSensor;
import simulation.robot.sensors.Sensor;

public class CompassNNInput extends NNInput {
	CompassSensor sensor;
	
	public CompassNNInput(Sensor sensor) {
		this.sensor = (CompassSensor) sensor;
	}
	
//	@Override
	public int getNumberOfInputValues() {	
		return 1;
	}

//	@Override
	public double getValue(int index) {
		return sensor.getSensorReading(index);
	}

}
