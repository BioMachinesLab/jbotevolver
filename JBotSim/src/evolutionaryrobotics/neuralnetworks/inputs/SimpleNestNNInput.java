package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.SimpleLightTypeSensor;

public class SimpleNestNNInput extends NNInput{

	private SimpleLightTypeSensor lightSensor;

	public SimpleNestNNInput(Sensor lightSensor) {
		super(lightSensor);
		this.lightSensor = (SimpleLightTypeSensor) lightSensor;
	}

	@Override
	public int getNumberOfInputValues() {
		return lightSensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return lightSensor.getSensorReading(index);
	}
}