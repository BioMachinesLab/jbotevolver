package inputs;

import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class WaypointNNInput extends NNInput{
	
	private Sensor s;
	
	public WaypointNNInput(Sensor s) {
		super(s);
		this.s = s;
	}

	@Override
	public int getNumberOfInputValues() {
		return 2;
	}

	@Override
	public double getValue(int index) {
		return s.getSensorReading(index);
	}

}
