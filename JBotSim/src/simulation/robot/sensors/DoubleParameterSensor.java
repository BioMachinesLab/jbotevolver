package simulation.robot.sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;
/**
 * A sensor that maps from a user specified parameter to a sensor value. The "key" is the name of the parameter.
 * 
 * @author alc
 */
public class DoubleParameterSensor extends Sensor {
	protected String key;
	protected double minValue;
	protected double maxValue;
	
	
	public DoubleParameterSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot,args);
		key = args.getArgumentAsString("key");
		minValue = (args.getArgumentIsDefined("min")) ? args
				.getArgumentAsDouble("min") : 0.0;
		maxValue = (args.getArgumentIsDefined("max")) ? args
				.getArgumentAsDouble("max") : 1.0;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		
		return robot.getParameterAsDouble(key).doubleValue();
	}
	
	public double getMinimumValue() {
		return minValue;
	}

	public double getMaximumValue() {
		return maxValue;
	}
	
	@Override
	public String toString() {
		return "DoubleParameterSensor ["+getSensorReading(0)+"]";
	}

	
}
