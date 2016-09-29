package environments;

import java.util.LinkedList;

import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.physicalobjects.Line;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectType;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class MultiWaypointEnvironment extends Environment {

	protected double distance = 0.1;
	protected String waypointCoordinates = "";
	protected LinkedList<LightPole> waypoints = new LinkedList<LightPole>();
	protected boolean singleWaypoint = true;
	protected boolean rand = false;
	
	public MultiWaypointEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		distance = args.getArgumentAsDoubleOrSetDefault("distance", distance);
		waypointCoordinates = args.getArgumentAsString("waypoints");
		singleWaypoint = args.getArgumentAsIntOrSetDefault("singlewaypoint", 1) == 1;
		rand = args.getArgumentAsIntOrSetDefault("random", 0) == 1;
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		if(waypointCoordinates.contains("!")) {
			String[] splitted = waypointCoordinates.split("!");
			waypointCoordinates = splitted[simulator.getRandom().nextInt(splitted.length)];
		}
		
		
		String[] splitted = waypointCoordinates.split(";");
		int i = 0;
		for(String s : splitted) {
			String[] sp = s.split(",");
			double x = Double.parseDouble(sp[0]);
			double y = Double.parseDouble(sp[1]);
			
			if(rand) {
				x= x/2+simulator.getRandom().nextDouble()*x/2;
				y= y/2+simulator.getRandom().nextDouble()*y/2;
			}
			
			LightPole p = new LightPole(simulator, "boundary"+i, x , y, 0.1);
			waypoints.add(p);
			i++;
		}
		
		if(singleWaypoint) {
			if(!waypoints.isEmpty())
				addObject(waypoints.poll());
		}else{
			for(LightPole p : waypoints) {
				addObject(p);
			}
			
			LightPole prev = null;
			
			for(int j = 0 ; j < waypoints.size() ; j++) {
				LightPole current = waypoints.get(j);
				if(j == 0)
					prev = current;
				else {
					Line l = new Line(simulator,"line_"+prev.getName()+"_"+current.getName(),prev.getPosition().getX(),prev.getPosition().getY(),current.getPosition().getX(),current.getPosition().getY());
					addObject(l);
					prev = current;
				}
			}
		}
		
		for(Robot r : robots)
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
	}
	
	@Override
	public void update(double time) {
		
		Robot r = robots.get(0);
		
		if(singleWaypoint) {
			LightPole lp = null;
			int i = 0;
			
			for(PhysicalObject p : getAllObjects()) {
				if(p.getType() == PhysicalObjectType.LIGHTPOLE) {
					lp = (LightPole)p;
					break;
				}
				i++;
			}
			
			if(lp != null) {
				double dist = r.getPosition().distanceTo(lp.getPosition());
				
				if(dist < distance) {
					getAllObjects().remove(i);
					if(!waypoints.isEmpty()) {
						addObject(waypoints.poll());
					}
				}
			}
		}
	}
}
