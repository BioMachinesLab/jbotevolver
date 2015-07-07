package sensors;

import java.util.ArrayList;

import environment.OpenEnvironment;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import simulation.util.ArgumentsAnnotation;

public class OrientationSensor extends Sensor {

	@ArgumentsAnnotation(name = "range", defaultValue = ""+DEFAULT_RANGE)
	private double range; 
	private Environment env;
	
	@ArgumentsAnnotation(name = "turnoff", help="Only works once.", values={"0","1"})
	private boolean turnOff = false;
	private boolean seenRobot = false;
	private double previousValue = 0;
	
	private static final String ID_DESCRIPTION = "type0";
	
	public OrientationSensor(Simulator simulator,int id, Robot robot, Arguments args) {
		super(simulator,id,robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range",DEFAULT_RANGE);
		//default it as true to enable backwards compatibility
		turnOff = args.getArgumentAsIntOrSetDefault("turnoff", 0) == 1;
		this.env = simulator.getEnvironment();
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		
		double x = 0.0;
		double y = 0.0;

		for(Robot r: env.getRobots()) {
			if(!r.getDescription().equals("prey") && r.getPosition().distanceTo(robot.getPosition()) <= range){
				x += Math.cos(r.getOrientation());
				y += Math.sin(r.getOrientation());
			}
		}

		Vector2d avg = new Vector2d(x, y);
			
		double angle = (avg.getAngle() - robot.getOrientation());
			
		if(angle > Math.PI)
			angle -= 2*Math.PI;
		else if(angle < -Math.PI)
			angle += 2*Math.PI;
		
		return ((0.5/Math.PI*angle) + 0.5);
		
		
	}
	
	public double getRange() {
		return range;
	}
}
