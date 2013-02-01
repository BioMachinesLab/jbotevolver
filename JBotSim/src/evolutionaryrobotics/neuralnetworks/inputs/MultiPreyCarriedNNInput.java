package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.MultyPreyCarriedSensor;
import simulation.robot.sensors.Sensor;

public class MultiPreyCarriedNNInput extends NNInput {
	MultyPreyCarriedSensor sensor;
	
	public MultiPreyCarriedNNInput(Sensor sensor) {
		this.sensor = (MultyPreyCarriedSensor) sensor;
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
