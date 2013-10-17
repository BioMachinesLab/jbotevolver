package sensors;

import evolutionaryrobotics.neuralnetworks.inputs.NNInput;
import simulation.robot.sensors.RobotColorSensor;
import simulation.robot.sensors.Sensor;

public class RobotOrientationNNInput extends NNInput {
	RobotOrientationSensor robotSensor;
	
	public RobotOrientationNNInput(Sensor robotSensor) {
		super(robotSensor);
		this.robotSensor = (RobotOrientationSensor) robotSensor;
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