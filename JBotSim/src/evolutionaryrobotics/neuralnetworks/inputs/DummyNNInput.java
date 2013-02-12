package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.Sensor;

public class DummyNNInput extends NNInput{
	
	private double value;
	
	public DummyNNInput(Sensor s) {
		super(s);
	}

	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return value;
	}
	
	public void setInput(double inputValue) {
		this.value = inputValue;
	}

}
