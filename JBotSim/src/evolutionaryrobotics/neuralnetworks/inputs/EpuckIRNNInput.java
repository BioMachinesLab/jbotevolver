package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.EpuckIRSensor;
import simulation.robot.sensors.Sensor;

public class EpuckIRNNInput extends NNInput{
	
	EpuckIRSensor epuckSensor;
	
	public EpuckIRNNInput(Sensor epuckSensor) {
		this.epuckSensor = (EpuckIRSensor) epuckSensor;
	}

//	@Override
	public int getNumberOfInputValues() {
		return epuckSensor.getNumberOfSensors();
	}

//	@Override
	public double getValue(int index) {
		return epuckSensor.getSensorReading(index);
	}

}
