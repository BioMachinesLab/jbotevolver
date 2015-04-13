package controllers;

import commoninterface.ThymioCI;
import commoninterface.utils.CIArguments;
import simpletestbehaviors.ThymioRandomWalkCIBehavior;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class ThymioMovementController extends Controller{

	private ThymioRandomWalkCIBehavior randomWalk;
	
	public ThymioMovementController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
		randomWalk = new ThymioRandomWalkCIBehavior(new CIArguments(args.getCompleteArgumentString()), (ThymioCI)robot);
	}

	@Override
	public void controlStep(double time) {
		randomWalk.step(time);
	}
	
}
