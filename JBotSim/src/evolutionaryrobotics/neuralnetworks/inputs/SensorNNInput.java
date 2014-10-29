package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;

public class SensorNNInput extends NNInput {

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
		return coneTypeSensor == null ? 1 : coneTypeSensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		if(coneTypeSensor == null){
			if(sensor.isEnabled())
				return sensor.getSensorReading(index);
			return 0;
		}else{
			if(coneTypeSensor.isEnabled())
				return coneTypeSensor.getSensorReading(index);
			return 0;
		}
	}
}