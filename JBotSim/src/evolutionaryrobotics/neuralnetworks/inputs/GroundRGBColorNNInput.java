package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.GroundRGBColorSensor;
import simulation.robot.sensors.Sensor;

public class GroundRGBColorNNInput extends NNInput {
	GroundRGBColorSensor groundRGBColorSensor;
	
	public GroundRGBColorNNInput(Sensor robotColorSensor) {
		this.groundRGBColorSensor = (GroundRGBColorSensor) robotColorSensor;
	}

//	@Override
	public int getNumberOfInputValues() {
		return groundRGBColorSensor.getNumberOfSensors();
	}

//	@Override
	public double getValue(int index) {
		return groundRGBColorSensor.getSensorReading(index)/255.0;
	}
}
