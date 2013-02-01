package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.RobotRGBColorSensor;
import simulation.robot.sensors.Sensor;

public class RobotRGBColorNNInput extends NNInput {
	RobotRGBColorSensor robotRGBColorSensor;
	
	public RobotRGBColorNNInput(Sensor robotColorSensor) {
		this.robotRGBColorSensor = (RobotRGBColorSensor) robotColorSensor;
	}

//	@Override
	public int getNumberOfInputValues() {
		return robotRGBColorSensor.getNumberOfSensors();
	}

//	@Override
	public double getValue(int index) {
		return robotRGBColorSensor.getSensorReading(index);
	}
}
