package evolutionaryrobotics.neuralnetworks.inputs;

import simulation.robot.sensors.NearRobotSensor;
import simulation.robot.sensors.Sensor;

public class NearRobotNNInput extends NNInput {
	
	private NearRobotSensor nrs;
	
	public NearRobotNNInput(Sensor nrs){
		super(nrs);
		this.nrs = (NearRobotSensor) nrs;
	}
	
	public int getNumberOfInputValues() {
		return 1;
	}

	public double getValue(int index) {
		return nrs.getSensorReading(index);
	}
}