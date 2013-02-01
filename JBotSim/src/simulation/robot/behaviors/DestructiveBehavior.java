package simulation.robot.behaviors;

import simulation.Simulator;
import simulation.environment.TMazeEnvironment;
import simulation.robot.Robot;

public class DestructiveBehavior extends Behavior{

	private TMazeEnvironment env;
	
	public DestructiveBehavior(Simulator simulator, Robot r, boolean lock) {
		super(simulator, r, lock);
		this.env = (TMazeEnvironment)simulator.getEnvironment();
	}

	@Override
	public boolean isLocked() {
		return false;
	}

	@Override
	public void applyBehavior() {
		env.killSample(true);
	}
}
