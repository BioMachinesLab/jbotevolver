package sensors;

import simulation.Simulator;
import simulation.physicalobjects.Prey;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class RoomPreySensor extends Sensor {
	
	private Simulator simulator;

	public RoomPreySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		
		double robotX = robot.getPosition().getX();
		
		double val = 0;
		
		if(Math.abs(robotX) > 0.2) {//corridor width
			for(Prey p : simulator.getEnvironment().getPrey()) {
				double preyX = p.getPosition().getX();
				if(preyX > 0 && robotX > 0)
					val = 1;
				else if(preyX < 0 && robotX < 0)
					val = 1;
			}
		}
		return val;
	}
}