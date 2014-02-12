package inputs;

import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class InsideBoundaryNNInput extends NNInput {
	
	private Sensor s;
	
	public InsideBoundaryNNInput(Sensor s) {
		super(s);
		this.s = s;
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
