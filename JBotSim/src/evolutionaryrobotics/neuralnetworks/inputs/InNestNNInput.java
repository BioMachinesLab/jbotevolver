package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.InNestSensor;
import simulation.robot.sensors.Sensor;

public class InNestNNInput extends NNInput {
	InNestSensor sensor;
	
	public InNestNNInput(Sensor sensor) {
		this.sensor = (InNestSensor) sensor;
	}
	
//	@Override
	public int getNumberOfInputValues() {	
		return 1;
	}

//	@Override
	public double getValue(int index) {
		if (sensor.isInNest())
			return 1;
		else
			return 0;
	}

}
