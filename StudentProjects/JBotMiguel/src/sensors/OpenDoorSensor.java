package sensors;

import roommaze.TwoRoomsEnvironment;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class OpenDoorSensor extends Sensor {
	
	private TwoRoomsEnvironment env;
	
	public OpenDoorSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		if(simulator.getEnvironment() instanceof TwoRoomsEnvironment)
			env = (TwoRoomsEnvironment)simulator.getEnvironment();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		if(env != null)
			return env.doorsOpen ? 1 : 0;
		return 0;
	}
}