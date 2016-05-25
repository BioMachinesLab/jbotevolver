package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.ConeTypeSensor;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

/**
 * This is a wrapper for any sensor which basically introduces a delay
 * in each sensory reading. The purpose is to minimize the reality gap
 * when we use, for instance, computer vision techniques to acquire the
 * location of a real robot and then use that to compute the relative
 * positions of virtual objects. 
 * 
 * @author miguelduarte42
 */
public class LaggedSensor extends Sensor{
	
	private int lagBufferSize = 0;
	private double[][] lagBuffer;
	private int lagBufferIndex = 0;
	private int sensorCount = 0;
	private int numberOfSensors = 1;
	
	private Sensor realSensor;
	
	public LaggedSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		
		realSensor = Sensor.getSensor(robot, simulator, name, new Arguments(args.getArgumentAsString("sensor")));
		
		lagBufferSize = args.getArgumentAsIntOrSetDefault("lagbuffer", lagBufferSize);
		
		if(realSensor instanceof ConeTypeSensor)
			numberOfSensors = ((ConeTypeSensor)realSensor).getNumberOfSensors();
		
		if(lagBufferSize > 0)
			lagBuffer = new double[numberOfSensors][lagBufferSize];
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		double currentValue = realSensor.getSensorReading(sensorNumber);
		
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
