package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.LightTypeSensor;
import simulation.robot.sensors.Sensor;

public class LightTypeNNInput extends NNInput{

	private LightTypeSensor lightSensor;

	public LightTypeNNInput(Sensor lightSensor) {
		super();
		this.lightSensor = (LightTypeSensor) lightSensor;
	}

	//@Override
	public int getNumberOfInputValues() {
		return lightSensor.getNumberOfSensors();
	}

	//@Override
	public double getValue(int index) {
		return lightSensor.getSensorReading(index);
	}

}
