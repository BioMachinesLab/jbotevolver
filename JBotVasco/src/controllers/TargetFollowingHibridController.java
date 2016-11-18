package controllers;

import commoninterface.AquaticDroneCI;
import commoninterface.RobotCI;
import commoninterface.entities.Waypoint;
import commoninterface.entities.target.Target;
import commoninterface.mathutils.Vector2d;
import commoninterface.sensors.TargetComboCISensor;
import commoninterface.utils.CIArguments;
import commoninterface.utils.CoordinateUtilities;
import simpletestbehaviors.GoToWaypointCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class TargetFollowingHibridController extends DroneNeuralNetworkController {
	private static final long serialVersionUID = -9125420957879778962L;
	public boolean commutationActive = false;
	public TargetComboCISensor targetComboCISensor = null;
	protected GoToWaypointCIBehavior gotoWaypoint;

	public TargetFollowingHibridController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);

		if (robot instanceof AquaticDroneCI) {
			targetComboCISensor = (TargetComboCISensor) ((AquaticDroneCI) robot)
					.getCISensorByType(TargetComboCISensor.class);
			commutationActive = targetComboCISensor != null;
			gotoWaypoint = new GoToWaypointCIBehavior(new CIArguments(args.getCompleteArgumentString()),
					(RobotCI) robot);
		}
	}

	@Override
	public void controlStep(double time) {
		if (commutationActive) {
			if (isInsideTarget(targetComboCISensor.getConsideringTarget())) {
				Waypoint wp = new Waypoint("wp", targetComboCISensor.getConsideringTarget().getLatLon());
				((AquaticDroneCI) robot).setActiveWaypoint(wp);
				gotoWaypoint.step(time);
			} else {
				super.controlStep(time);
			}
		} else {
			super.controlStep(time);
		}
	}

	private boolean isInsideTarget(Target target) {
		Vector2d pos = CoordinateUtilities.GPSToCartesian(target.getLatLon());
		mathutils.Vector2d robotPosition = robot.getPosition();

		return pos.distanceTo(new Vector2d(robotPosition.x, robotPosition.y)) <= target.getRadius()
				|| robot.getName().equals(target.getOccupantID());
	}
}
