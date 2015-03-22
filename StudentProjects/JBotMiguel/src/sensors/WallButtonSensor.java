package sensors;

import net.jafama.FastMath;
import simulation.Simulator;
import simulation.physicalobjects.checkers.AllowWallButtonChecker;
import simulation.robot.Robot;
import simulation.robot.sensors.LightTypeSensor;
import simulation.util.Arguments;

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
			angles[0] = FastMath.toRadians(30);
			angles[1] = FastMath.toRadians(330);
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
