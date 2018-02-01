package sensors;

import robots.JumpingRobot;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;


/**
 * Proprioceptive sensor that indicates if the robot is currently jumping or not.
 * @author Rita Ramos
 */

public class JumpingSensor extends Sensor {
	
	JumpingRobot jumpingRobot;
	
	public JumpingSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		jumpingRobot = (JumpingRobot)robot;
	}
	
	public boolean isJumping() {
		return jumpingRobot.isJumping();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return isJumping() ? 1 : 0;
	}

}
