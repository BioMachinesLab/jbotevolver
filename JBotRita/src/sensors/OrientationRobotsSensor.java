
package sensors;

import java.util.Arrays;
import java.util.HashMap;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;


/**
 * Alignment sensor to see the relative orientation regarding others robots
 * This sensor is similar to RobotSensor 
 * (see our relative orientation regarding the specific robot perceived in the cone sensor)
 * @author Rita Ramos
 */


public class OrientationRobotsSensor extends ConeTypeSensor  {
	private Robot robot;

	public OrientationRobotsSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
		this.robot=robot;
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
	
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
	
		if((sensorInfo.getDistance() < getCutOff()) && 
		   (sensorInfo.getAngle() < (openingAngle / 2.0)) && 
		   (sensorInfo.getAngle() > (-openingAngle / 2.0))) {

			Robot robotNeighbour = (Robot) source.getObject();

			double differenceOfOrientation=calculateDifferenceBetweenAngles(Math.toDegrees(robotNeighbour.getOrientation()),Math.toDegrees(robot.getOrientation()));

			return 0.5*(differenceOfOrientation)/180+0.5;
			
		}

 		return 0;
	}

	private double calculateDifferenceBetweenAngles( double secondAngle, double firstAngle)
	 {
	        double difference = secondAngle - firstAngle;
	        while (difference < -180) difference += 360;
	        while (difference > 180) difference -= 360;
	        return difference;
	 }
	
	@Override
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j=0; j<numberOfSensors; j++) {
			readings[j] = Math.max(calculateContributionToSensor(j, source), readings[j]);
		}
	}

	@Override
	public String toString() {
		for(int i=0;i<numberOfSensors;i++)
			getSensorReading(i);
		return "OrientationRobotsSensor [readings=" + Arrays.toString(readings) + "]";
	}
	
}