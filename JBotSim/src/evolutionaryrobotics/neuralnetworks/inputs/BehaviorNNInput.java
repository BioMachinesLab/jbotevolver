package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.Robot;
import simulation.robot.sensors.BehaviorSensor;
import simulation.robot.sensors.Sensor;

public class BehaviorNNInput extends NNInput {
	BehaviorSensor sensor;
	Robot robot;
	public BehaviorNNInput(Sensor sensor, Robot r) {
		this.sensor = (BehaviorSensor) sensor;
		this.robot = r;
	}
	
	@Override
	public int getNumberOfInputValues() {	
		return sensor.getNumberOfBehaviors();
	}

	@Override
	public double getValue(int index) {
		return sensor.getSensorReading(index);
	}

}
