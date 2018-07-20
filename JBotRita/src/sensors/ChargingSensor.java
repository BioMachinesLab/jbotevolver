package sensors;

import robots.JumpingRobot;

/**
 * Proprioceptive sensor that indicates if the robot is currently charging to jump or not.
 * @author Rita Ramos
 */

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class ChargingSensor extends Sensor {
	
	JumpingRobot jumpingRobot;
	
	public ChargingSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		jumpingRobot = (JumpingRobot) robot;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return jumpingRobot.charging() ? 1 : 0;
	}

}
