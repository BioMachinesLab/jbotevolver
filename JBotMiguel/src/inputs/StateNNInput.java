package inputs;

import sensors.StateSensor;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class StateNNInput extends NNInput {
	
	private StateSensor ss;
	
	public StateNNInput(Sensor s) {
		super(s);
		this.ss = (StateSensor)s;
	}

	@Override
	public int getNumberOfInputValues() {
		return ss.getNumberOfStates();
	}

	@Override
	public double getValue(int index) {
		return ss.getSensorReading(index);
	}
}
