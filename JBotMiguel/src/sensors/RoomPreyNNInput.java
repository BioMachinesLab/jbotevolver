package sensors;
import simulation.robot.sensors.Sensor;
import evolutionaryrobotics.neuralnetworks.inputs.NNInput;

public class RoomPreyNNInput extends NNInput {
	
	private RoomPreySensor rps;

	public RoomPreyNNInput(Sensor s) {
		super(s);
		rps = (RoomPreySensor)s;
	}
	
	@Override
	public int getNumberOfInputValues() {
		return 1;
	}

	@Override
	public double getValue(int index) {
		return rps.getSensorReading(index);
	}
}