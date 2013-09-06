package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.RobotColorSensor;
import simulation.robot.sensors.RobotSensor;
import simulation.robot.sensors.Sensor;

public class RobotNNInput extends NNInput {
	RobotSensor robotSensor;
	
	public RobotNNInput(Sensor robotSensor) {
		super(robotSensor);
		this.robotSensor = (RobotSensor) robotSensor;
	}

	@Override
	public int getNumberOfInputValues() {
		return robotSensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return robotSensor.getSensorReading(index);
	}
}