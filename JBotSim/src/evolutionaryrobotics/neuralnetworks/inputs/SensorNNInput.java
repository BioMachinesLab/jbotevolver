package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;

public class SensorNNInput extends NNInput {

	private Sensor sensor;
	private ConeTypeSensor coneTypeSensor;
	
	public SensorNNInput(Sensor sensor) {
		super(sensor);
		
		if(sensor instanceof ConeTypeSensor)
			this.coneTypeSensor = (ConeTypeSensor)sensor;
		else
			this.sensor = sensor;
	}
	
	@Override
	public int getNumberOfInputValues() {
		return sensor == null ? coneTypeSensor.getNumberOfSensors() : 1;
	}

	@Override
	public double getValue(int index) {
		if(sensor == null){
			if(coneTypeSensor.isEnabled())
				return coneTypeSensor.getSensorReading(index);
			return 0;
		}else{
			if(sensor.isEnabled())
				return sensor.getSensorReading(index);
			return 0;
		}
	}
}