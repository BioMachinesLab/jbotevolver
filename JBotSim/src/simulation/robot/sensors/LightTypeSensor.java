package simulation.robot.sensors;

import java.util.Arrays;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowLightChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class LightTypeSensor extends ConeTypeSensor {
	
	public LightTypeSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		setAllowedObjectsChecker(new AllowLightChecker());
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
	
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
	
		if((sensorInfo.getDistance() < getCutOff()) && 
		   (sensorInfo.getAngle() < (openingAngle / 2.0)) && 
		   (sensorInfo.getAngle() > (-openingAngle / 2.0))) {

			return (getRange() - sensorInfo.getDistance()) / getRange();
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
