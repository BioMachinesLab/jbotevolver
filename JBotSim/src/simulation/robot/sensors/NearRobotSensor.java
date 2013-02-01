package simulation.robot.sensors;

import java.util.ArrayList;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class NearRobotSensor extends Sensor {

	private double range; 
	
	public NearRobotSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot, args);
		range = (args.getArgumentIsDefined("range")) ? args.getArgumentAsDouble("range") : DEFAULT_RANGE;
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		ArrayList<Robot> robots = this.simulator.getEnvironment().getRobots();
		
		double value = 0;
		
		for(Robot r : robots) {
			if(r.getPosition().distanceTo(this.robot.getPosition()) < range && this.robot.getId() != r.getId()) {
				value = 1;
				break;
			}
		}
		
		return value;
	}
}