package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.EpuckLightSensor;
import simulation.robot.sensors.Sensor;

public class EpuckLightNNInput extends NNInput{
	
	private EpuckLightSensor sensor;
	
	public EpuckLightNNInput(Sensor sensor) {
		super(sensor);
		this.sensor = (EpuckLightSensor)sensor;
	}

	@Override
	public int getNumberOfInputValues() {
		return sensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return sensor.getSensorReading(index);
	}
}