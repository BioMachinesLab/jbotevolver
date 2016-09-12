package sensors;

import mathutils.Vector2d;
import robots.JumpingSumo;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class JumpingRobotsGlobalSensor extends Sensor {
	private Simulator simulator;
	private Robot robot;
	@ArgumentsAnnotation(name = "rangeLimit", help = "Range of the sensor.", defaultValue = "1.0")
	protected double rangeLimit = 1.0;

	public JumpingRobotsGlobalSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		this.robot = robot;
		rangeLimit = (args.getArgumentIsDefined("rangeLimit")) ? args
				.getArgumentAsDouble("rangeLimit") : 1.0;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		Vector2d robotPosition = robot.getPosition();
		double totalRobotsJumping = 0.0;
		double numberOfRobots_withinRange = 0.0;

		if (robot instanceof JumpingSumo) {

			if (((JumpingSumo) robot).isJumping())
				rangeLimit = rangeLimit + 1;

			for (Robot robotNeighbour : simulator.getRobots()) {
				if (robotPosition.distanceTo(robotNeighbour.getPosition()) < rangeLimit) {
					if (robot.getId() != robotNeighbour.getId()) {
						if (((JumpingSumo) robotNeighbour).statusOfJumping()) {
							totalRobotsJumping += 1;
						}
						numberOfRobots_withinRange += 1.0;
					}
				}
			}
		}

		if (numberOfRobots_withinRange > 0.0)
			return totalRobotsJumping / numberOfRobots_withinRange;
		else
			return 0.0;
	}

	@Override
	public String toString() {
		return "JumpingRobotsGlobalSensor [" + getSensorReading(0) + "]";
	}
}
