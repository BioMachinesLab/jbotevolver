package simulation.robot.sensors;

import java.util.Arrays;

import mathutils.Vector2d;

import simulation.Simulator;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowNestChecker;
import simulation.physicalobjects.checkers.AllowOrderedPreyChecker;
import simulation.physicalobjects.checkers.AllowWallButtonChecker;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.SimRandom;

/**
 * @author miguelduarte
 */
public class WallButtonSensor extends LightTypeSensor {
	
	private int lagBufferSize = 0;
	private double[][] lagBuffer;
	private int lagBufferIndex = 0;
	private int sensorCount = 0;
	private int randomizeLagBuffer = 0;

	public WallButtonSensor(Simulator simulator, int id, Robot robot, Arguments args) {

		super(simulator, id, robot, args);
		
		if(numberOfSensors == 2) {
			angles[0] = Math.toRadians(30);
			angles[1] = Math.toRadians(330);
		}
		
		lagBufferSize = args.getArgumentAsIntOrSetDefault("lagbuffer", lagBufferSize);
		randomizeLagBuffer = args.getArgumentAsIntOrSetDefault("randomizelagbuffer", randomizeLagBuffer);
		
		if(lagBufferSize > 0) {
			
			int rand = (int)(simulator.getRandom().nextDouble()*randomizeLagBuffer);
			
			if(simulator.getRandom().nextBoolean())
				rand*=-1;
			
			lagBufferSize = lagBufferSize+rand;
			
			lagBuffer = new double[numberOfSensors][lagBufferSize];
		}
		setAllowedObjectsChecker(new AllowWallButtonChecker(robot.getId()));
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

}
