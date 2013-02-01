package simulation.robot.sensors;

import simulation.Simulator;
import simulation.robot.MultiPreyForagerRobot;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MultyPreyCarriedSensor extends Sensor {

	public MultyPreyCarriedSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		if (!(robot instanceof MultiPreyForagerRobot)){
			throw new RuntimeException("MultyPreyCarriedSensor can only be used with a MultiPreyForagerRobot robot");
		}
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return (double)((MultiPreyForagerRobot) robot).getNumberOfPreysCarried()
				/ ((MultiPreyForagerRobot) robot).getPreysCarriedLimit();
	}

	@Override
	public String toString() {
		return "MultyPreyCarriedSensor: "+ ((MultiPreyForagerRobot) robot).getNumberOfPreysCarried();
	}
}