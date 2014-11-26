package controllers;

import simpletestbehaviors.GoToWaypointCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

import commoninterface.AquaticDroneCI;
import commoninterface.CILogger;
import commoninterface.CIStdOutLogger;

public class GoToWayPointController extends Controller {

	private GoToWaypointCIBehavior goToWayPointBehavior;
	
	public GoToWayPointController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		CILogger logger = new CIStdOutLogger((AquaticDroneCI) robot);
		String[] argsString = new String[]{"angletolerance=10", "distancetolerance=3"};
		goToWayPointBehavior = new GoToWaypointCIBehavior(argsString, (AquaticDroneCI) robot, logger);
	}

	@Override
	public void controlStep(double time) {
		goToWayPointBehavior.step();
	}

}
