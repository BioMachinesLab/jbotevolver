package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class CollisionWallSensor extends Sensor {

	
	public CollisionWallSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	public boolean isColliding() {
		
		return robot.isInvolvedInCollisonWall();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return isColliding() ? 1 : 0;
	}

	
}