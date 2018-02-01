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

/**
 * Sensor that indicates if the neighbours robot are currently jumping
 * (see if the robot perceived in the cone sensor is jumping or not)
 * @author Rita Ramos
 */
public class RobotsJumpingConeSensor extends ConeTypeSensor  {
	
	public RobotsJumpingConeSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		setAllowedObjectsChecker(new AllowAllRobotsChecker(robot.getId()));
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
	
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
	
		if((sensorInfo.getDistance() < getCutOff()) && 
		   (sensorInfo.getAngle() < (openingAngle / 2.0)) && 
		   (sensorInfo.getAngle() > (-openingAngle / 2.0))) {
			
			Robot robot = (Robot) source.getObject();
			if( robot instanceof JumpingSumo){
				if (((JumpingSumo) robot).statusOfJumping()){
					return 1.0;
				}else{
					return 0.5;
				}
			}
		}
 		return 0;
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