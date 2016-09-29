package sensors;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;

public class RobotOrientationDistanceSensor extends LightTypeSensor {

	private int robotId = 0;

	public RobotOrientationDistanceSensor(Simulator simulator, int id,
			Robot robot, Arguments args) {
		super(simulator, id, robot, args);

		if (numberOfSensors == 2) {
			angles[0] = Math.PI / 4.0;
			angles[1] = (2.0 * Math.PI) - Math.PI / 4.0;
		}
		
		this.readings = new double[numberOfSensors+1];
		
		if(robot.getId() == 0)
			setTrackingRobotId(1);
		else
			setTrackingRobotId(0);
		setAllowedObjectsChecker( new AllowAllRobotsChecker(robot.getId()));
	}

	@Override
	public int getNumberOfSensors() {
		return super.getNumberOfSensors() + 1;
	}

	public void setTrackingRobotId(int id) {
		robotId = id;
	}
	
	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {

		double sensorVal = 0;
		
		if (source.getObject().getId() == robotId) {
			
			double distance = robot.getPosition().distanceTo(source.getObject().getPosition());

			if (sensorNumber == numberOfSensors) { // distance

				if (distance < getRange())
					sensorVal = (getRange() - distance) / getRange();

			} else { // orientation
				
				GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber,
						source);

				if (sensorInfo.getDistance() < getRange())
					sensorVal = (openingAngle - Math.abs(sensorInfo.getAngle())) / openingAngle;
			}
		}
		return sensorVal;
	}
}