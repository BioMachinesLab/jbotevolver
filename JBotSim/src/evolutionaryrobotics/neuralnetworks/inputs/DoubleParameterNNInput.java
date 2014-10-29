package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.DoubleParameterSensor;
import simulation.robot.sensors.Sensor;

public class DoubleParameterNNInput extends NNInput {
	private DoubleParameterSensor dpSensor;
	
	public DoubleParameterNNInput(Sensor sensor) {
		super(sensor);
		this.dpSensor = (DoubleParameterSensor) sensor;
	}
	
	@Override
	public int getNumberOfInputValues() {	
		return 1;
	}

	@Override
	public double getValue(int index) {
		return (dpSensor.getSensorReading(index) - dpSensor.getMinimumValue()) / (dpSensor.getMaximumValue() - dpSensor.getMinimumValue());
	}
}