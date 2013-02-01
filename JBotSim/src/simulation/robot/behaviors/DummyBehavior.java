package simulation.robot.behaviors;

import simulation.Simulator;
import simulation.robot.Robot;

public class DummyBehavior extends Behavior {
	
	private int id;
	
	public DummyBehavior(Simulator simulator, Robot r, boolean lock, int id) {
		super(simulator, r, lock);
		this.id = id;
	}

	@Override
	public boolean isLocked() {
		return false;
	}
	
	@Override
	public String toString() {
		return "DummyBehavior "+id;		
	}

	@Override
	public void applyBehavior() {}

}
