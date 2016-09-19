package sensors;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class NearRobotOrientationSensor extends Sensor{
	@ArgumentsAnnotation(name = "range", defaultValue = ""+DEFAULT_RANGE)
	private double range; 
	private Environment env;
	
	@ArgumentsAnnotation(name = "turnoff", help="Only works once.", values={"0","1"})
	private boolean turnOff = false;
	private boolean seenRobot = false;
	private double previousValue = 0;
	
	private static final String ID_DESCRIPTION = "type1";
	
	public NearRobotOrientationSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range",DEFAULT_RANGE);
		//default it as true to enable backwards compatibility
		turnOff = args.getArgumentAsIntOrSetDefault("turnoff", 0) == 1;
		this.env = simulator.getEnvironment();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		
		Robot near = null;
		double minDistance = Double.MAX_VALUE;
		for(Robot r: env.getRobots()) {
			double distance = robot.getPosition().distanceTo(r.getPosition());
			if(r.getDescription().equals(ID_DESCRIPTION) && distance <= range){
				if(minDistance>distance){
					minDistance = distance;
					near = r;
				}
			}
		}
		if(near==null){
			return 0;
		}
		return near.getPosition().getAngle()/Math.PI;
		
		
	}
	
	public double getRange() {
		return range;
	}

}
