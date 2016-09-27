package behaviors;

import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class DummyBehavior extends Behavior {
	
	public DummyBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
	}

	@Override
	public String toString() {
		return "DummyBehavior";		
	}

	@Override
	public void controlStep(double time) {}
}
