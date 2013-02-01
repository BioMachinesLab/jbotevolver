package simulation.robot.sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GroundRGBColorSensor extends Sensor {

	int[] rgb = {255,255,255};
	int numberOfSensors = 3;
	
	public GroundRGBColorSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		rgb = simulator.getEnvironment().getGroundColor(robot.getId());
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
