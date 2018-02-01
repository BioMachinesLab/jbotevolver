package sensors;

import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

/**
 * Sensor that indicates if the neighbours robot are currently jumping
 * (average of the neighbours robots jumping)
 * @author Rita Ramos
 */
public class RobotsJumpingAvarageSensor extends Sensor {
	private Simulator simulator;
	private JumpingRobot robot;
	protected boolean rangedIncreased = false;
	
	@ArgumentsAnnotation(name = "range", help = "Range of the sensor.", defaultValue = "1.0")
	protected double range = 1.0;

	@ArgumentsAnnotation(name = "increaseRange", help = "Increase range of the sensor while jumping.", defaultValue = "1.0")
	protected double increaseRange = 1.0;
	
	
	public RobotsJumpingAvarageSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		this.robot = (((JumpingRobot) robot));
		range = (args.getArgumentIsDefined("range")) ? args
				.getArgumentAsDouble("range") : 1.0;
		increaseRange = (args.getArgumentIsDefined("increaseRange")) ? args
				.getArgumentAsDouble("increaseRange") : 1.0;
	}
	

	/**
	 * Counts the number of neighbours robot currently jumping and then averages the result
	 */

	@Override
	public double getSensorReading(int sensorNumber) {
		Vector2d robotPosition = robot.getPosition();
		double totalRobotsJumping = 0.0;
		double numberOfRobots_withinRange = 0.0;

		if (robot.isJumping()) {
			range += increaseRange;
			rangedIncreased = true;
		}
		for (Robot robotNeighbour : simulator.getRobots()) {
			if (robotPosition.distanceTo(robotNeighbour.getPosition()) < range) {
				if (robot.getId() != robotNeighbour.getId()) {
					if (((JumpingRobot) robotNeighbour).statusOfJumping()) {
						totalRobotsJumping += 1;
					}
					numberOfRobots_withinRange += 1.0;
				}
			}
		}
		rangeBackToDefault();
		if (numberOfRobots_withinRange > 0.0)
			return totalRobotsJumping / numberOfRobots_withinRange;
		else
			return 0.0;
	}

	@Override
	public String toString() {
		return "JumpingRobotsGlobalSensor [" + getSensorReading(0) + "]";
	}

	private void rangeBackToDefault() {
		if (rangedIncreased == true) {
			rangedIncreased = false;
			range = range - increaseRange;
		}
	}

}
