package simulation.robot.sensor;

import java.util.ArrayList;

import objects.Entity;
import objects.Waypoint;
import simulation.Simulator;
import simulation.physicalobjects.PhysicalObject;
import simulation.robot.Robot;
import simulation.robot.sensors.Sensor;
import simulation.util.Arguments;
import commoninterface.AquaticDroneCI;
import commoninterface.CISensor;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;

public class WaypointSensor extends Sensor{
	
	private double[] readings = {0,0};
	private double range = 1;

	public WaypointSensor(Simulator simulator, int id, Robot robot,
			Arguments args) {
		super(simulator, id, robot, args);
		range = args.getArgumentAsDoubleOrSetDefault("range", range);
	}
	
	@Override
	public double getSensorReading(int sensorNumber) {
		return readings[sensorNumber];
	}

	@Override
	public void update(double time, ArrayList<PhysicalObject> teleported) {		
		double closestDistance = Double.MAX_VALUE;
		
		AquaticDroneCI drone = (AquaticDroneCI)robot;
		Vector2d robotPos = new Vector2d(drone.getGPSLatitude(), drone.getGPSLongitude());
		
		for(Entity e : drone.getEntities()) {
			if(e instanceof Waypoint) {
				Vector2d latLon = new Vector2d(e.getLatitude(),e.getLongitude());
				
				double currentDistance = CoordinateUtilities.distanceInMeters(robotPos,latLon);
				
				if(currentDistance < closestDistance) {
				
					double currentOrientation = drone.getCompassOrientationInDegrees();
					double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotPos,latLon);
					
					double difference = currentOrientation - coordinatesAngle;
					
					difference%=360;
					
					if(difference > 180){
						difference = -((180 -difference) + 180);
					}
					readings[0] = difference;
					readings[1] = currentDistance;
					closestDistance = currentDistance;
				}
				
				break;
			}
		}
	}
	
	public double getRange() {
		return range;
	}
}
