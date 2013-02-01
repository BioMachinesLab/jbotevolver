package simulation.robot.sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.BeeRobot;
import simulation.util.Arguments;
/**
 * Tells the robot how much energy it has (percentage).
 * 
 * @author miguelduarte
 */
public class EnergySensor extends Sensor{

	public EnergySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		
		BeeRobot bee = (BeeRobot)robot;
		if (bee.getMaxEnergy() != 0) {
			return bee.getEnergy() / bee.getMaxEnergy();
		}
		else
			return 0;
	}

	@Override
	public String toString() {
		return "EnergySensor ["+getSensorReading(0)+"]";
	}

	
}
