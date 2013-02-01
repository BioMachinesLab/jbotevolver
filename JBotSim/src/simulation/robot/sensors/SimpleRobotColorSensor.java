package simulation.robot.sensors;

import java.awt.Color;
import java.util.Arrays;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;

public class SimpleRobotColorSensor extends ConeTypeSensor {
	private double openingAngle;
	private double r;
	private Color mode;

	protected SimRandom random;
	
	// public static float NOISESTDEV = 0.05f;

	public SimpleRobotColorSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.random = simulator.getRandom();
		String modeStr = (args.getArgumentIsDefined("mode")) ? args.getArgumentAsString("mode") : "red";

		this.openingAngle = openingAngle / 2;
		r = (Math.PI / 2) / openingAngle;

		if (modeStr.equalsIgnoreCase("red")) {
			mode = Color.red;
		} else if (modeStr.equalsIgnoreCase("green")) {
			mode = Color.green;
		} else if (modeStr.equalsIgnoreCase("blue")) {
			mode = Color.blue;
		} else if (modeStr.equalsIgnoreCase("lightgray")) {
			mode = Color.LIGHT_GRAY;
		} else if (modeStr.equalsIgnoreCase("darkgray")) {
			mode = Color.DARK_GRAY;
		} else if (modeStr.equalsIgnoreCase("yellow")) {
			mode = Color.yellow;
		}
		
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {

		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);

		if (sensorInfo.getDistance() < getRange()
				&& (sensorInfo.getAngle() < (openingAngle))
				&& (sensorInfo.getAngle() > (-openingAngle))) {

			return (getRange() - sensorInfo.getDistance()) / getRange();

		}
		return 0;
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		Robot nextRobot = (Robot) source.getObject();
		if ((mode == null && nextRobot.getBodyColor() != Color.BLACK)
				|| nextRobot.getBodyColor().getRGB() == mode.getRGB()) {
			for (int j = 0; j < numberOfSensors; j++) {
				readings[j] = Math
						.max(calculateContributionToSensor(j, source)
								* (1 + random.nextGaussian() * NOISESTDEV),
								readings[j]);
			}
		}
	}

	@Override
	public int getNumberOfSensors() {
		return numberOfSensors;
	}

	@Override
	public String toString() {
		for (int i = 0; i < numberOfSensors; i++)
			getSensorReading(i);
		return "RobotColorSensor [readings=" + Arrays.toString(readings) + "]";
	}

}
