package simulation.robot.sensors;

import java.util.Arrays;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowPheromoneChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class PheromoneSensor extends ConeTypeSensor {

	public PheromoneSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		setAllowedObjectsChecker(new AllowPheromoneChecker());
		this.range = 1;
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
		
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);

		if((sensorInfo.getDistance() < getRange())) {
			
			return (getRange() - sensorInfo.getDistance()) / getRange();

		}
 		return 0;
	}
	
	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j=0; j<numberOfSensors; j++) {
			readings[j] = Math.max(calculateContributionToSensor(j, source), readings[j]);
		}
	}
	
	public String toString() {
		for(int i=0;i<numberOfSensors;i++)
			getSensorReading(i);
		return "PheromoneSensor [readings=" + Arrays.toString(readings) + "]";
	}
}