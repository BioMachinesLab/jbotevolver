package sensors;

import java.util.ArrayList;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class NearTypeBRobotSensor extends Sensor{

	@ArgumentsAnnotation(name = "range", defaultValue = ""+DEFAULT_RANGE)
	private double range; 
	private Environment env;
	
	@ArgumentsAnnotation(name = "turnoff", help="Only works once.", values={"0","1"})
	private boolean turnOff = false;
	private boolean seenRobot = false;
	private double previousValue = 0;
	
	private static final String ID_DESCRIPTION = "type1";
	
	public NearTypeBRobotSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range",DEFAULT_RANGE);
		//default it as true to enable backwards compatibility
		turnOff = args.getArgumentAsIntOrSetDefault("turnoff", 0) == 1;
		this.env = simulator.getEnvironment();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		ArrayList<Robot> robots = env.getRobots();
		
		double value = 0;
		
		if(turnOff && previousValue == 0 && seenRobot)
			return value;
		
		for(Robot r : robots) { 
						
			if(r.getPosition().distanceTo(this.robot.getPosition()) < range && this.robot.getId() != r.getId() &&
					r.getDescription().equals(ID_DESCRIPTION)) {
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

	public double getRange() {
		return range;
	}
}
