package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.SimpleRobotColorSensor;

public class SimpleRobotColorNNInput extends NNInput {
	SimpleRobotColorSensor robotColorSensor;
	
	public SimpleRobotColorNNInput(Sensor robotColorSensor) {
		this.robotColorSensor = (SimpleRobotColorSensor) robotColorSensor;
	}

//	@Override
	public int getNumberOfInputValues() {
		return robotColorSensor.getNumberOfSensors();
	}

//	@Override
	public double getValue(int index) {
		return robotColorSensor.getSensorReading(index);
	}
}
