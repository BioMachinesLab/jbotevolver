package sensors;

import java.util.Arrays;
import java.util.HashMap;

import robots.JumpingSumo;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowLightChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class JumpingRobotPositionSensor extends ConeTypeSensor {

	public JumpingRobotPositionSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowLightChecker());
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		if (((JumpingSumo) robot).isJumping())
			range = range + 1;
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
		if ((sensorInfo.getDistance() < getCutOff())
				&& (sensorInfo.getAngle() < (openingAngle / 2.0))
				&& (sensorInfo.getAngle() > (-openingAngle / 2.0))) {

			return (getRange() - sensorInfo.getDistance()) / getRange();

		}
		return 0;
	}

	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for (int j = 0; j < numberOfSensors; j++) {
			readings[j] = Math.max(calculateContributionToSensor(j, source),
					readings[j]);
		}
	}

	@Override
	public String toString() {
		for (int i = 0; i < numberOfSensors; i++)
			getSensorReading(i);
		return "SimPointLightSensor [readings=" + Arrays.toString(readings)
				+ "]";
	}


}
