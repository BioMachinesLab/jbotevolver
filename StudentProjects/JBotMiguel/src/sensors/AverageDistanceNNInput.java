package sensors;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class AverageDistanceNNInput extends NNInput {
	
	private AverageDistanceSensor rds;

	public AverageDistanceNNInput(Sensor s) {
		super(s);
		rds = (AverageDistanceSensor)s;
	}
	
	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return rds.getSensorReading(index);
	}
}