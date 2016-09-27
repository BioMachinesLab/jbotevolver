package sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class RoomTimeSensor extends Sensor {
	
	private double maxTime = 500;
	private double currentTime = 0;
	private double prevX = 0;

	public RoomTimeSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		maxTime = args.getArgumentAsDoubleOrSetDefault("time", maxTime);
		prevX = robot.getPosition().getX();
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		if(currentTime < maxTime)
			currentTime++;
		
		double currentX = robot.getPosition().getX();
		
		if(Math.abs(currentX) > 0.3 && Math.abs(prevX) < 0.3) {
			currentTime = 0;
		}
		prevX = currentX;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return currentTime/maxTime;
	}
}