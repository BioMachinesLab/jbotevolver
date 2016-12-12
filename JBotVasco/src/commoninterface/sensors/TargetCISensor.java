package commoninterface.sensors;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Entity;
import commoninterface.entities.formation.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import commoninterface.utils.jcoord.LatLon;
import simulation.robot.AquaticDrone;

public class TargetCISensor extends WaypointCISensor {
	private static final long serialVersionUID = -8046529238682316798L;
	private boolean normalize = false;

	public TargetCISensor(int id, RobotCI robot, CIArguments args) {
		super(id, robot, args);

		normalize = args.getArgumentAsIntOrSetDefault("normalize", 0) == 1;
	}

	/**
	 * Sets the sensor reading in the readings array. The first element of the
	 * array corresponds to the difference between the robot orientation and the
	 * azimut to target. The second element corresponds to the distance from
	 * robot to the target readings[0] = difference between robot orientation an
	 * the azimut to target readings[1] = distance to target (in meters)
	 */
	@Override
	public void update(double time, Object[] entities) {

		LatLon robotLatLon = ((AquaticDroneCI) robot).getGPSLatLon();
		Target target = getClosestTarget();

		if (target == null) {
			throw new NullPointerException("No target defined!");
		} else {
			LatLon latLon = target.getLatLon();
			double currentDistance = CoordinateUtilities.distanceInMeters(robotLatLon, latLon);

			if (currentDistance <= range) {
				double currentOrientation = ((AquaticDroneCI) robot).getCompassOrientationInDegrees();
				double coordinatesAngle = CoordinateUtilities.angleInDegrees(robotLatLon, latLon);

				double difference = currentOrientation - coordinatesAngle;

				difference %= 360;

				if (difference > 180) {
					difference = -((180 - difference) + 180);
				}

				if (normalize) {
					readings[0] = difference / 360;
					readings[1] = currentDistance / range;
				} else {
					readings[0] = difference;
					readings[1] = currentDistance;
				}
			} else {
				readings[0] = 0;
				readings[1] = Double.MAX_VALUE;
			}
			// } else {
			// // If there is no target, the sensor
			// // should act as if the robot is near target
			// readings[0] = 0.5;
			// readings[1] = 1;
		}

	}

	private Target getClosestTarget() {
		Target target = null;

		Vector2d location = null;
		if (robot instanceof AquaticDrone) {
			location = CoordinateUtilities.GPSToCartesian(((AquaticDrone) robot).getGPSLatLon());
		}

		if (location == null) {
			throw new NullPointerException("Incompatible robot instance!");
		}

		double minDistance = Double.MAX_VALUE;
		for (Entity ent : ((AquaticDroneCI) robot).getEntities()) {
			if (ent instanceof Target) {
				Vector2d pos = CoordinateUtilities.GPSToCartesian(((Target) ent).getLatLon());
				if (location.distanceTo(pos) < minDistance) {
					minDistance = location.distanceTo(pos);
					target = (Target) ent;
				}

			}
		}

		return target;
	}
}
