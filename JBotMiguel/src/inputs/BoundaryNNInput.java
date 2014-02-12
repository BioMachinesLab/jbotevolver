package inputs;

import sensors.BoundarySensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class BoundaryNNInput extends NNInput{
	
	private BoundarySensor s;
	
	public BoundaryNNInput(Sensor s) {
		super(s);
		this.s = (BoundarySensor)s;
	}

	@Override
	public int getNumberOfInputValues() {
		return s.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return s.getSensorReading(index);
	}

}
