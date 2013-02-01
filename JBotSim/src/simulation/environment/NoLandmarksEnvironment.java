package simulation.environment;

import simulation.Simulator;
import simulation.util.Arguments;

public class NoLandmarksEnvironment extends Environment {

	private double forageLimit, forbiddenArea;
	public NoLandmarksEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
	}
	
	public double getForageRadius() {
		return forageLimit;
	}

	public double getForbiddenArea() {
		return forbiddenArea;
	}
	
	@Override
	public void update(double time) {}
}