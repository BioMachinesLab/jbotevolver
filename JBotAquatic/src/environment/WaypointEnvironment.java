package environment;

import commoninterface.AquaticDroneCI;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CoordinateUtilities;
import objects.Waypoint;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.util.Arguments;

public class WaypointEnvironment extends Environment{
	
	private int nWaypoints = 1;

	public WaypointEnvironment(Simulator simulator, Arguments args) {
		super(simulator, args);
		nWaypoints = args.getArgumentAsIntOrSetDefault("numberwaypoints", nWaypoints);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		simulator.getRobots().get(0).setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
		
		for(int i = 0 ; i < nWaypoints ; i++) {
			double x = 2;
			double y = 0;
			Vector2d latLon = CoordinateUtilities.cartesianToGPS(x, y);
			Waypoint wp = new Waypoint(latLon.getX(), latLon.getY(), "wp"+i);
			((AquaticDroneCI)simulator.getRobots().get(0)).getEntities().add(wp);
			LightPole lp = new LightPole(simulator, "wp"+i, x, y, 0.5);
			addObject(lp);
		}
	}

	@Override
	public void update(double time) {
		
	}
}