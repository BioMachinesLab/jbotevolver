package sensors;


import mathutils.Vector2d;
import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class RotationJumpingRobotsGlobalSensor extends Sensor {  //the same as the RotationRobotsGlobalSensor, but just takes into account the robots that are charging
	private Simulator simulator;
	private Robot robot;
	protected boolean rangedIncreased = false;

	
	@ArgumentsAnnotation(name = "range", help = "Range of the sensor.", defaultValue = "1.0")
	protected double range = 1.0;

	@ArgumentsAnnotation(name = "increaseRange", help = "Increase range of the sensor while jumping.", defaultValue = "1.0")
	protected double increaseRange = 1.0;

	public RotationJumpingRobotsGlobalSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		this.robot = robot;
		range = (args.getArgumentIsDefined("range")) ? args
				.getArgumentAsDouble("range") : 1.0;
		increaseRange = (args.getArgumentIsDefined("increaseRange")) ? args
				.getArgumentAsDouble("increaseRange") : 1.0;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		Vector2d robotPosition = robot.getPosition();
		double sumRelativeOrientation = 0.0;
		double numberOfRobots_withinRange = 0.0;
		if(robot instanceof JumpingRobot){
			if (((JumpingRobot) robot).isJumping()){
				range+=increaseRange;
				rangedIncreased = true;
			}
		}
		for (Robot robotNeighbour :  simulator.getRobots()) {

			if (robotPosition.distanceTo(robotNeighbour.getPosition()) < range) {

				if (robot.getId() != robotNeighbour.getId() && ((JumpingRobot)robotNeighbour).charging()) {
					double differenceOfOrientation = calculateDifferenceBetweenAngles(
							Math.toDegrees(robotNeighbour.getOrientation()),
							Math.toDegrees(robot.getOrientation()));

					sumRelativeOrientation += 0.5 * (differenceOfOrientation) / 180 + 0.5;
					numberOfRobots_withinRange += 1.0;

				}
			}
		}
		rangeBackToDefault();
		if (numberOfRobots_withinRange > 0.0)
			return sumRelativeOrientation / numberOfRobots_withinRange;
		else
			return 0.0;
	}

	private double calculateDifferenceBetweenAngles(double secondAngle,
			double firstAngle) {
		double difference = secondAngle - firstAngle;
		while (difference < -180)
			difference += 360;
		while (difference > 180)
			difference -= 360;
		return difference;
	}

	
	
	
	@Override
	public String toString() {
		return "RotationRobotsGlobalSensor [" + getSensorReading(0) + "]";
	}
	
	private void rangeBackToDefault() {
		if (rangedIncreased == true) {
			rangedIncreased = false;
			range = range - increaseRange;
		}
	}
}
