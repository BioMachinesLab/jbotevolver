package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.PositionSensor;
import simulation.robot.sensors.Sensor;

public class PositionNNInput extends NNInput {
	private PositionSensor positionSensor;
	
	public PositionNNInput(Sensor sensor) {
		super(sensor);
		this.positionSensor = (PositionSensor) sensor;
	}
	
//	@Override
	public int getNumberOfInputValues() {	
		return 2;
	}

//	@Override
	public double getValue(int index) {
		if (index == 0)
			return positionSensor.getSensorReading(index) / positionSensor.getEnvironmentWidth() + 0.5;
		else
			return positionSensor.getSensorReading(index) / positionSensor.getEnvironmentHeight() + 0.5;			
	
	}

}
