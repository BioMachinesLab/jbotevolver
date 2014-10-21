package inputs;

import sensors.GroundBandSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class GroundBandNNInput extends NNInput {
	
	private GroundBandSensor s;

	public GroundBandNNInput(Sensor s) {
		super(s);
		this.s = (GroundBandSensor)s;
	}

	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return s.getSensorReading(index);
	}

}
