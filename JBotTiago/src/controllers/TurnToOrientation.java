package controllers;

import simpletestbehaviors.TurnToOrientationCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

import commoninterface.AquaticDroneCI;
import commoninterface.CILogger;
import commoninterface.CIStdOutLogger;

public class TurnToOrientation extends Controller {

	private TurnToOrientationCIBehavior turnBehavior;
	
	public TurnToOrientation(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		CILogger logger = new CIStdOutLogger((AquaticDroneCI) robot);
		String[] argsString = new String[]{"target=0", "tolerance=20"};
		turnBehavior = new TurnToOrientationCIBehavior(argsString, (AquaticDroneCI) robot, logger);
	}

	@Override
	public void controlStep(double time) {
		turnBehavior.step();
	}
	
}
