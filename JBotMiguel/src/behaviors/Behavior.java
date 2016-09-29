package behaviors;

import controllers.Controller;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public abstract class Behavior extends Controller{
	
	protected int numberOfOutputs = 1;
	protected boolean lock = false;
	protected boolean isLocked = false;
	
	public Behavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		
		lock = args.getArgumentAsIntOrSetDefault("lock", 0) == 1;
		this.robot = r;
	}

	public boolean isLocked() {
		return isLocked;
	}
}