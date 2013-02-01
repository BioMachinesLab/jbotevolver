package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.PreyCarriedSensor;
import simulation.robot.sensors.Sensor;

public class PreyCarriedNNInput extends NNInput {
	PreyCarriedSensor sensor;
	
	public PreyCarriedNNInput(Sensor sensor) {
		this.sensor = (PreyCarriedSensor) sensor;
	}
	
//	@Override
	public int getNumberOfInputValues() {	
		return 1;
	}

//	@Override
	public double getValue(int index) {
		if (sensor.preyCarried())
			return 1;
		else
			return 0;
	}

}
