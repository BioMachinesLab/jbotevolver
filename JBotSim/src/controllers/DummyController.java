package controllers;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class DummyController extends Controller {
	
	public DummyController(Simulator simulator, Robot robot, Arguments args) {
		super(simulator, robot, args);
	}

	@Override
	public void controlStep(double time) {}
}