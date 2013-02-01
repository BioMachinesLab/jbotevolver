package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.PositionSensor;
import simulation.robot.sensors.Sensor;

public class PositionNNInput extends NNInput {
	PositionSensor sensor;
	
	public PositionNNInput(Sensor sensor) {
		this.sensor = (PositionSensor) sensor;
	}
	
//	@Override
	public int getNumberOfInputValues() {	
		return 2;
	}

//	@Override
	public double getValue(int index) {
		if (index == 0)
			return sensor.getSensorReading(index) / sensor.getEnvironmentWidth() + 0.5;
		else
			return sensor.getSensorReading(index) / sensor.getEnvironmentHeight() + 0.5;			
	
	}

}
