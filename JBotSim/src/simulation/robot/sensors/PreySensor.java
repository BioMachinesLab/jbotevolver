package simulation.robot.sensors;

import simulation.Simulator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

/**
 * 
 * This class is used just so we can differentiate the PreySensor from the
 * NestSensor, since they both extend LightTypeSensor. It's useful for
 * evaluation functions that need to access individual sensor readings.
 * 
 * @author miguelduarte
 */
public class PreySensor extends LightTypeSensor {
	
	private int lagBufferSize = 0;
	private double[][] lagBuffer;
	private int lagBufferIndex = 0;
	private int sensorCount = 0;

	public PreySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		lagBufferSize = args.getArgumentAsIntOrSetDefault("lagbuffer", lagBufferSize);
		
		if(lagBufferSize > 0)
			lagBuffer = new double[numberOfSensors][lagBufferSize];
		
		setAllowedObjectsChecker( new AllowOrderedPreyChecker(robot.getId()));
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		
		double currentValue = super.getSensorReading(sensorNumber);
		
		if(lagBuffer != null) {
			
			double lagValue = lagBuffer[sensorNumber][lagBufferIndex % lagBufferSize];
			
			lagBuffer[sensorNumber][lagBufferIndex] = currentValue;
			
			if(++sensorCount == numberOfSensors) {
				lagBufferIndex = (lagBufferIndex+1) % lagBufferSize;
				sensorCount = 0;
			}
			currentValue = lagValue;
		}
		return currentValue;
	}
	
	@Override
	protected double calculateContributionToSensor(int sensorNumber,
			PhysicalObjectDistance source) {
		GeometricInfo sensorInfo = getSensorGeometricInfo(sensorNumber, source);
//		System.out.println(sensorInfo.getDistance());
		return super.calculateContributionToSensor(sensorNumber, source);
	}
}