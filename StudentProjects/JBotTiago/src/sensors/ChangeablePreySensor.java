package sensors;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.robot.Robot;
import simulation.robot.sensors.PreySensor;
import simulation.util.Arguments;

public class ChangeablePreySensor extends PreySensor {
	
	private double[] ranges;
	private double[] openingAngles;

	public ChangeablePreySensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		ranges = new double[numberOfSensors];
		openingAngles = new double[numberOfSensors];
		
		for (int i = 0; i < ranges.length; i++) {
			ranges[i] = range;
			openingAngles[i] = openingAngle;
		}
	}

	@Override
	protected double calculateContributionToSensor(int sensorNumber, PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
		
		if((sensorInfo.getDistance() < ranges[sensorNumber]) && 
		   (sensorInfo.getAngle() < (openingAngles[sensorNumber] / 2.0)) && 
		   (sensorInfo.getAngle() > (-openingAngles[sensorNumber] / 2.0))) {

			return (ranges[sensorNumber] - sensorInfo.getDistance()) / ranges[sensorNumber];
		}
		
 		return 0;
	}
	
	public double getRangeAtIndex(int index) {
		return ranges[index];
	}
	
	public double getOpeningAngleAtIndex(int index) {
		return openingAngles[index];
	}
	
	public void setRangeAtIndex(int index ,double range) {
		ranges[index] = range;
	}
	
	public void setOpeningAngleAtIndex(int index, double openingAngle) {
		openingAngles[index] = openingAngle;
	}
	
}
