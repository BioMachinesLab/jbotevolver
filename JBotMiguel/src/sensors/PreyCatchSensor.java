package sensors;

import java.util.ArrayList;

import environments.TwoRoomsMultiPreyEnvironment;

import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class PreyCatchSensor extends Sensor {
	
	private double maxTime = 500;
	private double currentTime = 0;
	private double prevPreys = 0;
	private TwoRoomsMultiPreyEnvironment env;

	public PreyCatchSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		maxTime = args.getArgumentAsDoubleOrSetDefault("time", maxTime);
		env = (TwoRoomsMultiPreyEnvironment)simulator.getEnvironment();
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		if(currentTime < maxTime)
			currentTime++;
		
		double currentPreys = env.getPreysCaught();
		
		if(currentPreys > prevPreys) {
			currentTime = 0;
		}
		prevPreys = currentPreys;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return currentTime/maxTime;
	}
}