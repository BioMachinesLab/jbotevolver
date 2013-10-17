package sensors;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class RobotDistanceNNInput extends NNInput {
	
	private RobotDistanceSensor rds;

	public RobotDistanceNNInput(Sensor s) {
		super(s);
		rds = (RobotDistanceSensor)s;
	}

	@Override
	public int getNumberOfInputValues() {
		return rds.getNumberOfSensors();
	}

	@Override
	public double getValue(int index) {
		return rds.getSensorReading(index);
	}
}