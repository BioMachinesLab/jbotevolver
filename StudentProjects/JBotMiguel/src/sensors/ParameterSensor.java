package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class ParameterSensor extends Sensor {
	
	private double maxValue = 1;
	private double currentValue = 0;
	
	public ParameterSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		maxValue = args.getArgumentAsDoubleOrSetDefault("maxvalue",maxValue);
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return currentValue/maxValue;
	}
	
	public void setCurrentValue(double c) {
		this.currentValue = c;
	}
	
	public double getCurrentValue() {
		return currentValue;
	}
}