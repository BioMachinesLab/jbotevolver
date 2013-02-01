package simulation.robot.sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class CompassSensor extends Sensor {

	public CompassSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}

	@Override
	public double getSensorReading(int sensorNumber) {

		double modulus = (robot.getOrientation() % (Math.PI * 2));

		// In Java, -3 % 2*PI is a negative number! Workaround...
		while (modulus < 0)
			modulus = modulus + (2 * Math.PI);

		return modulus / (2 * Math.PI);
	}

	@Override
	public String toString() {
		return "CompassSensor [" + getSensorReading(0) + "]";
	}

}
