package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.Sensor;
import simulation.robot.sensors.WallRaySensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class WallRayNNInput extends NNInput{
	
	WallRaySensor wallRaySensor;
	
	public WallRayNNInput(Sensor wallRaySensor) {
		super(wallRaySensor);
		this.wallRaySensor = (WallRaySensor) wallRaySensor;
	}

	@Override
	public int getNumberOfInputValues() {
		return wallRaySensor.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return wallRaySensor.getSensorReading(index);
	}
}