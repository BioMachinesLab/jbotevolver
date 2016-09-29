package behaviors;

import roommaze.TMazeEnvironment;
import simulation.Simulator;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class DestructiveBehavior extends Behavior{

	private TMazeEnvironment env;
	
	public DestructiveBehavior(Simulator simulator, Robot r, Arguments args) {
		super(simulator, r, args);
		this.env = (TMazeEnvironment)simulator.getEnvironment();
	}
	
	@Override
	public void controlStep(double time) {
		env.killSample(true);
	}
}