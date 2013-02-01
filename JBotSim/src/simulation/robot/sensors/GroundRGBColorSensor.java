package simulation.robot.sensors;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GroundRGBColorSensor extends Sensor {

	int[] rgb = {255,255,255};
	int numberOfSensors = 3;
	protected Environment env;
	
	public GroundRGBColorSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.env = simulator.getEnvironment();
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		rgb = env.getGroundColor(robot.getId());
		return rgb[sensorNumber];
	}

	@Override
	public String toString() {
		return "GroundRGBColorSensor [" + getSensorReading(0) + ", "+getSensorReading(1)+", "+getSensorReading(2)+" ]";
	}
	
	public int getNumberOfSensors()	{
		return numberOfSensors;
	}

}
