package sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.MathUtils;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class RobotPositionSensor extends Sensor {
	
	private LinkedList<PhysicalObject> positions = new LinkedList<PhysicalObject>();
	private double range;
	private Simulator sim;
	private boolean robots = true;
	private boolean lines = true;
	
	public RobotPositionSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
		range = args.getArgumentAsDoubleOrSetDefault("range", 1);
		sim = simulator;
		robots = args.getArgumentAsIntOrSetDefault("robots", 1) == 1;
		lines = args.getArgumentAsIntOrSetDefault("lines", 1) == 1;
	}
	
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		
		positions.clear();
		
		for(PhysicalObject p : sim.getEnvironment().getAllObjects()) {
			
			if(robots && p.getType()==PhysicalObjectType.ROBOT && p.getId() != robot.getId() && p.getPosition().distanceTo(robot.getPosition()) < range) {
				positions.add(p);
			}
			
			if(lines && p.getType()==PhysicalObjectType.LINE) {
				
				Line l = (Line)p;
				
				Vector2d closestPoint = MathUtils.perpendicularIntersectionPoint(l.getPointA(),l.getPointB(), robot.getPosition());
				
				if(closestPoint.distanceTo(robot.getPosition()) < range) {
					positions.add(p);
				}
			}
		}
	}
	
	public LinkedList<PhysicalObject> getPositions() {
		return positions;
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return 0;
	}

	public double getRange() {
		return range;
	}
}