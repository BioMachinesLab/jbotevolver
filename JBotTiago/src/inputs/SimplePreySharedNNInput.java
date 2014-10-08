package inputs;

import sensors.SimplePreySharedSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class SimplePreySharedNNInput extends NNInput {

	private SimplePreySharedSensor sensor;
	
	public SimplePreySharedNNInput(Sensor s) {
		super(s);
		sensor = (SimplePreySharedSensor)s;
	}
	
	@Override
	public int getNumberOfInputValues() {
		return sensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		if(sensor.isEnabled())
			return sensor.getSensorReading(index);
		else
			return 0;
	}

}
