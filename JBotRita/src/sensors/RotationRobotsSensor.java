
package sensors;

import java.util.Arrays;
import java.util.HashMap;

import robots.JumpingSumo;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.MovableObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowAllRobotsChecker;
import simulation.physicalobjects.checkers.AllowLightChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.util.Arguments;

public class RotationRobotsSensor extends ConeTypeSensor  {
	private Robot robot;

	public RotationRobotsSensor(Simulator simulator,int id, Robot robot, Arguments args) {
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


//			System.out.println("mysensor"+ sensorNumber);
//			System.out.println("myRobot"+ robot.getId());

			Robot robotNeighbour = (Robot) source.getObject();
//			System.out.println("otherRobot"+ robotNeighbourd.getId());

//			System.out.println(Math.toDegrees(robotNeighbourd.getOrientation())+ " "+ Math.toDegrees(robot.getOrientation()));

			double differenceOfOrientation=calculateDifferenceBetweenAngles(Math.toDegrees(robotNeighbour.getOrientation()),Math.toDegrees(robot.getOrientation()));
//			System.out.println(differenceOfOrientation);
//			System.out.println(0.5*(differenceOfOrientation)/180+0.5);

			return 0.5*(differenceOfOrientation)/180+0.5;
			
		}
		
		//normalize http://stackoverflow.com/questions/1471370/normalizing-from-0-5-1-to-0-1
		//System.out.println(" sensores lygghtype"+ 0);
		//sensorsJumping.put(sensorNumber, 0.0);
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
		return "SimPointLightSensor [readings=" + Arrays.toString(readings) + "]";
	}
	
	


}