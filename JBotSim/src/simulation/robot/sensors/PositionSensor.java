package simulation.robot.sensors;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PositionSensor extends Sensor {

	protected Environment env;
	
	public PositionSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.env = simulator.getEnvironment();
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		Vector2d p = robot.getPosition();
		if (sensorNumber == 0) {
			return p.x;
		} else {
			return p.y;
		}
	}

	@Override
	public String toString() {
		return "PositionSensor [" + getSensorReading(0) + ", " + getSensorReading(1) + "]";
	}

	public double getEnvironmentWidth() {
		return env.getWidth();
	}

	public double getEnvironmentHeight() {
		return env.getHeight();
	}
}