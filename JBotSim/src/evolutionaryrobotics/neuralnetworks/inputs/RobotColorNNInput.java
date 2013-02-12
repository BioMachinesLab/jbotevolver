package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.RobotColorSensor;
import simulation.robot.sensors.Sensor;

public class RobotColorNNInput extends NNInput {
	RobotColorSensor robotColorSensor;
	
	public RobotColorNNInput(Sensor robotColorSensor) {
		super(robotColorSensor);
		this.robotColorSensor = (RobotColorSensor) robotColorSensor;
	}

	@Override
	public int getNumberOfInputValues() {
		return robotColorSensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return robotColorSensor.getSensorReading(index);
	}
}