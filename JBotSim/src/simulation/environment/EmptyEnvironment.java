package simulation.environment;

import simulation.Simulator;
import simulation.util.Arguments;

public class EmptyEnvironment extends Environment {

	private double forageLimit;
	private double forbiddenArea;
	
	public EmptyEnvironment(Simulator simulator, Arguments arguments) {
		super(simulator, arguments);
	}
	
	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
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