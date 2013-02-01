package simulation.robot.sensors;

import java.util.Arrays;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowLightChecker;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class SimpleLightTypeSensor extends ConeTypeSensor {
	
	private double halfOpeningAngle;

	public SimpleLightTypeSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		this.halfOpeningAngle = openingAngle / 2.0;
		setAllowedObjectsChecker(new AllowLightChecker());
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
	
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
	
		if((sensorInfo.getDistance() < getRange()) && 
		   (sensorInfo.getAngle() < (halfOpeningAngle)) && 
		   (sensorInfo.getAngle() > (-halfOpeningAngle))) {

			return (getRange() - sensorInfo.getDistance()) / getRange();
//			if (val > 1.0)
//				val = 1.0;
//			else if (val < 0.0)
//				val = 0.0;
//				
//			return val;
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
