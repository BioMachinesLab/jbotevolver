package sensors;

import java.util.ArrayList;

import environment.OpenEnvironment;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class ChainSensor extends Sensor{

	@ArgumentsAnnotation(name = "range", defaultValue = ""+DEFAULT_RANGE)
	private double range; 
	private Environment env;
	
	@ArgumentsAnnotation(name = "turnoff", help="Only works once.", values={"0","1"})
	private boolean turnOff = false;
	
	public ChainSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range",DEFAULT_RANGE);
		//default it as true to enable backwards compatibility
		turnOff = args.getArgumentAsIntOrSetDefault("turnoff", 0) == 1;
		this.env = simulator.getEnvironment();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		if(env instanceof OpenEnvironment){
			if(((OpenEnvironment) env).isConnected())
				return 1;
		}
		return 0;
	}

	public double getRange() {
		return range;
	}
}
