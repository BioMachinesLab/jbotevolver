package sensors;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class RobotOrientationDistanceNNInput extends NNInput {
	
	private RobotOrientationDistanceSensor rds;

	public RobotOrientationDistanceNNInput(Sensor s) {
		super(s);
		rds = (RobotOrientationDistanceSensor)s;
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