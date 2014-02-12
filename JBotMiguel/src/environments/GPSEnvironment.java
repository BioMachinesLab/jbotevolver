package environments;

import java.util.LinkedList;

import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.LightPole;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GPSEnvironment extends Environment{
	
	protected LinkedList<LightPole> waypoints = new LinkedList<LightPole>();
	protected String waypointCoordinates = "";
	
	public GPSEnvironment(Simulator simulator, Arguments args) {
		super(simulator,args);
		waypointCoordinates = args.getArgumentAsString("waypoints");
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		String[] splitted = waypointCoordinates.split(";");
		int i = 0;
		for(String s : splitted) {
			String[] sp = s.split(",");
			LightPole p = new LightPole(simulator, "wp"+i, Double.parseDouble(sp[0]), Double.parseDouble(sp[1]), 0.1);
			waypoints.add(p);
			addObject(p);
			i++;
		}
		
		for(Robot r : robots)
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
	}

	@Override
	public void update(double time) {
		
	}

}