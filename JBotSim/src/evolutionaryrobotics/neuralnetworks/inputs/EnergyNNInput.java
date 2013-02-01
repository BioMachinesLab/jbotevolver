package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.EnergySensor;
import simulation.robot.sensors.Sensor;

public class EnergyNNInput extends NNInput {
	EnergySensor sensor;
	
	public EnergyNNInput(Sensor sensor) {
		this.sensor = (EnergySensor) sensor;
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
