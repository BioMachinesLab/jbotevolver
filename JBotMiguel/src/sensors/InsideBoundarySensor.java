package sensors;

import java.util.ArrayList;
import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;

public class InsideBoundarySensor extends Sensor {
	
	private LinkedList<Line> lines = new LinkedList<Line>();
	private boolean isSetup = false;
	
	public InsideBoundarySensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id,robot,args);
	}
	
	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {
		if(!isSetup) {
			for(PhysicalObject p :teleported) {
				if(p.getType() == PhysicalObjectType.LINE)
					lines.add((Line)p);
			}
			isSetup = true;
		}
	}

	@Override
	public double getSensorReading(int sensorNumber) {
		return insideBoundary() ? 1 : 0;
	}
	
	public boolean insideBoundary() {
		//http://en.wikipedia.org/wiki/Point_in_polygon
		int count = 0;
		for(Line l : lines) {
			if(l.intersectsWithLineSegment(robot.getPosition(), new Vector2d(0,-Integer.MAX_VALUE)) != null)
				count++;
		}
		return count % 2 != 0;
	}

}
