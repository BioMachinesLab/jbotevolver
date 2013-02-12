package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.DoubleParameterSensor;
import simulation.robot.sensors.Sensor;

public class DoubleParameterNNInput extends NNInput {
	DoubleParameterSensor sensor;
	
	public DoubleParameterNNInput(Sensor sensor) {
		super(sensor);
		this.sensor = (DoubleParameterSensor) sensor;
	}
	
	@Override
	public int getNumberOfInputValues() {	
		return 1;
	}

	@Override
	public double getValue(int index) {
		return (sensor.getSensorReading(index) - sensor.getMinimumValue()) / (sensor.getMaximumValue() - sensor.getMinimumValue());
	}
}