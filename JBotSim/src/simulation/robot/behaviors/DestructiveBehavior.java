package simulation.robot.behaviors;

import simulation.Simulator;
import simulation.environment.TMazeEnvironment;
import simulation.robot.Robot;

public class DestructiveBehavior extends Behavior{

	public DestructiveBehavior(Simulator simulator, Robot r, boolean lock) {
		super(simulator, r, lock);
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public void applyBehavior() {
		TMazeEnvironment t = (TMazeEnvironment) simulator.getEnvironment();
		t.killSample(true);
	}
}
