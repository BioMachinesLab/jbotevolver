package simulation.robot.sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class NearRobotSensor extends Sensor {

	@ArgumentsAnnotation(name = "range", defaultValue = ""+DEFAULT_RANGE)
	private double range; 
	private Environment env;
	
	@ArgumentsAnnotation(name = "turnoff", help="Only works once.", values={"0","1"})
	private boolean turnOff = true;
	private boolean seenRobot = false;
	private double previousValue = 0;
	
	public NearRobotSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range",DEFAULT_RANGE);
		//default it as true to enable backwards compatibility
		turnOff = args.getArgumentAsIntOrSetDefault("turnoff", 1) == 1;
		this.env = simulator.getEnvironment();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		ArrayList<Robot> robots = env.getRobots();
		
		double value = 0;
		
		if(turnOff && previousValue == 0 && seenRobot)
			return value;
		
		for(Robot r : robots) {
			if(r.getPosition().distanceTo(this.robot.getPosition()) < 
					range && this.robot.getId() != r.getId()) {
				value = 1;
				break;
			}
		}
		
		if(turnOff) {
			previousValue = value;
			if(!seenRobot && value > 0)
				seenRobot = true;
		}
		
		return value;
	}
}