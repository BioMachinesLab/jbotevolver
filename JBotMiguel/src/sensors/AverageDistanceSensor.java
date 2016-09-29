package sensors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class AverageDistanceSensor extends Sensor {
	
	private Simulator simulator;
	private double range = 1;

	public AverageDistanceSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		this.simulator = simulator;
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		
		double nRobots = simulator.getRobots().size() - 1;
		
		double avgDistance = 0;
		
		for(Robot r : simulator.getRobots()) {
			if(r.getId() != robot.getId()) {
				double distance = r.getPosition().distanceTo(robot.getPosition());
				if(distance < range)
					avgDistance+= (range - distance)/range / nRobots;
			}
		}
		
		return avgDistance;
	}
}